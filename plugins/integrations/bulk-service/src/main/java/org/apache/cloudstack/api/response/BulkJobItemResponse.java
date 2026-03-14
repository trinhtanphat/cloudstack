// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class BulkJobItemResponse extends BaseResponse {

    @SerializedName("index")
    private int index;

    @SerializedName("resourceid")
    private String resourceId;

    @SerializedName("resourcetype")
    private String resourceType;

    @SerializedName("name")
    private String name;

    @SerializedName("status")
    private String status;

    @SerializedName("ipaddress")
    private String ipAddress;

    @SerializedName("publicport")
    private Integer publicPort;

    @SerializedName("error")
    private String error;

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Integer getPublicPort() { return publicPort; }
    public void setPublicPort(Integer publicPort) { this.publicPort = publicPort; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
