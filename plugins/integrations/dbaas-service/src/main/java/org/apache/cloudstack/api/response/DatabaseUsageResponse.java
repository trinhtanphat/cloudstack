// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class DatabaseUsageResponse extends BaseResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("dbengine")
    private String dbEngine;

    @SerializedName("account")
    private String account;

    @SerializedName("domainid")
    private String domainId;

    @SerializedName("state")
    private String state;

    @SerializedName("cpucores")
    private int cpuCores;

    @SerializedName("memorymb")
    private int memoryMb;

    @SerializedName("storagesizegb")
    private int storageSizeGb;

    @SerializedName("runninghours")
    private double runningHours;

    @SerializedName("cpuhoursused")
    private double cpuHoursUsed;

    @SerializedName("memorymbhours")
    private double memoryMbHours;

    @SerializedName("storagegbhours")
    private double storageGbHours;

    @SerializedName("backupcount")
    private int backupCount;

    @SerializedName("backupsizegb")
    private double backupSizeGb;

    @SerializedName("networkbytesreceived")
    private long networkBytesReceived;

    @SerializedName("networkbytessent")
    private long networkBytesSent;

    @SerializedName("startdate")
    private Date startDate;

    @SerializedName("enddate")
    private Date endDate;

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public void setDbEngine(String dbEngine) { this.dbEngine = dbEngine; }
    public String getDbEngine() { return dbEngine; }
    public void setAccount(String account) { this.account = account; }
    public String getAccount() { return account; }
    public void setDomainId(String domainId) { this.domainId = domainId; }
    public String getDomainId() { return domainId; }
    public void setState(String state) { this.state = state; }
    public String getState() { return state; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }
    public int getCpuCores() { return cpuCores; }
    public void setMemoryMb(int memoryMb) { this.memoryMb = memoryMb; }
    public int getMemoryMb() { return memoryMb; }
    public void setStorageSizeGb(int storageSizeGb) { this.storageSizeGb = storageSizeGb; }
    public int getStorageSizeGb() { return storageSizeGb; }
    public void setRunningHours(double runningHours) { this.runningHours = runningHours; }
    public double getRunningHours() { return runningHours; }
    public void setCpuHoursUsed(double cpuHoursUsed) { this.cpuHoursUsed = cpuHoursUsed; }
    public double getCpuHoursUsed() { return cpuHoursUsed; }
    public void setMemoryMbHours(double memoryMbHours) { this.memoryMbHours = memoryMbHours; }
    public double getMemoryMbHours() { return memoryMbHours; }
    public void setStorageGbHours(double storageGbHours) { this.storageGbHours = storageGbHours; }
    public double getStorageGbHours() { return storageGbHours; }
    public void setBackupCount(int backupCount) { this.backupCount = backupCount; }
    public int getBackupCount() { return backupCount; }
    public void setBackupSizeGb(double backupSizeGb) { this.backupSizeGb = backupSizeGb; }
    public double getBackupSizeGb() { return backupSizeGb; }
    public void setNetworkBytesReceived(long networkBytesReceived) { this.networkBytesReceived = networkBytesReceived; }
    public long getNetworkBytesReceived() { return networkBytesReceived; }
    public void setNetworkBytesSent(long networkBytesSent) { this.networkBytesSent = networkBytesSent; }
    public long getNetworkBytesSent() { return networkBytesSent; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getStartDate() { return startDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Date getEndDate() { return endDate; }
}
