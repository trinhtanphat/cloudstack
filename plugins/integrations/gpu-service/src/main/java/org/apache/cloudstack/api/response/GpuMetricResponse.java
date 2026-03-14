// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class GpuMetricResponse extends BaseResponse {

    @SerializedName("gpuinstanceid")
    private String gpuInstanceId;

    @SerializedName("name")
    private String name;

    @SerializedName("provider")
    private String provider;

    @SerializedName("gpuprofileid")
    private String gpuProfileId;

    @SerializedName("gpuutilization")
    private double gpuUtilization;

    @SerializedName("memoryutilization")
    private double memoryUtilization;

    @SerializedName("memorytotalmb")
    private long memoryTotalMb;

    @SerializedName("powerwatts")
    private double powerWatts;

    @SerializedName("temperaturec")
    private double temperatureC;

    @SerializedName("timestamp")
    private Date timestamp;

    public void setGpuInstanceId(String gpuInstanceId) { this.gpuInstanceId = gpuInstanceId; }
    public void setName(String name) { this.name = name; }
    public void setProvider(String provider) { this.provider = provider; }
    public void setGpuProfileId(String gpuProfileId) { this.gpuProfileId = gpuProfileId; }
    public void setGpuUtilization(double gpuUtilization) { this.gpuUtilization = gpuUtilization; }
    public void setMemoryUtilization(double memoryUtilization) { this.memoryUtilization = memoryUtilization; }
    public void setMemoryTotalMb(long memoryTotalMb) { this.memoryTotalMb = memoryTotalMb; }
    public void setPowerWatts(double powerWatts) { this.powerWatts = powerWatts; }
    public void setTemperatureC(double temperatureC) { this.temperatureC = temperatureC; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
