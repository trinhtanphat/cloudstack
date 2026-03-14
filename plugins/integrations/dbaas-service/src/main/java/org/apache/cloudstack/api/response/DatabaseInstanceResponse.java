// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class DatabaseInstanceResponse extends BaseResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("dbengine")
    private String dbEngine;

    @SerializedName("dbversion")
    private String dbVersion;

    @SerializedName("state")
    private String state;

    @SerializedName("zoneid")
    private String zoneId;

    @SerializedName("zonename")
    private String zoneName;

    @SerializedName("vmid")
    private String vmId;

    @SerializedName("ipaddress")
    private String ipAddress;

    @SerializedName("publicipaddress")
    private String publicIpAddress;

    @SerializedName("port")
    private int port;

    @SerializedName("publicport")
    private Integer publicPort;

    @SerializedName("adminusername")
    private String adminUsername;

    @SerializedName("connectionstring")
    private String connectionString;

    @SerializedName("storagesizegb")
    private int storageSizeGb;

    @SerializedName("cpucores")
    private int cpuCores;

    @SerializedName("memorymb")
    private int memoryMb;

    @SerializedName("backupenabled")
    private boolean backupEnabled;

    @SerializedName("highavailability")
    private boolean highAvailability;

    @SerializedName("created")
    private Date created;

    @SerializedName("account")
    private String account;

    @SerializedName("domainid")
    private String domainId;

    // All setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDbEngine(String dbEngine) { this.dbEngine = dbEngine; }
    public void setDbVersion(String dbVersion) { this.dbVersion = dbVersion; }
    public void setState(String state) { this.state = state; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public void setVmId(String vmId) { this.vmId = vmId; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setPublicIpAddress(String publicIpAddress) { this.publicIpAddress = publicIpAddress; }
    public void setPort(int port) { this.port = port; }
    public void setPublicPort(Integer publicPort) { this.publicPort = publicPort; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }
    public void setStorageSizeGb(int storageSizeGb) { this.storageSizeGb = storageSizeGb; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }
    public void setMemoryMb(int memoryMb) { this.memoryMb = memoryMb; }
    public void setBackupEnabled(boolean backupEnabled) { this.backupEnabled = backupEnabled; }
    public void setHighAvailability(boolean highAvailability) { this.highAvailability = highAvailability; }
    public void setCreated(Date created) { this.created = created; }
    public void setAccount(String account) { this.account = account; }
    public void setDomainId(String domainId) { this.domainId = domainId; }
}
