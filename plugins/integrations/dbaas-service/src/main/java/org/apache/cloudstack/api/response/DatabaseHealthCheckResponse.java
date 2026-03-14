// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class DatabaseHealthCheckResponse extends BaseResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("dbengine")
    private String dbEngine;

    @SerializedName("state")
    private String state;

    @SerializedName("vmstate")
    private String vmState;

    @SerializedName("healthy")
    private boolean healthy;

    @SerializedName("portreachable")
    private boolean portReachable;

    @SerializedName("ipaddress")
    private String ipAddress;

    @SerializedName("port")
    private int port;

    @SerializedName("uptimeseconds")
    private long uptimeSeconds;

    @SerializedName("cpuusagepercent")
    private double cpuUsagePercent;

    @SerializedName("memoryusagepercent")
    private double memoryUsagePercent;

    @SerializedName("diskusagepercent")
    private double diskUsagePercent;

    @SerializedName("connectioncount")
    private int connectionCount;

    @SerializedName("lastrestarted")
    private Date lastRestarted;

    @SerializedName("checkedat")
    private Date checkedAt;

    @SerializedName("message")
    private String message;

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public void setDbEngine(String dbEngine) { this.dbEngine = dbEngine; }
    public String getDbEngine() { return dbEngine; }
    public void setState(String state) { this.state = state; }
    public String getState() { return state; }
    public void setVmState(String vmState) { this.vmState = vmState; }
    public String getVmState() { return vmState; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }
    public boolean isHealthy() { return healthy; }
    public void setPortReachable(boolean portReachable) { this.portReachable = portReachable; }
    public boolean isPortReachable() { return portReachable; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getIpAddress() { return ipAddress; }
    public void setPort(int port) { this.port = port; }
    public int getPort() { return port; }
    public void setUptimeSeconds(long uptimeSeconds) { this.uptimeSeconds = uptimeSeconds; }
    public long getUptimeSeconds() { return uptimeSeconds; }
    public void setCpuUsagePercent(double cpuUsagePercent) { this.cpuUsagePercent = cpuUsagePercent; }
    public double getCpuUsagePercent() { return cpuUsagePercent; }
    public void setMemoryUsagePercent(double memoryUsagePercent) { this.memoryUsagePercent = memoryUsagePercent; }
    public double getMemoryUsagePercent() { return memoryUsagePercent; }
    public void setDiskUsagePercent(double diskUsagePercent) { this.diskUsagePercent = diskUsagePercent; }
    public double getDiskUsagePercent() { return diskUsagePercent; }
    public void setConnectionCount(int connectionCount) { this.connectionCount = connectionCount; }
    public int getConnectionCount() { return connectionCount; }
    public void setLastRestarted(Date lastRestarted) { this.lastRestarted = lastRestarted; }
    public Date getLastRestarted() { return lastRestarted; }
    public void setCheckedAt(Date checkedAt) { this.checkedAt = checkedAt; }
    public Date getCheckedAt() { return checkedAt; }
    public void setMessage(String message) { this.message = message; }
    public String getMessage() { return message; }
}
