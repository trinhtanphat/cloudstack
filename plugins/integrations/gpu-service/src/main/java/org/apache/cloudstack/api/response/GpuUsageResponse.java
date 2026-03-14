// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class GpuUsageResponse extends BaseResponse {

    @SerializedName("gpuinstanceid")
    private String gpuInstanceId;

    @SerializedName("name")
    private String name;

    @SerializedName("provider")
    private String provider;

    @SerializedName("gpuprofileid")
    private String gpuProfileId;

    @SerializedName("gpucount")
    private int gpuCount;

    @SerializedName("runninghours")
    private double runningHours;

    @SerializedName("hourlyrateusd")
    private double hourlyRateUsd;

    @SerializedName("totalcostusd")
    private double totalCostUsd;

    @SerializedName("startdate")
    private Date startDate;

    @SerializedName("enddate")
    private Date endDate;

    public void setGpuInstanceId(String gpuInstanceId) { this.gpuInstanceId = gpuInstanceId; }
    public void setName(String name) { this.name = name; }
    public void setProvider(String provider) { this.provider = provider; }
    public void setGpuProfileId(String gpuProfileId) { this.gpuProfileId = gpuProfileId; }
    public void setGpuCount(int gpuCount) { this.gpuCount = gpuCount; }
    public void setRunningHours(double runningHours) { this.runningHours = runningHours; }
    public void setHourlyRateUsd(double hourlyRateUsd) { this.hourlyRateUsd = hourlyRateUsd; }
    public void setTotalCostUsd(double totalCostUsd) { this.totalCostUsd = totalCostUsd; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}
