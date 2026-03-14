// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class CatalogDeploymentResponse extends BaseResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("catalogitemid")
    private String catalogItemId;

    @SerializedName("catalogitemname")
    private String catalogItemName;

    @SerializedName("status")
    private String status;

    @SerializedName("vmid")
    private String vmId;

    @SerializedName("ipaddress")
    private String ipAddress;

    @SerializedName("publicipaddress")
    private String publicIpAddress;

    @SerializedName("accessurl")
    private String accessUrl;

    @SerializedName("connectionstring")
    private String connectionString;

    @SerializedName("created")
    private Date created;

    @SerializedName("batchoperationid")
    private String batchOperationId;

    public void setId(String id) { this.id = id; }
    public void setCatalogItemId(String catalogItemId) { this.catalogItemId = catalogItemId; }
    public void setCatalogItemName(String catalogItemName) { this.catalogItemName = catalogItemName; }
    public void setStatus(String status) { this.status = status; }
    public void setVmId(String vmId) { this.vmId = vmId; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setPublicIpAddress(String publicIpAddress) { this.publicIpAddress = publicIpAddress; }
    public void setAccessUrl(String accessUrl) { this.accessUrl = accessUrl; }
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }
    public void setCreated(Date created) { this.created = created; }
    public void setBatchOperationId(String batchOperationId) { this.batchOperationId = batchOperationId; }
}
