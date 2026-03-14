// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.catalog;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.apache.cloudstack.api.command.user.dbaas.CreateDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.catalog.DeployCatalogItemCmd;
import org.apache.cloudstack.api.command.user.catalog.ListCatalogDeploymentStatusCmd;
import org.apache.cloudstack.api.command.user.catalog.ListCatalogItemsCmd;
import org.apache.cloudstack.api.response.CatalogDeploymentInstanceStatusResponse;
import org.apache.cloudstack.api.response.CatalogDeploymentResponse;
import org.apache.cloudstack.api.response.CatalogDeploymentStatusResponse;
import org.apache.cloudstack.api.response.CatalogItemResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.ConfigKey;

import com.cloud.dc.DataCenter;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dbaas.DatabaseAsAService;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.network.NetworkService;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.RulesService;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.uservm.UserVm;
import com.cloud.utils.net.Ip;
import com.cloud.vm.UserVmService;
import com.cloud.utils.component.ManagerBase;

/**
 * ServiceCatalogManagerImpl - Provides a curated marketplace of deployable services.
 *
 * The catalog is organized by categories:
 * - DATABASE: MySQL, PostgreSQL, MongoDB, SQL Server, Redis
 * - WEBSERVER: Nginx, Apache HTTP Server
 * - APPSTACK: LAMP Stack, MEAN Stack, WordPress, Node.js
 * - CACHE: Redis, Memcached
 * - MONITORING: Grafana, Prometheus, Zabbix
 * - MANAGEMENT: phpMyAdmin, Portainer (Docker), pgAdmin
 *
 * Each item can be deployed with one API call. The system:
 * 1. Creates a VM from appropriate template
 * 2. Injects cloud-init script to install & configure the service
 * 3. Optionally assigns public IP
 * 4. Returns connection details
 */
public class ServiceCatalogManagerImpl extends ManagerBase implements ServiceCatalogService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int PORT_RANGE_MIN = 10000;
    private static final int PORT_RANGE_MAX = 65000;
    private static final String IP_MODE_STATIC_NAT = "STATIC_NAT";
    private static final String IP_MODE_PORT_FORWARD = "PORT_FORWARD";
    private static final String DEFAULT_ALLOWED_CIDR = "0.0.0.0/0";
    private static final ConfigKey<Integer> ServiceCatalogExecutorPoolSize = new ConfigKey<>("Advanced", Integer.class,
        "service.catalog.executor.pool.size", "5",
        "Thread pool size for service catalog batch deployments.", true);
    private static final ConfigKey<Long> ServiceCatalogDeployTimeoutSeconds = new ConfigKey<>("Advanced", Long.class,
        "service.catalog.deploy.timeout.seconds", "1800",
        "Maximum runtime in seconds for one catalog batch deployment job.", true);
    private static final Set<Integer> globalUsedPorts = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, BatchDeploymentStatus> batchStatusStore = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, ServiceCatalogExecutorPoolSize.value()));

    @Inject private AccountManager accountManager;
    @Inject private DatabaseAsAService databaseService;
    @Inject private UserVmService userVmService;
    @Inject private DataCenterDao dataCenterDao;
    @Inject private ServiceOfferingDao serviceOfferingDao;
    @Inject private VMTemplateDao templateDao;
    @Inject private NetworkService networkService;
    @Inject private RulesService rulesService;

    @Override
    public List<CatalogItemResponse> listCatalogItems(ListCatalogItemsCmd cmd) {
        List<CatalogItemDefinition> definitions = buildCatalogDefinitions();
        List<CatalogItemResponse> catalog = new ArrayList<>();
        for (CatalogItemDefinition definition : definitions) {
            catalog.add(definition.toResponse());
        }

        // Filter by category
        if (cmd.getCategory() != null) {
            String category = cmd.getCategory().toUpperCase();
            catalog.removeIf(item -> !category.equals(item.getCategory()));
        }

        // Filter by keyword
        if (cmd.getKeyword() != null) {
            String kw = cmd.getKeyword().toLowerCase();
            definitions.removeIf(item -> !item.matchesKeyword(kw));
            catalog.clear();
            for (CatalogItemDefinition definition : definitions) {
                catalog.add(definition.toResponse());
            }
        }

        return catalog;
    }

    @Override
    public CatalogDeploymentResponse deployCatalogItem(DeployCatalogItemCmd cmd) {
        CallContext ctx = CallContext.current();
        Account caller = ctx.getCallingAccount();

        if (cmd.getCount() < 1 || cmd.getCount() > 1000) {
            throw new IllegalArgumentException("count must be between 1 and 1000");
        }
        if (!IP_MODE_STATIC_NAT.equals(cmd.getIpMode()) && !IP_MODE_PORT_FORWARD.equals(cmd.getIpMode())) {
            throw new IllegalArgumentException("ipmode must be STATIC_NAT or PORT_FORWARD");
        }
        if (!isValidCidr(cmd.getAllowedCidr())) {
            throw new IllegalArgumentException("Invalid CIDR format: " + cmd.getAllowedCidr());
        }

        CatalogItemDefinition definition = findCatalogDefinition(cmd.getCatalogItemId());
        if (definition == null) {
            throw new IllegalArgumentException("Catalog item not found: " + cmd.getCatalogItemId());
        }

        CatalogDeploymentResponse response = new CatalogDeploymentResponse();
        response.setId(UUID.randomUUID().toString());
        response.setCatalogItemId(definition.id);
        response.setCatalogItemName(definition.name);
        response.setCreated(new Date());

        logger.info("Catalog deployment initiated: " + definition.id + " (" + cmd.getName() +
            ") by account " + caller.getAccountName() +
            (cmd.getCount() > 1 ? " x" + cmd.getCount() + " instances" : ""));

        if (isDatabaseCatalogItem(definition)) {
            for (int index = 1; index <= cmd.getCount(); index++) {
                String instanceName = cmd.getCount() == 1 ? cmd.getName() : String.format("%s-%04d", cmd.getName(), index);
                deployDatabaseCatalogItem(definition, cmd, instanceName);
            }
            response.setStatus("DEPLOYING");
            return response;
        }

        if (cmd.getCount() == 1) {
            populateFromGenericDeployment(response, deployGenericCatalogItem(definition, cmd, cmd.getName(), caller, null, null));
            response.setStatus("DEPLOYED");
            return response;
        }

        String batchOperationId = UUID.randomUUID().toString();
        response.setBatchOperationId(batchOperationId);
        BatchDeploymentStatus batchStatus = initializeBatchStatus(batchOperationId, definition, cmd);
        deployBatch(definition, cmd, caller, batchOperationId);
        batchStatus.state = batchStatus.failedCount > 0 ? "PARTIAL" : "COMPLETED";
        batchStatus.updated = new Date();
        response.setStatus("DEPLOYED");

        return response;
    }

    @Override
    public CatalogDeploymentStatusResponse listCatalogDeploymentStatus(ListCatalogDeploymentStatusCmd cmd) {
        BatchDeploymentStatus status = batchStatusStore.get(cmd.getBatchOperationId());
        if (status == null) {
            throw new IllegalArgumentException("Catalog batch operation not found: " + cmd.getBatchOperationId());
        }

        CatalogDeploymentStatusResponse response = new CatalogDeploymentStatusResponse();
        response.setBatchOperationId(status.batchOperationId);
        response.setCatalogItemId(status.catalogItemId);
        response.setCatalogItemName(status.catalogItemName);
        response.setState(status.state);
        response.setTotalCount(status.totalCount);
        response.setCompletedCount(status.completedCount);
        response.setFailedCount(status.failedCount);
        response.setCreated(status.created);
        response.setUpdated(status.updated);

        List<CatalogDeploymentInstanceStatusResponse> instances = new ArrayList<>();
        for (InstanceDeploymentStatus item : status.instances) {
            CatalogDeploymentInstanceStatusResponse detail = new CatalogDeploymentInstanceStatusResponse();
            detail.setIndex(item.index);
            detail.setName(item.name);
            detail.setStatus(item.status);
            detail.setVmId(item.vmId);
            detail.setPrivateIp(item.privateIp);
            detail.setPublicIp(item.publicIp);
            detail.setPublicPort(item.publicPort);
            detail.setError(item.error);
            detail.setUpdated(item.updated);
            instances.add(detail);
        }
        response.setInstances(instances);
        return response;
    }

    private void deployBatch(CatalogItemDefinition definition, DeployCatalogItemCmd cmd, Account caller, String batchOperationId) {
        BatchDeploymentStatus batchStatus = batchStatusStore.get(batchOperationId);
        IpAddress sharedIp = null;
        Set<Integer> usedPorts = null;
        List<Future<?>> futures = new ArrayList<>();
        long deployTimeoutSeconds = Math.max(30L, ServiceCatalogDeployTimeoutSeconds.value());
        long deadlineMillis = System.currentTimeMillis() + deployTimeoutSeconds * 1000L;
        if (!isDatabaseCatalogItem(definition) && cmd.getAssignPublicIp() && IP_MODE_PORT_FORWARD.equals(cmd.getIpMode())) {
            sharedIp = allocatePublicIp(caller, cmd.getZoneId(), cmd.getNetworkId());
            usedPorts = Collections.synchronizedSet(new HashSet<>());
        }

        for (int index = 1; index <= cmd.getCount(); index++) {
            final String instanceName = String.format("%s-%04d", cmd.getName(), index);
            final int instanceIndex = index;
            final IpAddress finalSharedIp = sharedIp;
            final Set<Integer> finalUsedPorts = usedPorts;
            futures.add(executor.submit(() -> {
                InstanceDeploymentStatus item = getInstanceStatus(batchStatus, instanceIndex, instanceName);
                item.status = "RUNNING";
                item.updated = new Date();
                try {
                    GenericDeploymentResult result = deployGenericCatalogItem(definition, cmd, instanceName, caller, finalSharedIp, finalUsedPorts);
                    item.vmId = String.valueOf(result.vmId);
                    item.privateIp = result.ipAddress;
                    item.publicIp = result.publicIpAddress;
                    item.publicPort = result.publicPort;
                    item.status = "COMPLETED";
                    item.updated = new Date();
                    synchronized (batchStatus) {
                        batchStatus.completedCount++;
                        batchStatus.updated = new Date();
                    }
                } catch (RuntimeException e) {
                    item.status = "FAILED";
                    item.error = e.getMessage();
                    item.updated = new Date();
                    synchronized (batchStatus) {
                        batchStatus.failedCount++;
                        batchStatus.updated = new Date();
                    }
                    throw e;
                }
            }));
        }

        for (Future<?> future : futures) {
            long remainingMillis = deadlineMillis - System.currentTimeMillis();
            if (remainingMillis <= 0) {
                throw new IllegalStateException("Catalog batch deployment timed out for operation " + batchOperationId +
                    " after " + deployTimeoutSeconds + " seconds");
            }
            try {
                future.get(remainingMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                batchStatus.state = "FAILED";
                throw new IllegalStateException("Catalog batch deployment interrupted for operation " + batchOperationId, e);
            } catch (ExecutionException e) {
                batchStatus.state = batchStatus.completedCount > 0 ? "PARTIAL" : "FAILED";
                throw new IllegalStateException("Catalog batch deployment failed for operation " + batchOperationId, e.getCause());
            } catch (TimeoutException e) {
                batchStatus.state = "FAILED";
                throw new IllegalStateException("Catalog batch deployment timed out for operation " + batchOperationId +
                    " after " + deployTimeoutSeconds + " seconds", e);
            }
        }
    }

    private static BatchDeploymentStatus initializeBatchStatus(String batchOperationId, CatalogItemDefinition definition,
                                                               DeployCatalogItemCmd cmd) {
        BatchDeploymentStatus status = new BatchDeploymentStatus();
        status.batchOperationId = batchOperationId;
        status.catalogItemId = definition.id;
        status.catalogItemName = definition.name;
        status.totalCount = cmd.getCount();
        status.completedCount = 0;
        status.failedCount = 0;
        status.state = "RUNNING";
        status.created = new Date();
        status.updated = status.created;
        for (int index = 1; index <= cmd.getCount(); index++) {
            InstanceDeploymentStatus item = new InstanceDeploymentStatus();
            item.index = index;
            item.name = String.format("%s-%04d", cmd.getName(), index);
            item.status = "PENDING";
            item.updated = status.created;
            status.instances.add(item);
        }
        batchStatusStore.put(batchOperationId, status);
        return status;
    }

    private static InstanceDeploymentStatus getInstanceStatus(BatchDeploymentStatus batchStatus, int index, String name) {
        if (batchStatus == null || index < 1 || index > batchStatus.instances.size()) {
            InstanceDeploymentStatus fallback = new InstanceDeploymentStatus();
            fallback.index = index;
            fallback.name = name;
            fallback.status = "UNKNOWN";
            fallback.updated = new Date();
            return fallback;
        }
        return batchStatus.instances.get(index - 1);
    }

    private void deployDatabaseCatalogItem(CatalogItemDefinition definition, DeployCatalogItemCmd cmd, String instanceName) {
        CreateDatabaseInstanceCmd databaseCmd = new ServiceCatalogDatabaseInstanceCmd(
            instanceName, mapCatalogItemToDbEngine(definition.id), cmd.getZoneId(),
            cmd.getServiceOfferingId(), cmd.getTemplateId(), cmd.getNetworkId(),
            definition.minStorageGb, cmd.getAssignPublicIp(), cmd.getIpMode(), cmd.getAllowedCidr());
        databaseService.createDatabaseInstance(databaseCmd);
    }

    private GenericDeploymentResult deployGenericCatalogItem(CatalogItemDefinition definition, DeployCatalogItemCmd cmd,
                                                             String instanceName, Account account, IpAddress sharedIp,
                                                             Set<Integer> usedPorts) {
        DataCenter zone = dataCenterDao.findById(cmd.getZoneId());
        if (zone == null) {
            throw new IllegalArgumentException("Invalid zone ID: " + cmd.getZoneId());
        }
        ServiceOffering offering = serviceOfferingDao.findById(cmd.getServiceOfferingId());
        if (offering == null) {
            throw new IllegalArgumentException("Invalid service offering ID: " + cmd.getServiceOfferingId());
        }
        VirtualMachineTemplate template = resolveTemplate(cmd.getTemplateId(), cmd.getZoneId());
        if (template == null) {
            throw new IllegalArgumentException("No suitable template found for catalog deployment");
        }

        List<Long> networkIds = cmd.getNetworkId() != null ? List.of(cmd.getNetworkId()) : new ArrayList<>();
        String userData = Base64.getEncoder().encodeToString(
            generateCloudInitScript(definition).getBytes(StandardCharsets.UTF_8));

        UserVm vm;
        try {
            vm = userVmService.createAdvancedVirtualMachine(
                zone, offering, template,
                networkIds, account,
                instanceName, instanceName,
                null, null, null,
                null, null, null,
                userData, null, null,
                null, null, null,
                null, null, null,
                null, null, null,
                null, null, true,
                null, null, null, null
            );
        } catch (InsufficientCapacityException | ResourceUnavailableException | ResourceAllocationException e) {
            throw new IllegalStateException("Insufficient capacity for catalog item " + definition.id, e);
        }

        if (vm == null) {
            throw new IllegalStateException("Failed to create VM for catalog item " + definition.id);
        }

        GenericDeploymentResult result = new GenericDeploymentResult();
        result.vmId = vm.getId();
        result.ipAddress = vm.getPrivateIpAddress();

        if (cmd.getAssignPublicIp()) {
            if (IP_MODE_PORT_FORWARD.equals(cmd.getIpMode())) {
                IpAddress ip = sharedIp != null ? sharedIp : allocatePublicIp(account, cmd.getZoneId(), cmd.getNetworkId());
                int[] privatePorts = parsePrivatePorts(cmd.getPrivatePorts(), definition.defaultPort);
                Integer firstPort = assignPortForwarding(ip, vm, cmd.getNetworkId(), account, privatePorts,
                    usedPorts != null ? usedPorts : Collections.synchronizedSet(new HashSet<>()), cmd.getAllowedCidr());
                result.publicIpAddress = ip.getAddress().addr();
                result.publicPort = firstPort;
            } else {
                IpAddress ip = allocatePublicIp(account, cmd.getZoneId(), cmd.getNetworkId());
                long networkId = cmd.getNetworkId() != null ? cmd.getNetworkId() : resolveVmNetworkId(networkIds);
                try {
                    rulesService.enableStaticNat(ip.getId(), vm.getId(), networkId, null);
                } catch (ResourceUnavailableException | NetworkRuleConflictException e) {
                    throw new IllegalStateException("Failed to enable static NAT for catalog item " + definition.id, e);
                }
                result.publicIpAddress = ip.getAddress().addr();
            }
        }

        result.accessUrl = buildAccessUrl(definition, result.publicIpAddress != null ? result.publicIpAddress : result.ipAddress,
            result.publicPort != null ? result.publicPort : definition.defaultPort);
        return result;
    }

    private IpAddress allocatePublicIp(Account account, Long zoneId, Long networkId) {
        IpAddress ip;
        try {
            ip = networkService.allocateIP(account, zoneId, networkId, null, null);
        } catch (ResourceAllocationException | InsufficientAddressCapacityException e) {
            throw new IllegalStateException("Failed to allocate public IP address", e);
        }
        if (ip == null) {
            throw new IllegalStateException("Failed to allocate public IP address");
        }
        return ip;
    }

    private Integer assignPortForwarding(IpAddress ip, UserVm vm, Long networkId, Account account,
                                         int[] privatePorts, Set<Integer> usedPorts, String allowedCidr) {
        if (networkId == null) {
            throw new IllegalArgumentException("networkId is required for PORT_FORWARD catalog deployments");
        }
        long effectiveNetworkId = networkId;
        Integer firstPort = null;
        for (int privatePort : privatePorts) {
            int publicPort = allocateRandomPort(usedPorts);
            if (firstPort == null) {
                firstPort = publicPort;
            }
            PortForwardingRuleAdapter adapter = new PortForwardingRuleAdapter(
                ip.getId(), publicPort, privatePort, "tcp", effectiveNetworkId,
                account.getId(), account.getDomainId(), vm.getId(), allowedCidr);
            try {
                rulesService.createPortForwardingRule(adapter, vm.getId(), null, true, true);
            } catch (NetworkRuleConflictException e) {
                usedPorts.remove(publicPort);
                throw new IllegalStateException("Failed to create port forwarding rule", e);
            }
        }
        try {
            rulesService.applyPortForwardingRules(ip.getId(), account);
        } catch (ResourceUnavailableException e) {
            throw new IllegalStateException("Failed to apply port forwarding rules", e);
        }
        return firstPort;
    }

    private void populateFromGenericDeployment(CatalogDeploymentResponse response, GenericDeploymentResult result) {
        response.setVmId(String.valueOf(result.vmId));
        response.setIpAddress(result.ipAddress);
        response.setPublicIpAddress(result.publicIpAddress);
        response.setAccessUrl(result.accessUrl);
    }

    private VirtualMachineTemplate resolveTemplate(Long templateId, long zoneId) {
        if (templateId != null) {
            return templateDao.findById(templateId);
        }
        List<VMTemplateVO> zoneTemplates = templateDao.listAllInZone(zoneId);
        if (zoneTemplates != null && !zoneTemplates.isEmpty()) {
            return zoneTemplates.get(0);
        }
        List<VMTemplateVO> publicTemplates = templateDao.listByPublic();
        return publicTemplates != null && !publicTemplates.isEmpty() ? publicTemplates.get(0) : null;
    }

    private static int[] parsePrivatePorts(String privatePorts, int defaultPort) {
        String value = privatePorts == null || privatePorts.isBlank() ? String.valueOf(defaultPort) : privatePorts;
        String[] parts = value.split(",");
        int[] parsed = new int[parts.length];
        for (int index = 0; index < parts.length; index++) {
            try {
                int port = Integer.parseInt(parts[index].trim());
                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException("Port out of range: " + port);
                }
                parsed[index] = port;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid private port value: " + parts[index], e);
            }
        }
        return parsed;
    }

    private static boolean isValidCidr(String cidr) {
        if (cidr == null || cidr.isBlank()) {
            return false;
        }
        return cidr.matches("^(\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}$");
    }

    private static int allocateRandomPort(Set<Integer> usedPorts) {
        int range = PORT_RANGE_MAX - PORT_RANGE_MIN;
        for (int attempt = 0; attempt < 100; attempt++) {
            int port = PORT_RANGE_MIN + secureRandom.nextInt(range);
            if (usedPorts.add(port) && globalUsedPorts.add(port)) {
                return port;
            }
        }
        for (int port = PORT_RANGE_MIN; port < PORT_RANGE_MAX; port++) {
            if (usedPorts.add(port) && globalUsedPorts.add(port)) {
                return port;
            }
        }
        throw new IllegalStateException("Port range exhausted");
    }

    private static String buildAccessUrl(CatalogItemDefinition definition, String host, int port) {
        if (host == null) {
            return null;
        }
        String scheme = definition.defaultPort == 9443 ? "https" : "http";
        if (definition.defaultPort == 80 && port == 80) {
            return scheme + "://" + host;
        }
        if (definition.defaultPort == 443 && port == 443) {
            return "https://" + host;
        }
        if ("WEBSERVER".equals(definition.category) || "APPSTACK".equals(definition.category) || "MONITORING".equals(definition.category) || "MANAGEMENT".equals(definition.category)) {
            return scheme + "://" + host + ":" + port;
        }
        return host + ":" + port;
    }

    private static long resolveVmNetworkId(List<Long> networkIds) {
        if (networkIds == null || networkIds.isEmpty()) {
            throw new IllegalArgumentException("networkId is required for public IP assignment in catalog deployment");
        }
        return networkIds.get(0);
    }

    private static boolean isDatabaseCatalogItem(CatalogItemDefinition definition) {
        return mapCatalogItemToDbEngine(definition.id) != null;
    }

    private static String mapCatalogItemToDbEngine(String catalogItemId) {
        Map<String, String> mapping = Map.of(
            "mysql-8.0", "MYSQL",
            "postgresql-16", "POSTGRESQL",
            "mongodb-7.0", "MONGODB",
            "sqlserver-2022", "SQLSERVER",
            "redis-7.2", "REDIS",
            "phpmyadmin-5.2", "PHPMYADMIN"
        );
        return mapping.get(catalogItemId);
    }

    private CatalogItemDefinition findCatalogDefinition(String id) {
        for (CatalogItemDefinition definition : buildCatalogDefinitions()) {
            if (definition.id.equalsIgnoreCase(id)) {
                return definition;
            }
        }
        return null;
    }

    private List<CatalogItemDefinition> buildCatalogDefinitions() {
        List<CatalogItemDefinition> catalog = new ArrayList<>();
        catalog.add(new CatalogItemDefinition("mysql-8.0", "MySQL 8.0", "DATABASE",
            "World's most popular open-source relational database. Ideal for web applications, CMS, and e-commerce.",
            "mysql", "8.0", 1, 1024, 10, 3306, true,
            List.of("InnoDB engine", "Replication support", "JSON support", "Full-text search", "SSL connections")));
        catalog.add(new CatalogItemDefinition("postgresql-16", "PostgreSQL 16", "DATABASE",
            "Most advanced open-source relational database. Perfect for GIS, analytics, and complex queries.",
            "postgresql", "16", 1, 1024, 10, 5432, true,
            List.of("JSONB support", "PostGIS extension", "Partitioning", "Parallel queries", "SSL connections")));
        catalog.add(new CatalogItemDefinition("mongodb-7.0", "MongoDB 7.0", "DATABASE",
            "Leading document-oriented NoSQL database. Great for flexible schemas and real-time analytics.",
            "mongodb", "7.0", 2, 2048, 20, 27017, true,
            List.of("Document model", "Aggregation pipeline", "Sharding", "Replica sets", "Atlas compatible")));
        catalog.add(new CatalogItemDefinition("sqlserver-2022", "SQL Server 2022 Express", "DATABASE",
            "Microsoft SQL Server for enterprise applications. Compatible with .NET and business intelligence tools.",
            "sqlserver", "2022", 2, 4096, 20, 1433, false,
            List.of("T-SQL support", "SSMS compatible", "Backup/Restore", "Express Edition (10GB limit)", "Windows auth")));
        catalog.add(new CatalogItemDefinition("redis-7.2", "Redis 7.2", "CACHE",
            "In-memory data structure store. Use as cache, message broker, or session store.",
            "redis", "7.2", 1, 512, 5, 6379, true,
            List.of("In-memory storage", "Pub/Sub messaging", "Lua scripting", "Cluster mode", "Persistence")));
        catalog.add(new CatalogItemDefinition("phpmyadmin-5.2", "phpMyAdmin 5.2", "MANAGEMENT",
            "Web-based MySQL/MariaDB administration tool. Manage databases through a browser interface.",
            "phpmyadmin", "5.2", 1, 512, 5, 8080, true,
            List.of("Web UI for MySQL", "Import/Export", "SQL editor", "User management", "Table designer")));
        catalog.add(new CatalogItemDefinition("pgadmin-4", "pgAdmin 4", "MANAGEMENT",
            "Web-based PostgreSQL administration and development tool.",
            "pgadmin", "4", 1, 512, 5, 5050, false,
            List.of("Web UI for PostgreSQL", "Query tool", "ERD designer", "Backup/Restore UI", "Dashboard")));
        catalog.add(new CatalogItemDefinition("portainer-ce", "Portainer CE", "MANAGEMENT",
            "Docker container management UI. Deploy and manage Docker containers easily.",
            "portainer", "2.19", 1, 512, 10, 9443, false,
            List.of("Docker management", "Container deploy", "Image registry", "Stack management", "Web terminal")));
        catalog.add(new CatalogItemDefinition("nginx-latest", "Nginx Web Server", "WEBSERVER",
            "High-performance web server and reverse proxy. Serves static content blazingly fast.",
            "nginx", "1.25", 1, 512, 10, 80, true,
            List.of("Reverse proxy", "Load balancing", "SSL termination", "Static files", "HTTP/2 support")));
        catalog.add(new CatalogItemDefinition("apache-httpd", "Apache HTTP Server", "WEBSERVER",
            "The world's most used web server. Comprehensive feature set with modules ecosystem.",
            "apache", "2.4", 1, 512, 10, 80, false,
            List.of("mod_rewrite", "Virtual hosts", "SSL/TLS", ".htaccess support", "Module ecosystem")));
        catalog.add(new CatalogItemDefinition("lamp-stack", "LAMP Stack", "APPSTACK",
            "Linux + Apache + MySQL + PHP. The classic web application stack.",
            "lamp", "8.2", 2, 2048, 20, 80, true,
            List.of("Apache 2.4", "MySQL 8.0", "PHP 8.2", "phpMyAdmin", "SSL ready")));
        catalog.add(new CatalogItemDefinition("mean-stack", "MEAN Stack", "APPSTACK",
            "MongoDB + Express.js + Angular + Node.js. Full JavaScript application stack.",
            "mean", "18", 2, 2048, 20, 3000, false,
            List.of("MongoDB 7.0", "Express.js", "Angular 17", "Node.js 18 LTS", "PM2 process manager")));
        catalog.add(new CatalogItemDefinition("wordpress", "WordPress", "APPSTACK",
            "World's most popular CMS. Pre-configured with MySQL, Nginx, and PHP.",
            "wordpress", "6.4", 2, 2048, 20, 80, true,
            List.of("Nginx + PHP-FPM", "MySQL 8.0", "WP-CLI", "Auto-updates", "SSL with Let's Encrypt")));
        catalog.add(new CatalogItemDefinition("nodejs-app", "Node.js Application Server", "APPSTACK",
            "Node.js runtime with PM2 process manager. Ready for Express/Fastify/Nest.js applications.",
            "nodejs", "20 LTS", 1, 1024, 10, 3000, false,
            List.of("Node.js 20 LTS", "PM2", "Nginx reverse proxy", "npm/yarn", "Git deploy hook")));
        catalog.add(new CatalogItemDefinition("grafana-oss", "Grafana OSS", "MONITORING",
            "Open-source analytics and monitoring dashboard. Visualize metrics from any data source.",
            "grafana", "10.2", 1, 1024, 10, 3000, false,
            List.of("Dashboard builder", "Alerting", "Prometheus integration", "Multiple data sources", "Plugins")));
        catalog.add(new CatalogItemDefinition("prometheus", "Prometheus", "MONITORING",
            "Open-source monitoring and alerting toolkit. Collect metrics with powerful query language.",
            "prometheus", "2.48", 1, 1024, 20, 9090, false,
            List.of("PromQL queries", "Service discovery", "Alert manager", "Time-series DB", "Grafana compatible")));
        catalog.add(new CatalogItemDefinition("zabbix-server", "Zabbix Server", "MONITORING",
            "Enterprise-class monitoring for networks, servers, applications, and cloud services.",
            "zabbix", "6.4", 2, 2048, 20, 8080, false,
            List.of("Auto-discovery", "SNMP monitoring", "Distributed monitoring", "Web dashboard", "SLA tracking")));
        return catalog;
    }

    private String generateCloudInitScript(CatalogItemDefinition definition) {
        switch (definition.id) {
            case "nginx-latest":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y nginx\nsystemctl enable nginx\nsystemctl restart nginx\n";
            case "apache-httpd":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y apache2\nsystemctl enable apache2\nsystemctl restart apache2\n";
            case "lamp-stack":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y apache2 mysql-server php libapache2-mod-php php-mysql\nsystemctl enable apache2 mysql\nsystemctl restart apache2 mysql\n";
            case "mean-stack":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y curl gnupg nginx mongodb\ncurl -fsSL https://deb.nodesource.com/setup_18.x | bash -\napt-get install -y nodejs\nnpm install -g pm2\nsystemctl enable nginx mongod\nsystemctl restart nginx mongod\n";
            case "wordpress":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y nginx mysql-server php-fpm php-mysql wget tar\nsystemctl enable nginx mysql\nsystemctl restart nginx mysql\ncd /var/www/html\nwget -q https://wordpress.org/latest.tar.gz\ntar -xzf latest.tar.gz --strip-components=1\nchown -R www-data:www-data /var/www/html\n";
            case "nodejs-app":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y curl nginx\ncurl -fsSL https://deb.nodesource.com/setup_20.x | bash -\napt-get install -y nodejs\nnpm install -g pm2\nsystemctl enable nginx\nsystemctl restart nginx\n";
            case "pgadmin-4":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y curl gnupg apache2\ncurl -fsS https://www.pgadmin.org/static/packages_pgadmin_org.pub | gpg --dearmor -o /usr/share/keyrings/packages-pgadmin-org.gpg\necho 'deb [signed-by=/usr/share/keyrings/packages-pgadmin-org.gpg] https://ftp.postgresql.org/pub/pgadmin/pgadmin4/apt/$(lsb_release -cs) pgadmin4 main' > /etc/apt/sources.list.d/pgadmin4.list\napt-get update\napt-get install -y pgadmin4-web\n";
            case "portainer-ce":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y docker.io\nsystemctl enable docker\nsystemctl restart docker\ndocker volume create portainer_data\ndocker run -d -p 9443:9443 --name portainer --restart=always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer-ce:lts\n";
            case "grafana-oss":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y adduser libfontconfig1 musl wget\nwget -q https://dl.grafana.com/oss/release/grafana_10.2.0_amd64.deb -O /tmp/grafana.deb\ndpkg -i /tmp/grafana.deb || apt-get install -f -y\nsystemctl enable grafana-server\nsystemctl restart grafana-server\n";
            case "prometheus":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y prometheus\nsystemctl enable prometheus\nsystemctl restart prometheus\n";
            case "zabbix-server":
                return "#!/bin/bash\nset -e\napt-get update\napt-get install -y apache2 mysql-server php libapache2-mod-php\nsystemctl enable apache2 mysql\nsystemctl restart apache2 mysql\n";
            default:
                return "#!/bin/bash\nset -e\napt-get update\n";
        }
    }

    private static final class GenericDeploymentResult {
        private long vmId;
        private String ipAddress;
        private String publicIpAddress;
        private Integer publicPort;
        private String accessUrl;
    }

    private static final class CatalogItemDefinition {
        private final String id;
        private final String name;
        private final String category;
        private final String description;
        private final String icon;
        private final String version;
        private final int minCpu;
        private final int minMemoryMb;
        private final int minStorageGb;
        private final int defaultPort;
        private final boolean popular;
        private final List<String> features;

        private CatalogItemDefinition(String id, String name, String category, String description, String icon,
                                      String version, int minCpu, int minMemoryMb, int minStorageGb,
                                      int defaultPort, boolean popular, List<String> features) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.description = description;
            this.icon = icon;
            this.version = version;
            this.minCpu = minCpu;
            this.minMemoryMb = minMemoryMb;
            this.minStorageGb = minStorageGb;
            this.defaultPort = defaultPort;
            this.popular = popular;
            this.features = features;
        }

        private CatalogItemResponse toResponse() {
            CatalogItemResponse r = new CatalogItemResponse();
            r.setId(id);
            r.setName(name);
            r.setCategory(category);
            r.setDescription(description);
            r.setIcon(icon);
            r.setVersion(version);
            r.setMinCpu(minCpu);
            r.setMinMemoryMb(minMemoryMb);
            r.setMinStorageGb(minStorageGb);
            r.setDefaultPort(defaultPort);
            r.setPopular(popular);
            r.setFeatures(features);
            return r;
        }

        private boolean matchesKeyword(String keyword) {
            String searchable = String.join(" ", id, name, category, description, icon, version, String.join(" ", features)).toLowerCase();
            return searchable.contains(keyword);
        }
    }

    private static class PortForwardingRuleAdapter implements PortForwardingRule {
        private final long sourceIpAddressId;
        private final int publicPort;
        private final int privatePort;
        private final String protocol;
        private final long networkId;
        private final long accountId;
        private final long domainId;
        private final long virtualMachineId;
        private final String allowedCidr;

        private PortForwardingRuleAdapter(long sourceIpAddressId, int publicPort, int privatePort,
                                          String protocol, long networkId, long accountId,
                                          long domainId, long virtualMachineId, String allowedCidr) {
            this.sourceIpAddressId = sourceIpAddressId;
            this.publicPort = publicPort;
            this.privatePort = privatePort;
            this.protocol = protocol;
            this.networkId = networkId;
            this.accountId = accountId;
            this.domainId = domainId;
            this.virtualMachineId = virtualMachineId;
            this.allowedCidr = allowedCidr != null ? allowedCidr : DEFAULT_ALLOWED_CIDR;
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
        @Override public List<String> getSourceCidrList() { return List.of(allowedCidr); }
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

    private static final class ServiceCatalogDatabaseInstanceCmd extends CreateDatabaseInstanceCmd {
        private final String name;
        private final String dbEngine;
        private final Long zoneId;
        private final Long serviceOfferingId;
        private final Long templateId;
        private final Long networkId;
        private final Integer storageSizeGb;
        private final Boolean assignPublicIp;
        private final String ipMode;
        private final String allowedCidr;

        private ServiceCatalogDatabaseInstanceCmd(String name, String dbEngine, Long zoneId, Long serviceOfferingId,
                                                  Long templateId, Long networkId, Integer storageSizeGb,
                                                  Boolean assignPublicIp, String ipMode, String allowedCidr) {
            this.name = name;
            this.dbEngine = dbEngine;
            this.zoneId = zoneId;
            this.serviceOfferingId = serviceOfferingId;
            this.templateId = templateId;
            this.networkId = networkId;
            this.storageSizeGb = storageSizeGb;
            this.assignPublicIp = assignPublicIp;
            this.ipMode = ipMode;
            this.allowedCidr = allowedCidr;
        }

        @Override public String getName() { return name; }
        @Override public String getDbEngine() { return dbEngine; }
        @Override public Long getZoneId() { return zoneId; }
        @Override public Long getServiceOfferingId() { return serviceOfferingId; }
        @Override public Long getTemplateId() { return templateId; }
        @Override public Long getNetworkId() { return networkId; }
        @Override public Integer getStorageSizeGb() { return storageSizeGb; }
        @Override public Boolean getAssignPublicIp() { return assignPublicIp; }
        @Override public String getIpMode() { return ipMode; }
        @Override public String getAllowedCidr() { return allowedCidr; }
    }

    private static final class BatchDeploymentStatus {
        private String batchOperationId;
        private String catalogItemId;
        private String catalogItemName;
        private int totalCount;
        private int completedCount;
        private int failedCount;
        private String state;
        private Date created;
        private Date updated;
        private final List<InstanceDeploymentStatus> instances = new ArrayList<>();
    }

    private static final class InstanceDeploymentStatus {
        private int index;
        private String name;
        private String status;
        private String vmId;
        private String privateIp;
        private String publicIp;
        private Integer publicPort;
        private String error;
        private Date updated;
    }

    @Override
    public List<Class<?>> getCommands() {
        List<Class<?>> commands = new ArrayList<>();
        commands.add(ListCatalogItemsCmd.class);
        commands.add(DeployCatalogItemCmd.class);
        commands.add(ListCatalogDeploymentStatusCmd.class);
        return commands;
    }

    @Override
    public String getConfigComponentName() {
        return ServiceCatalogManagerImpl.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[] {
            ServiceCatalogExecutorPoolSize,
            ServiceCatalogDeployTimeoutSeconds
        };
    }
}
