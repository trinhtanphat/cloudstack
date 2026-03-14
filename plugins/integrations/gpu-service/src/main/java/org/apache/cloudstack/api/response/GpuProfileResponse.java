// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class GpuProfileResponse extends BaseResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("provider")
    private String provider;

    @SerializedName("gpuvendor")
    private String gpuVendor;

    @SerializedName("gpumodel")
    private String gpuModel;

    @SerializedName("memorygb")
    private int memoryGb;

    @SerializedName("maxgpucount")
    private int maxGpuCount;

    @SerializedName("description")
    private String description;

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setProvider(String provider) { this.provider = provider; }
    public void setGpuVendor(String gpuVendor) { this.gpuVendor = gpuVendor; }
    public void setGpuModel(String gpuModel) { this.gpuModel = gpuModel; }
    public void setMemoryGb(int memoryGb) { this.memoryGb = memoryGb; }
    public void setMaxGpuCount(int maxGpuCount) { this.maxGpuCount = maxGpuCount; }
    public void setDescription(String description) { this.description = description; }
}
