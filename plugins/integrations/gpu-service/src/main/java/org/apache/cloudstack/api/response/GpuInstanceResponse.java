// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class GpuInstanceResponse extends BaseResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("state")
    private String state;

    @SerializedName("provider")
    private String provider;

    @SerializedName("gpuprofileid")
    private String gpuProfileId;

    @SerializedName("gpucount")
    private int gpuCount;

    @SerializedName("zoneid")
    private String zoneId;

    @SerializedName("vmid")
    private String vmId;

    @SerializedName("serviceofferingid")
    private String serviceOfferingId;

    @SerializedName("templateid")
    private String templateId;

    @SerializedName("networkid")
    private String networkId;

    @SerializedName("created")
    private Date created;

    @SerializedName("account")
    private String account;

    @SerializedName("domainid")
    private String domainId;

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setState(String state) { this.state = state; }
    public void setProvider(String provider) { this.provider = provider; }
    public void setGpuProfileId(String gpuProfileId) { this.gpuProfileId = gpuProfileId; }
    public void setGpuCount(int gpuCount) { this.gpuCount = gpuCount; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public void setVmId(String vmId) { this.vmId = vmId; }
    public void setServiceOfferingId(String serviceOfferingId) { this.serviceOfferingId = serviceOfferingId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public void setNetworkId(String networkId) { this.networkId = networkId; }
    public void setCreated(Date created) { this.created = created; }
    public void setAccount(String account) { this.account = account; }
    public void setDomainId(String domainId) { this.domainId = domainId; }
}
