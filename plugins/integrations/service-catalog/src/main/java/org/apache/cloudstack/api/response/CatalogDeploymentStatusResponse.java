// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class CatalogDeploymentStatusResponse extends BaseResponse {

    @SerializedName("batchoperationid")
    private String batchOperationId;

    @SerializedName("catalogitemid")
    private String catalogItemId;

    @SerializedName("catalogitemname")
    private String catalogItemName;

    @SerializedName("state")
    private String state;

    @SerializedName("totalcount")
    private int totalCount;

    @SerializedName("completedcount")
    private int completedCount;

    @SerializedName("failedcount")
    private int failedCount;

    @SerializedName("instances")
    private List<CatalogDeploymentInstanceStatusResponse> instances;

    @SerializedName("created")
    private Date created;

    @SerializedName("updated")
    private Date updated;

    public void setBatchOperationId(String batchOperationId) { this.batchOperationId = batchOperationId; }
    public void setCatalogItemId(String catalogItemId) { this.catalogItemId = catalogItemId; }
    public void setCatalogItemName(String catalogItemName) { this.catalogItemName = catalogItemName; }
    public void setState(String state) { this.state = state; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public void setInstances(List<CatalogDeploymentInstanceStatusResponse> instances) { this.instances = instances; }
    public void setCreated(Date created) { this.created = created; }
    public void setUpdated(Date updated) { this.updated = updated; }
}