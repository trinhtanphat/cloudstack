// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas.dao;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "dbaas_instances")
public class DatabaseInstanceVO {

    public enum DbEngine {
        MYSQL, POSTGRESQL, MONGODB, SQLSERVER, REDIS, PHPMYADMIN
    }

    public enum State {
        CREATING, RUNNING, STOPPED, SCALING, BACKING_UP, RESTORING, ERROR, DESTROYED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "db_engine")
    @Enumerated(EnumType.STRING)
    private DbEngine dbEngine;

    @Column(name = "db_version")
    private String dbVersion;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "vm_id")
    private Long vmId;

    @Column(name = "service_offering_id")
    private long serviceOfferingId;

    @Column(name = "template_id")
    private long templateId;

    @Column(name = "network_id")
    private Long networkId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "public_ip_address")
    private String publicIpAddress;

    @Column(name = "public_ip_id")
    private Long publicIpId;

    @Column(name = "public_port")
    private Integer publicPort;

    @Column(name = "port")
    private int port;

    @Column(name = "admin_username")
    private String adminUsername;

    @Column(name = "admin_password_encrypted")
    private String adminPasswordEncrypted;

    @Column(name = "storage_size_gb")
    private int storageSizeGb;

    @Column(name = "cpu_cores")
    private int cpuCores;

    @Column(name = "memory_mb")
    private int memoryMb;

    @Column(name = "backup_enabled")
    private boolean backupEnabled;

    @Column(name = "backup_schedule")
    private String backupSchedule;

    @Column(name = "high_availability")
    private boolean highAvailability;

    @Column(name = "connection_string", length = 1024)
    private String connectionString;

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "removed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;

    public DatabaseInstanceVO() {
        this.uuid = UUID.randomUUID().toString();
        this.state = State.CREATING;
        this.created = new Date();
    }

    // Getters and Setters
    public long getId() { return id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }
    public long getDomainId() { return domainId; }
    public void setDomainId(long domainId) { this.domainId = domainId; }
    public DbEngine getDbEngine() { return dbEngine; }
    public void setDbEngine(DbEngine dbEngine) { this.dbEngine = dbEngine; }
    public String getDbVersion() { return dbVersion; }
    public void setDbVersion(String dbVersion) { this.dbVersion = dbVersion; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public long getZoneId() { return zoneId; }
    public void setZoneId(long zoneId) { this.zoneId = zoneId; }
    public Long getVmId() { return vmId; }
    public void setVmId(Long vmId) { this.vmId = vmId; }
    public long getServiceOfferingId() { return serviceOfferingId; }
    public void setServiceOfferingId(long serviceOfferingId) { this.serviceOfferingId = serviceOfferingId; }
    public long getTemplateId() { return templateId; }
    public void setTemplateId(long templateId) { this.templateId = templateId; }
    public Long getNetworkId() { return networkId; }
    public void setNetworkId(Long networkId) { this.networkId = networkId; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getPublicIpAddress() { return publicIpAddress; }
    public void setPublicIpAddress(String publicIpAddress) { this.publicIpAddress = publicIpAddress; }
    public Long getPublicIpId() { return publicIpId; }
    public void setPublicIpId(Long publicIpId) { this.publicIpId = publicIpId; }
    public Integer getPublicPort() { return publicPort; }
    public void setPublicPort(Integer publicPort) { this.publicPort = publicPort; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getAdminPasswordEncrypted() { return adminPasswordEncrypted; }
    public void setAdminPasswordEncrypted(String pw) { this.adminPasswordEncrypted = pw; }
    public int getStorageSizeGb() { return storageSizeGb; }
    public void setStorageSizeGb(int storageSizeGb) { this.storageSizeGb = storageSizeGb; }
    public int getCpuCores() { return cpuCores; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }
    public int getMemoryMb() { return memoryMb; }
    public void setMemoryMb(int memoryMb) { this.memoryMb = memoryMb; }
    public boolean isBackupEnabled() { return backupEnabled; }
    public void setBackupEnabled(boolean backupEnabled) { this.backupEnabled = backupEnabled; }
    public String getBackupSchedule() { return backupSchedule; }
    public void setBackupSchedule(String backupSchedule) { this.backupSchedule = backupSchedule; }
    public boolean isHighAvailability() { return highAvailability; }
    public void setHighAvailability(boolean highAvailability) { this.highAvailability = highAvailability; }
    public String getConnectionString() { return connectionString; }
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }
    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }
    public Date getRemoved() { return removed; }
    public void setRemoved(Date removed) { this.removed = removed; }
}
