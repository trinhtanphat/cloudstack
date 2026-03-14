// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class CatalogDeploymentInstanceStatusResponse extends BaseResponse {

    @SerializedName("index")
    private int index;

    @SerializedName("name")
    private String name;

    @SerializedName("status")
    private String status;

    @SerializedName("vmid")
    private String vmId;

    @SerializedName("privateip")
    private String privateIp;

    @SerializedName("publicip")
    private String publicIp;

    @SerializedName("publicport")
    private Integer publicPort;

    @SerializedName("error")
    private String error;

    @SerializedName("updated")
    private Date updated;

    public void setIndex(int index) { this.index = index; }
    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
    public void setVmId(String vmId) { this.vmId = vmId; }
    public void setPrivateIp(String privateIp) { this.privateIp = privateIp; }
    public void setPublicIp(String publicIp) { this.publicIp = publicIp; }
    public void setPublicPort(Integer publicPort) { this.publicPort = publicPort; }
    public void setError(String error) { this.error = error; }
    public void setUpdated(Date updated) { this.updated = updated; }
}