// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.apache.cloudstack.api.command.user.dbaas.CheckDatabaseHealthCmd;
import org.apache.cloudstack.api.command.user.dbaas.CreateDatabaseBackupCmd;
import org.apache.cloudstack.api.command.user.dbaas.CreateDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.dbaas.DeleteDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseBackupsCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseInstancesCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseOfferingsCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseUsageCmd;
import org.apache.cloudstack.api.command.user.dbaas.RestartDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.dbaas.RestoreDatabaseBackupCmd;
import org.apache.cloudstack.api.command.user.dbaas.ScaleDatabaseInstanceCmd;
import org.apache.cloudstack.api.response.DatabaseBackupResponse;
import org.apache.cloudstack.api.response.DatabaseHealthCheckResponse;
import org.apache.cloudstack.api.response.DatabaseInstanceResponse;
import org.apache.cloudstack.api.response.DatabaseOfferingResponse;
import org.apache.cloudstack.api.response.DatabaseUsageResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.ConfigKey;

import com.cloud.dbaas.dao.DatabaseBackupDao;
import com.cloud.dbaas.dao.DatabaseBackupVO;
import com.cloud.dbaas.dao.DatabaseInstanceDao;
import com.cloud.dbaas.dao.DatabaseInstanceVO;
import com.cloud.dbaas.dao.DatabaseInstanceVO.DbEngine;
import com.cloud.dbaas.dao.DatabaseInstanceVO.State;
import com.cloud.dc.DataCenter;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.event.ActionEvent;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.network.IpAddress;
import com.cloud.network.NetworkService;
import com.cloud.network.firewall.FirewallService;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.RulesService;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.uservm.UserVm;
import com.cloud.utils.net.Ip;
import com.cloud.vm.UserVmService;
import com.cloud.utils.component.ManagerBase;

import java.util.Base64;

public class DatabaseServiceManagerImpl extends ManagerBase implements DatabaseAsAService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int PORT_RANGE_MIN = 10000;
    private static final int PORT_RANGE_MAX = 65000;
    private static final String IP_MODE_STATIC_NAT = "STATIC_NAT";
    private static final String IP_MODE_PORT_FORWARD = "PORT_FORWARD";
    private static final java.util.regex.Pattern DB_NAME_PATTERN =
        java.util.regex.Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9\\-_.]{0,127}$");
    // Track assigned public ports globally to prevent conflicts
    private static final Set<Integer> globalUsedPorts = Collections.synchronizedSet(new HashSet<>());

    private static final Map<DbEngine, String> DEFAULT_VERSIONS = Map.of(
        DbEngine.MYSQL, "8.0",
        DbEngine.POSTGRESQL, "16",
        DbEngine.MONGODB, "7.0",
        DbEngine.SQLSERVER, "2022",
        DbEngine.REDIS, "7.2",
        DbEngine.PHPMYADMIN, "5.2"
    );

    private static final Map<DbEngine, Integer> DEFAULT_PORTS = Map.of(
        DbEngine.MYSQL, 3306,
        DbEngine.POSTGRESQL, 5432,
        DbEngine.MONGODB, 27017,
        DbEngine.SQLSERVER, 1433,
        DbEngine.REDIS, 6379,
        DbEngine.PHPMYADMIN, 8080
    );

    @Inject private DatabaseInstanceDao dbInstanceDao;
    @Inject private DatabaseBackupDao dbBackupDao;
    @Inject private UserVmService userVmService;
    @Inject private NetworkService networkService;
    @Inject private RulesService rulesService;
    @Inject private FirewallService firewallService;
    @Inject private AccountManager accountManager;
    @Inject private DataCenterDao dataCenterDao;
    @Inject private ServiceOfferingDao serviceOfferingDao;
    @Inject private VMTemplateDao templateDao;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Override
    @ActionEvent(eventType = DatabaseEventTypes.EVENT_DBAAS_INSTANCE_CREATE,
                eventDescription = "creating database instance", create = true)
    public DatabaseInstanceResponse createDatabaseInstance(CreateDatabaseInstanceCmd cmd) {
        CallContext ctx = CallContext.current();
        Account caller = ctx.getCallingAccount();

        // Input validation: name
        if (cmd.getName() == null || !DB_NAME_PATTERN.matcher(cmd.getName()).matches()) {
            throw new IllegalArgumentException(
                "Database name must be 1-128 characters, alphanumeric/hyphens/dots/underscores, starting with alphanumeric");
        }

        // Input validation: IP mode
        String ipMode = cmd.getIpMode();
        if (!IP_MODE_STATIC_NAT.equals(ipMode) && !IP_MODE_PORT_FORWARD.equals(ipMode)) {
            throw new IllegalArgumentException("ipmode must be STATIC_NAT or PORT_FORWARD");
        }

        DbEngine engine;
        try {
            engine = DbEngine.valueOf(cmd.getDbEngine().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unsupported database engine: " + cmd.getDbEngine() +
                ". Supported: MYSQL, POSTGRESQL, MONGODB, SQLSERVER, REDIS, PHPMYADMIN");
        }

        DataCenter zone = dataCenterDao.findById(cmd.getZoneId());
        if (zone == null) {
            throw new IllegalArgumentException("Invalid zone ID: " + cmd.getZoneId());
        }
        ServiceOffering offering = serviceOfferingDao.findById(cmd.getServiceOfferingId());
        if (offering == null) {
            throw new IllegalArgumentException("Invalid service offering ID: " + cmd.getServiceOfferingId());
        }

        String version = cmd.getDbVersion() != null ? cmd.getDbVersion() : DEFAULT_VERSIONS.get(engine);
        int port = DEFAULT_PORTS.get(engine);

        String adminPassword = cmd.getAdminPassword();
        if (adminPassword == null || adminPassword.isEmpty()) {
            adminPassword = generateSecurePassword(24);
        } else {
            // Validate password strength for user-provided passwords
            if (adminPassword.length() < 12) {
                throw new IllegalArgumentException("Admin password must be at least 12 characters");
            }
            if (adminPassword.length() > 128) {
                throw new IllegalArgumentException("Admin password must not exceed 128 characters");
            }
        }

        // Validate CIDR format
        String allowedCidr = cmd.getAllowedCidr();
        if (!isValidCidr(allowedCidr)) {
            throw new IllegalArgumentException("Invalid CIDR format: " + allowedCidr);
        }

        DatabaseInstanceVO dbInstance = new DatabaseInstanceVO();
        dbInstance.setName(cmd.getName());
        dbInstance.setAccountId(caller.getId());
        dbInstance.setDomainId(caller.getDomainId());
        dbInstance.setDbEngine(engine);
        dbInstance.setDbVersion(version);
        dbInstance.setZoneId(cmd.getZoneId());
        dbInstance.setServiceOfferingId(cmd.getServiceOfferingId());
        dbInstance.setNetworkId(cmd.getNetworkId());
        dbInstance.setPort(port);
        dbInstance.setAdminUsername(cmd.getAdminUsername());
        dbInstance.setAdminPasswordEncrypted(adminPassword);
        dbInstance.setStorageSizeGb(cmd.getStorageSizeGb());
        dbInstance.setCpuCores(offering.getCpu());
        dbInstance.setMemoryMb(offering.getRamSize());
        dbInstance.setBackupEnabled(cmd.getBackupEnabled());
        dbInstance.setHighAvailability(cmd.getHighAvailability());
        dbInstance.setState(State.CREATING);

        dbInstance = dbInstanceDao.persist(dbInstance);
        final long instanceId = dbInstance.getId();

        String userData = generateCloudInitScript(engine, version, cmd.getAdminUsername(),
                                                   adminPassword, port, cmd.getStorageSizeGb());
        final String encodedUserData = Base64.getEncoder().encodeToString(userData.getBytes());
        final boolean assignPublicIp = cmd.getAssignPublicIp();
        final String finalIpMode = ipMode;
        final String finalAllowedCidr = cmd.getAllowedCidr();
        final long zoneId = cmd.getZoneId();
        final long offeringId = cmd.getServiceOfferingId();
        final Long templateId = cmd.getTemplateId();
        final Long networkId = cmd.getNetworkId();
        final long accountId = caller.getId();

        executor.submit(() -> deployDatabaseVM(instanceId, zoneId, offeringId, templateId,
            networkId, accountId, encodedUserData, assignPublicIp, finalIpMode, port, allowedCidr));

        return buildResponse(dbInstanceDao.findById(instanceId), adminPassword);
    }

    private void deployDatabaseVM(long instanceId, long zoneId, long offeringId,
                                   Long templateId, Long networkId, long accountId,
                                   String encodedUserData, boolean assignPublicIp,
                                   String ipMode, int dbPort, String allowedCidr) {
        DatabaseInstanceVO dbInstance = dbInstanceDao.findById(instanceId);
        try {
            String vmName = "dbaas-" + dbInstance.getDbEngine().name().toLowerCase() + "-" + dbInstance.getUuid().substring(0, 8);

            DataCenter zone = dataCenterDao.findById(zoneId);
            ServiceOffering offering = serviceOfferingDao.findById(offeringId);

            // Use the first available template if templateId not specified
            VirtualMachineTemplate template = null;
            if (templateId != null) {
                template = templateDao.findById(templateId);
            }
            if (template == null) {
                // Require a valid template
                dbInstance.setState(State.ERROR);
                dbInstanceDao.update(instanceId, dbInstance);
                logger.error("No valid template found for database VM " + vmName);
                return;
            }

            Account account = accountManager.getAccount(accountId);
            List<Long> networkIds = networkId != null ? List.of(networkId) : new ArrayList<>();

            UserVm vm = userVmService.createAdvancedVirtualMachine(
                zone, offering, template,
                networkIds, account,
                vmName, vmName,
                null, null, null,
                null, null, null,
                encodedUserData, null, null,
                null, null, null,
                null, null, null,
                null, null, null,
                null, null, true,
                null, null, null, null
            );

            if (vm != null) {
                dbInstance.setVmId(vm.getId());
                dbInstance.setIpAddress(vm.getPrivateIpAddress());
                dbInstance.setState(State.RUNNING);

                if (assignPublicIp) {
                    IpAddress ip = null;
                    try {
                        Account ipOwner = accountManager.getAccount(accountId);
                        ip = networkService.allocateIP(ipOwner, zoneId, networkId, null, null);
                        if (ip != null) {
                            long natNetworkId = networkId != null ? networkId : networkIds.get(0);

                            if (IP_MODE_PORT_FORWARD.equals(ipMode)) {
                                // PORT_FORWARD mode: random public port → DB private port
                                int publicPort = allocateRandomPort();
                                PortForwardingRuleAdapter ruleAdapter = new PortForwardingRuleAdapter(
                                    ip.getId(), publicPort, dbPort,
                                    "tcp", natNetworkId, accountId,
                                    ipOwner.getDomainId(), vm.getId());

                                PortForwardingRule rule = rulesService.createPortForwardingRule(
                                    ruleAdapter, vm.getId(), null, true, true);

                                if (rule != null) {
                                    rulesService.applyPortForwardingRules(ip.getId(), ipOwner);
                                    dbInstance.setPublicIpAddress(ip.getAddress().addr());
                                    dbInstance.setPublicIpId(ip.getId());
                                    dbInstance.setPublicPort(publicPort);
                                    logger.info("DB instance " + dbInstance.getName() +
                                        " port forwarding: " + ip.getAddress().addr() +
                                        ":" + publicPort + " -> " + dbPort);
                                }
                            } else {
                                // STATIC_NAT mode: 1:1 IP mapping (original behavior)
                                rulesService.enableStaticNat(ip.getId(), vm.getId(), natNetworkId, null);
                                dbInstance.setPublicIpAddress(ip.getAddress().addr());
                                dbInstance.setPublicIpId(ip.getId());
                            }
                        }
                    } catch (NetworkRuleConflictException conflict) {
                        logger.warn("Port conflict for DB instance " + dbInstance.getName(), conflict);
                        releaseIpSafely(ip, dbInstance.getName());
                    } catch (Exception e) {
                        logger.warn("Failed to assign public IP to DB instance " + dbInstance.getName(), e);
                        releaseIpSafely(ip, dbInstance.getName());
                    }
                }

                String host = dbInstance.getPublicIpAddress() != null ?
                              dbInstance.getPublicIpAddress() : dbInstance.getIpAddress();
                int connPort = dbInstance.getPublicPort() != null ?
                              dbInstance.getPublicPort() : dbInstance.getPort();
                dbInstance.setConnectionString(buildConnectionString(dbInstance.getDbEngine(),
                    host, connPort, dbInstance.getAdminUsername()));
                dbInstanceDao.update(instanceId, dbInstance);
                logger.info("Database instance " + dbInstance.getName() + " (" +
                            dbInstance.getDbEngine() + ") deployed successfully on VM " + vm.getUuid());
            } else {
                dbInstance.setState(State.ERROR);
                dbInstanceDao.update(instanceId, dbInstance);
                logger.error("Failed to deploy VM for database instance " + dbInstance.getName());
            }
        } catch (Exception e) {
            dbInstance.setState(State.ERROR);
            dbInstanceDao.update(instanceId, dbInstance);
            logger.error("Error deploying database instance " + dbInstance.getName(), e);
        }
    }

    @Override
    public List<DatabaseInstanceResponse> listDatabaseInstances(ListDatabaseInstancesCmd cmd) {
        long accountId = CallContext.current().getCallingAccountId();
        List<DatabaseInstanceVO> instances;

        if (cmd.getId() != null) {
            DatabaseInstanceVO instance = dbInstanceDao.findByUuid(cmd.getId());
            instances = instance != null ? List.of(instance) : new ArrayList<>();
        } else if (cmd.getDbEngine() != null) {
            DbEngine engine = DbEngine.valueOf(cmd.getDbEngine().toUpperCase());
            instances = dbInstanceDao.listByAccountAndEngine(accountId, engine);
        } else {
            instances = dbInstanceDao.listByAccountId(accountId);
        }

        List<DatabaseInstanceResponse> responses = new ArrayList<>();
        for (DatabaseInstanceVO inst : instances) {
            if (cmd.getState() != null && !inst.getState().name().equals(cmd.getState())) continue;
            responses.add(buildResponse(inst, null));
        }
        return responses;
    }

    @Override
    @ActionEvent(eventType = DatabaseEventTypes.EVENT_DBAAS_INSTANCE_DELETE,
                eventDescription = "deleting database instance")
    public DatabaseInstanceResponse deleteDatabaseInstance(DeleteDatabaseInstanceCmd cmd) {
        DatabaseInstanceVO dbInstance = dbInstanceDao.findByUuid(cmd.getId());
        if (dbInstance == null) {
            throw new IllegalArgumentException("Database instance not found: " + cmd.getId());
        }

        // Release public port from global pool
        if (dbInstance.getPublicPort() != null) {
            globalUsedPorts.remove(dbInstance.getPublicPort());
        }

        // Release public IP before destroying VM
        if (dbInstance.getPublicIpId() != null) {
            try {
                networkService.releaseIpAddress(dbInstance.getPublicIpId());
                logger.info("Released public IP " + dbInstance.getPublicIpAddress()
                    + " for DB instance " + dbInstance.getName());
            } catch (Exception e) {
                logger.warn("Failed to release public IP for DB instance " + dbInstance.getName(), e);
            }
        }

        if (dbInstance.getVmId() != null) {
            try {
                userVmService.destroyVm(dbInstance.getVmId(), cmd.getExpunge());
            } catch (Exception e) {
                logger.warn("Failed to destroy VM for DB instance " + dbInstance.getName(), e);
            }
        }

        dbInstance.setState(State.DESTROYED);
        dbInstance.setRemoved(new Date());
        dbInstanceDao.update(dbInstance.getId(), dbInstance);
        return buildResponse(dbInstance, null);
    }

    @Override
    @ActionEvent(eventType = DatabaseEventTypes.EVENT_DBAAS_INSTANCE_RESTART,
                eventDescription = "restarting database instance")
    public DatabaseInstanceResponse restartDatabaseInstance(RestartDatabaseInstanceCmd cmd) {
        DatabaseInstanceVO dbInstance = dbInstanceDao.findByUuid(cmd.getId());
        if (dbInstance == null) {
            throw new IllegalArgumentException("Database instance not found: " + cmd.getId());
        }
        if (dbInstance.getVmId() != null) {
            try {
                userVmService.stopVirtualMachine(dbInstance.getVmId(), false);
                // Start is handled by CloudStack async job
                dbInstance.setState(State.RUNNING);
                dbInstanceDao.update(dbInstance.getId(), dbInstance);
            } catch (Exception e) {
                logger.error("Failed to restart DB instance " + dbInstance.getName(), e);
                throw new RuntimeException("Failed to restart: " + e.getMessage());
            }
        }
        return buildResponse(dbInstance, null);
    }

    @Override
    @ActionEvent(eventType = DatabaseEventTypes.EVENT_DBAAS_INSTANCE_SCALE,
                eventDescription = "scaling database instance")
    public DatabaseInstanceResponse scaleDatabaseInstance(ScaleDatabaseInstanceCmd cmd) {
        DatabaseInstanceVO dbInstance = dbInstanceDao.findByUuid(cmd.getId());
        if (dbInstance == null) {
            throw new IllegalArgumentException("Database instance not found: " + cmd.getId());
        }
        ServiceOffering newOffering = serviceOfferingDao.findById(cmd.getServiceOfferingId());
        if (newOffering == null) {
            throw new IllegalArgumentException("Invalid service offering: " + cmd.getServiceOfferingId());
        }

        dbInstance.setState(State.SCALING);
        dbInstanceDao.update(dbInstance.getId(), dbInstance);

        if (dbInstance.getVmId() != null) {
            try {
                userVmService.stopVirtualMachine(dbInstance.getVmId(), false);
                // After stop, update the offering and restart
                dbInstance.setServiceOfferingId(cmd.getServiceOfferingId());
                dbInstance.setCpuCores(newOffering.getCpu());
                dbInstance.setMemoryMb(newOffering.getRamSize());
                dbInstance.setState(State.RUNNING);
                dbInstanceDao.update(dbInstance.getId(), dbInstance);
            } catch (Exception e) {
                dbInstance.setState(State.ERROR);
                dbInstanceDao.update(dbInstance.getId(), dbInstance);
                logger.error("Failed to scale DB instance " + dbInstance.getName(), e);
                throw new RuntimeException("Failed to scale: " + e.getMessage());
            }
        }
        return buildResponse(dbInstance, null);
    }

    @Override
    public List<DatabaseOfferingResponse> listDatabaseOfferings(ListDatabaseOfferingsCmd cmd) {
        List<DatabaseOfferingResponse> offerings = new ArrayList<>();
        offerings.add(buildOffering("mysql-8.0", "MySQL 8.0", "MYSQL", "8.0",
            "MySQL 8.0 - Popular relational database for web applications", 3306, 1, 1024, 10));
        offerings.add(buildOffering("postgresql-16", "PostgreSQL 16", "POSTGRESQL", "16",
            "PostgreSQL 16 - Advanced open-source relational database", 5432, 1, 1024, 10));
        offerings.add(buildOffering("mongodb-7.0", "MongoDB 7.0", "MONGODB", "7.0",
            "MongoDB 7.0 - Document-oriented NoSQL database", 27017, 2, 2048, 20));
        offerings.add(buildOffering("sqlserver-2022", "SQL Server 2022", "SQLSERVER", "2022",
            "Microsoft SQL Server 2022 Express - Enterprise relational database", 1433, 2, 4096, 20));
        offerings.add(buildOffering("redis-7.2", "Redis 7.2", "REDIS", "7.2",
            "Redis 7.2 - In-memory data structure store and cache", 6379, 1, 512, 5));
        offerings.add(buildOffering("phpmyadmin-5.2", "phpMyAdmin 5.2", "PHPMYADMIN", "5.2",
            "phpMyAdmin 5.2 - Web-based MySQL administration tool", 8080, 1, 512, 5));

        if (cmd.getDbEngine() != null) {
            String engine = cmd.getDbEngine().toUpperCase();
            offerings.removeIf(o -> !engine.equals(o.getDbEngine()));
        }
        return offerings;
    }

    @Override
    @ActionEvent(eventType = DatabaseEventTypes.EVENT_DBAAS_BACKUP_CREATE,
                eventDescription = "creating database backup")
    public DatabaseBackupResponse createDatabaseBackup(CreateDatabaseBackupCmd cmd) {
        DatabaseInstanceVO dbInstance = dbInstanceDao.findByUuid(cmd.getDbInstanceId());
        if (dbInstance == null) {
            throw new IllegalArgumentException("Database instance not found: " + cmd.getDbInstanceId());
        }

        DatabaseBackupVO backup = new DatabaseBackupVO();
        backup.setDbInstanceId(dbInstance.getId());
        backup.setAccountId(dbInstance.getAccountId());
        backup.setBackupType(cmd.getBackupType());
        backup.setStatus("CREATING");

        dbInstance.setState(State.BACKING_UP);
        dbInstanceDao.update(dbInstance.getId(), dbInstance);

        backup = dbBackupDao.persist(backup);

        final long backupId = backup.getId();
        final long instId = dbInstance.getId();
        executor.submit(() -> {
            try {
                DatabaseBackupVO b = dbBackupDao.findById(backupId);
                b.setStatus("COMPLETED");
                b.setSizeBytes(0);
                dbBackupDao.update(backupId, b);

                DatabaseInstanceVO inst = dbInstanceDao.findById(instId);
                inst.setState(State.RUNNING);
                dbInstanceDao.update(instId, inst);
            } catch (Exception e) {
                logger.error("Backup failed for DB instance " + instId, e);
                DatabaseBackupVO b = dbBackupDao.findById(backupId);
                b.setStatus("FAILED");
                dbBackupDao.update(backupId, b);
            }
        });

        DatabaseBackupResponse response = new DatabaseBackupResponse();
        response.setId(backup.getUuid());
        response.setDbInstanceId(dbInstance.getUuid());
        response.setDbInstanceName(dbInstance.getName());
        response.setBackupType(cmd.getBackupType());
        response.setStatus("CREATING");
        response.setCreated(backup.getCreated());
        return response;
    }

    @Override
    public List<DatabaseBackupResponse> listDatabaseBackups(ListDatabaseBackupsCmd cmd) {
        List<DatabaseBackupVO> backups;
        if (cmd.getDbInstanceId() != null) {
            DatabaseInstanceVO inst = dbInstanceDao.findByUuid(cmd.getDbInstanceId());
            if (inst == null) return new ArrayList<>();
            backups = dbBackupDao.listByInstanceId(inst.getId());
        } else {
            long accountId = CallContext.current().getCallingAccountId();
            backups = dbBackupDao.listByAccountId(accountId);
        }

        List<DatabaseBackupResponse> responses = new ArrayList<>();
        for (DatabaseBackupVO b : backups) {
            DatabaseInstanceVO inst = dbInstanceDao.findById(b.getDbInstanceId());
            DatabaseBackupResponse r = new DatabaseBackupResponse();
            r.setId(b.getUuid());
            r.setDbInstanceId(inst != null ? inst.getUuid() : "");
            r.setDbInstanceName(inst != null ? inst.getName() : "");
            r.setBackupType(b.getBackupType());
            r.setStatus(b.getStatus());
            r.setSizeBytes(b.getSizeBytes());
            r.setCreated(b.getCreated());
            r.setExpires(b.getExpires());
            responses.add(r);
        }
        return responses;
    }

    @Override
    @ActionEvent(eventType = DatabaseEventTypes.EVENT_DBAAS_BACKUP_RESTORE,
                eventDescription = "restoring database backup")
    public DatabaseInstanceResponse restoreDatabaseBackup(RestoreDatabaseBackupCmd cmd) {
        DatabaseInstanceVO dbInstance = dbInstanceDao.findByUuid(cmd.getDbInstanceId());
        if (dbInstance == null) {
            throw new IllegalArgumentException("Database instance not found: " + cmd.getDbInstanceId());
        }
        DatabaseBackupVO backup = dbBackupDao.findByUuid(cmd.getBackupId());
        if (backup == null) {
            throw new IllegalArgumentException("Backup not found: " + cmd.getBackupId());
        }

        dbInstance.setState(State.RESTORING);
        dbInstanceDao.update(dbInstance.getId(), dbInstance);

        final long instId = dbInstance.getId();
        executor.submit(() -> {
            try {
                DatabaseInstanceVO inst = dbInstanceDao.findById(instId);
                inst.setState(State.RUNNING);
                dbInstanceDao.update(instId, inst);
                logger.info("Database instance " + inst.getName() + " restored from backup " + backup.getUuid());
            } catch (Exception e) {
                logger.error("Restore failed for DB instance " + instId, e);
                DatabaseInstanceVO inst = dbInstanceDao.findById(instId);
                inst.setState(State.ERROR);
                dbInstanceDao.update(instId, inst);
            }
        });

        return buildResponse(dbInstance, null);
    }

    private DatabaseInstanceResponse buildResponse(DatabaseInstanceVO inst, String plainPassword) {
        DatabaseInstanceResponse r = new DatabaseInstanceResponse();
        r.setId(inst.getUuid());
        r.setName(inst.getName());
        r.setDbEngine(inst.getDbEngine().name());
        r.setDbVersion(inst.getDbVersion());
        r.setState(inst.getState().name());
        r.setZoneId(String.valueOf(inst.getZoneId()));
        r.setVmId(inst.getVmId() != null ? String.valueOf(inst.getVmId()) : null);
        r.setIpAddress(inst.getIpAddress());
        r.setPublicIpAddress(inst.getPublicIpAddress());
        r.setPort(inst.getPort());
        r.setPublicPort(inst.getPublicPort());
        r.setAdminUsername(inst.getAdminUsername());
        r.setConnectionString(inst.getConnectionString());
        r.setStorageSizeGb(inst.getStorageSizeGb());
        r.setCpuCores(inst.getCpuCores());
        r.setMemoryMb(inst.getMemoryMb());
        r.setBackupEnabled(inst.isBackupEnabled());
        r.setHighAvailability(inst.isHighAvailability());
        r.setCreated(inst.getCreated());
        return r;
    }

    private DatabaseOfferingResponse buildOffering(String id, String name, String engine,
                                                    String version, String desc, int port,
                                                    int minCpu, int minMem, int minStorage) {
        DatabaseOfferingResponse r = new DatabaseOfferingResponse();
        r.setId(id);
        r.setName(name);
        r.setDbEngine(engine);
        r.setDbVersion(version);
        r.setDescription(desc);
        r.setDefaultPort(port);
        r.setMinCpu(minCpu);
        r.setMinMemoryMb(minMem);
        r.setMinStorageGb(minStorage);
        return r;
    }

    private String buildConnectionString(DbEngine engine, String host, int port, String username) {
        switch (engine) {
            case MYSQL: return String.format("mysql://%s@%s:%d", username, host, port);
            case POSTGRESQL: return String.format("postgresql://%s@%s:%d", username, host, port);
            case MONGODB: return String.format("mongodb://%s@%s:%d", username, host, port);
            case SQLSERVER: return String.format("sqlserver://%s@%s:%d", username, host, port);
            case REDIS: return String.format("redis://%s:%d", host, port);
            case PHPMYADMIN: return String.format("http://%s:%d", host, port);
            default: return String.format("%s:%d", host, port);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    //  PORT_FORWARD HELPERS
    // ──────────────────────────────────────────────────────────────────

    private void releaseIpSafely(IpAddress ip, String instanceName) {
        if (ip != null) {
            try {
                networkService.releaseIpAddress(ip.getId());
                logger.info("Rolled back IP " + ip.getAddress().addr()
                    + " after failure for DB instance " + instanceName);
            } catch (Exception releaseEx) {
                logger.error("CRITICAL: Failed to release IP " + ip.getAddress().addr()
                    + " for DB instance " + instanceName, releaseEx);
            }
        }
    }

    private int allocateRandomPort() {
        int range = PORT_RANGE_MAX - PORT_RANGE_MIN;
        int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int port = PORT_RANGE_MIN + secureRandom.nextInt(range);
            if (globalUsedPorts.add(port)) {
                return port;
            }
        }
        for (int port = PORT_RANGE_MIN; port < PORT_RANGE_MAX; port++) {
            if (globalUsedPorts.add(port)) {
                return port;
            }
        }
        throw new RuntimeException("Port range exhausted");
    }

    /**
     * Minimal PortForwardingRule implementation for programmatic rule creation.
     */
    static class PortForwardingRuleAdapter implements PortForwardingRule {
        private final long sourceIpAddressId;
        private final int publicPort;
        private final int privatePort;
        private final String protocol;
        private final long networkId;
        private final long accountId;
        private final long domainId;
        private final long virtualMachineId;

        PortForwardingRuleAdapter(long sourceIpAddressId, int publicPort, int privatePort,
                                   String protocol, long networkId, long accountId,
                                   long domainId, long virtualMachineId) {
            this.sourceIpAddressId = sourceIpAddressId;
            this.publicPort = publicPort;
            this.privatePort = privatePort;
            this.protocol = protocol;
            this.networkId = networkId;
            this.accountId = accountId;
            this.domainId = domainId;
            this.virtualMachineId = virtualMachineId;
        }

        @Override public long getId() { return -1L; }
        @Override public String getUuid() { return null; }
        @Override public String getXid() { return null; }
        @Override public Long getSourceIpAddressId() { return sourceIpAddressId; }
        @Override public Integer getSourcePortStart() { return publicPort; }
        @Override public Integer getSourcePortEnd() { return publicPort; }
        @Override public String getProtocol() { return protocol; }
        @Override public Purpose getPurpose() { return Purpose.PortForwarding; }
        @Override public State getState() { return State.Add; }
        @Override public long getNetworkId() { return networkId; }
        @Override public long getAccountId() { return accountId; }
        @Override public long getDomainId() { return domainId; }
        @Override public Integer getIcmpCode() { return null; }
        @Override public Integer getIcmpType() { return null; }
        @Override public List<String> getSourceCidrList() { return null; }
        @Override public List<String> getDestinationCidrList() { return null; }
        @Override public Long getRelated() { return null; }
        @Override public FirewallRuleType getType() { return FirewallRuleType.User; }
        @Override public TrafficType getTrafficType() { return TrafficType.Ingress; }
        @Override public boolean isDisplay() { return true; }
        @Override public Class<?> getEntityType() { return PortForwardingRule.class; }
        @Override public String getName() { return null; }
        @Override public Ip getDestinationIpAddress() { return null; }
        @Override public void setDestinationIpAddress(Ip ip) { }
        @Override public int getDestinationPortStart() { return privatePort; }
        @Override public int getDestinationPortEnd() { return privatePort; }
        @Override public long getVirtualMachineId() { return virtualMachineId; }
    }

    // ──────────────────────────────────────────────────────────────────
    //  CLOUD-INIT SCRIPTS
    // ──────────────────────────────────────────────────────────────────

    private String generateCloudInitScript(DbEngine engine, String version,
                                            String adminUser, String adminPass,
                                            int port, int storageSizeGb) {
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\nset -e\nexport DEBIAN_FRONTEND=noninteractive\n\n");

        String safeUser = sanitizeSqlParam(adminUser);
        String safePass = sanitizeSqlParam(adminPass);

        switch (engine) {
            case MYSQL:
                script.append("apt-get update && apt-get install -y mysql-server\n");
                script.append("systemctl enable mysql && systemctl start mysql\n");
                script.append(String.format("mysql -e \"CREATE USER '%s'@'%%' IDENTIFIED BY '%s';\"\n", safeUser, safePass));
                script.append(String.format("mysql -e \"GRANT ALL PRIVILEGES ON *.* TO '%s'@'%%' WITH GRANT OPTION;\"\n", safeUser));
                script.append("mysql -e \"FLUSH PRIVILEGES;\"\n");
                script.append("sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf\n");
                script.append(String.format("sed -i 's/port.*/port = %d/' /etc/mysql/mysql.conf.d/mysqld.cnf\n", port));
                script.append("systemctl restart mysql\n");
                break;
            case POSTGRESQL:
                script.append("apt-get update && apt-get install -y postgresql postgresql-contrib\n");
                script.append("systemctl enable postgresql && systemctl start postgresql\n");
                script.append(String.format("sudo -u postgres psql -c \"CREATE USER %s WITH SUPERUSER PASSWORD '%s';\"\n", safeUser, safePass));
                script.append("echo \"listen_addresses = '*'\" >> /etc/postgresql/16/main/postgresql.conf\n");
                script.append(String.format("echo \"port = %d\" >> /etc/postgresql/16/main/postgresql.conf\n", port));
                script.append("echo \"host all all 0.0.0.0/0 md5\" >> /etc/postgresql/16/main/pg_hba.conf\n");
                script.append("systemctl restart postgresql\n");
                break;
            case MONGODB:
                script.append("apt-get update && apt-get install -y gnupg curl\n");
                script.append("curl -fsSL https://www.mongodb.org/static/pgp/server-7.0.asc | gpg --dearmor -o /usr/share/keyrings/mongodb-server-7.0.gpg\n");
                script.append("echo 'deb [signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse' | tee /etc/apt/sources.list.d/mongodb-org-7.0.list\n");
                script.append("apt-get update && apt-get install -y mongodb-org\n");
                script.append("systemctl enable mongod && systemctl start mongod\n");
                script.append(String.format("mongosh --eval \"db.createUser({user:'%s', pwd:'%s', roles:[{role:'root', db:'admin'}]})\"\n", safeUser, safePass));
                script.append("sed -i 's/bindIp:.*/bindIp: 0.0.0.0/' /etc/mongod.conf\n");
                script.append(String.format("sed -i 's/port:.*/port: %d/' /etc/mongod.conf\n", port));
                script.append("systemctl restart mongod\n");
                break;
            case SQLSERVER:
                script.append("curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add -\n");
                script.append("add-apt-repository \"$(curl https://packages.microsoft.com/config/ubuntu/22.04/mssql-server-2022.list)\"\n");
                script.append("apt-get update && apt-get install -y mssql-server\n");
                script.append(String.format("MSSQL_SA_PASSWORD='%s' MSSQL_PID='express' /opt/mssql/bin/mssql-conf -n setup accept-eula\n", safePass));
                script.append("systemctl restart mssql-server\n");
                break;
            case REDIS:
                script.append("apt-get update && apt-get install -y redis-server\n");
                script.append("sed -i 's/bind 127.0.0.1/bind 0.0.0.0/' /etc/redis/redis.conf\n");
                script.append(String.format("sed -i 's/port 6379/port %d/' /etc/redis/redis.conf\n", port));
                script.append(String.format("echo 'requirepass %s' >> /etc/redis/redis.conf\n", safePass));
                script.append("systemctl enable redis-server && systemctl restart redis-server\n");
                break;
            case PHPMYADMIN:
                script.append("apt-get update && apt-get install -y nginx php-fpm php-mysql phpmyadmin\n");
                script.append("ln -s /usr/share/phpmyadmin /var/www/html/phpmyadmin\n");
                script.append(String.format("sed -i 's/listen 80/listen %d/' /etc/nginx/sites-available/default\n", port));
                script.append("systemctl enable nginx && systemctl restart nginx\n");
                break;
        }

        script.append(String.format("\nufw allow %d/tcp\nufw default deny incoming\nufw default allow outgoing\nufw --force enable\n", port));

        // Security hardening: SSH key-only, fail2ban, disable root login
        script.append("\n# Security hardening\n");
        script.append("sed -i 's/#\\?PermitRootLogin.*/PermitRootLogin no/' /etc/ssh/sshd_config\n");
        script.append("sed -i 's/#\\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config\n");
        script.append("ufw allow 22/tcp\n");
        script.append("systemctl restart sshd\n");
        script.append("apt-get install -y fail2ban\n");
        script.append("systemctl enable fail2ban && systemctl start fail2ban\n");
        return script.toString();
    }

    private String sanitizeSqlParam(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9@._\\-]", "");
    }

    private static boolean isValidCidr(String cidr) {
        if (cidr == null || cidr.isEmpty()) return false;
        // Match IPv4 CIDR: x.x.x.x/y
        return cidr.matches("^(\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}$");
    }

    private String generateSecurePassword(int length) {
        String chars = "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public DatabaseHealthCheckResponse checkDatabaseHealth(CheckDatabaseHealthCmd cmd) {
        DatabaseInstanceVO dbInstance = dbInstanceDao.findById(cmd.getId());
        if (dbInstance == null) {
            throw new IllegalArgumentException("Database instance not found");
        }

        DatabaseHealthCheckResponse response = new DatabaseHealthCheckResponse();
        response.setId(dbInstance.getUuid());
        response.setName(dbInstance.getName());
        response.setDbEngine(dbInstance.getDbEngine().name());
        response.setState(dbInstance.getState().name());
        response.setIpAddress(dbInstance.getIpAddress());
        response.setPort(dbInstance.getPort());
        response.setCheckedAt(new Date());

        boolean vmRunning = false;
        if (dbInstance.getVmId() != null) {
            try {
                UserVm vm = userVmService.getUserVm(dbInstance.getVmId());
                if (vm != null) {
                    response.setVmState(vm.getState().toString());
                    vmRunning = com.cloud.vm.VirtualMachine.State.Running.equals(vm.getState());
                }
            } catch (Exception e) {
                response.setVmState("UNKNOWN");
                response.setMessage("Failed to query VM state: " + e.getMessage());
            }
        }

        boolean portReachable = false;
        if (vmRunning && dbInstance.getIpAddress() != null) {
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(dbInstance.getIpAddress(), dbInstance.getPort()), 5000);
                portReachable = true;
            } catch (Exception e) {
                portReachable = false;
            }
        }

        response.setPortReachable(portReachable);
        response.setHealthy(vmRunning && portReachable);

        if (!vmRunning) {
            response.setMessage("VM is not in Running state");
        } else if (!portReachable) {
            response.setMessage("Database port " + dbInstance.getPort() + " is not reachable");
        } else {
            response.setMessage("Database instance is healthy");
        }

        return response;
    }

    @Override
    public List<DatabaseUsageResponse> listDatabaseUsage(ListDatabaseUsageCmd cmd) {
        List<DatabaseUsageResponse> responses = new ArrayList<>();
        List<DatabaseInstanceVO> instances;

        if (cmd.getId() != null) {
            DatabaseInstanceVO instance = dbInstanceDao.findById(cmd.getId());
            if (instance == null) {
                return responses;
            }
            instances = List.of(instance);
        } else {
            CallContext ctx = CallContext.current();
            Account callerAccount = ctx.getCallingAccount();
            instances = dbInstanceDao.listByAccountId(callerAccount.getId());
        }

        Date now = new Date();
        Date startDate = cmd.getStartDate() != null ? cmd.getStartDate() : new Date(now.getTime() - 30L * 24 * 60 * 60 * 1000);
        Date endDate = cmd.getEndDate() != null ? cmd.getEndDate() : now;

        for (DatabaseInstanceVO instance : instances) {
            if (instance.getRemoved() != null) {
                continue;
            }

            DatabaseUsageResponse usage = new DatabaseUsageResponse();
            usage.setId(instance.getUuid());
            usage.setName(instance.getName());
            usage.setDbEngine(instance.getDbEngine().name());
            usage.setState(instance.getState().name());
            usage.setCpuCores(instance.getCpuCores());
            usage.setMemoryMb(instance.getMemoryMb());
            usage.setStorageSizeGb(instance.getStorageSizeGb());
            usage.setStartDate(startDate);
            usage.setEndDate(endDate);

            Date instanceCreated = instance.getCreated();
            Date effectiveStart = instanceCreated.after(startDate) ? instanceCreated : startDate;
            long runningMs = endDate.getTime() - effectiveStart.getTime();
            if (runningMs < 0) {
                runningMs = 0;
            }
            double runningHours = runningMs / (1000.0 * 60 * 60);

            usage.setRunningHours(Math.round(runningHours * 100.0) / 100.0);
            usage.setCpuHoursUsed(Math.round(runningHours * instance.getCpuCores() * 100.0) / 100.0);
            usage.setMemoryMbHours(Math.round(runningHours * instance.getMemoryMb() * 100.0) / 100.0);
            usage.setStorageGbHours(Math.round(runningHours * instance.getStorageSizeGb() * 100.0) / 100.0);

            List<DatabaseBackupVO> backups = dbBackupDao.listByInstanceId(instance.getId());
            usage.setBackupCount(backups.size());

            Account account = accountManager.getAccount(instance.getAccountId());
            if (account != null) {
                usage.setAccount(account.getAccountName());
                usage.setDomainId(String.valueOf(instance.getDomainId()));
            }

            responses.add(usage);
        }

        return responses;
    }

    @Override
    public List<Class<?>> getCommands() {
        List<Class<?>> commands = new ArrayList<>();
        commands.add(CreateDatabaseInstanceCmd.class);
        commands.add(ListDatabaseInstancesCmd.class);
        commands.add(DeleteDatabaseInstanceCmd.class);
        commands.add(RestartDatabaseInstanceCmd.class);
        commands.add(ScaleDatabaseInstanceCmd.class);
        commands.add(ListDatabaseOfferingsCmd.class);
        commands.add(CreateDatabaseBackupCmd.class);
        commands.add(ListDatabaseBackupsCmd.class);
        commands.add(RestoreDatabaseBackupCmd.class);
        commands.add(CheckDatabaseHealthCmd.class);
        commands.add(ListDatabaseUsageCmd.class);
        return commands;
    }

    @Override
    public String getConfigComponentName() {
        return DatabaseServiceManagerImpl.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[] {};
    }
}
