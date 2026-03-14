// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class DatabaseBackupResponse extends BaseResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("dbinstanceid")
    private String dbInstanceId;

    @SerializedName("dbinstancename")
    private String dbInstanceName;

    @SerializedName("backuptype")
    private String backupType;

    @SerializedName("status")
    private String status;

    @SerializedName("sizebytes")
    private long sizeBytes;

    @SerializedName("created")
    private Date created;

    @SerializedName("expires")
    private Date expires;

    public void setId(String id) { this.id = id; }
    public void setDbInstanceId(String id) { this.dbInstanceId = id; }
    public void setDbInstanceName(String name) { this.dbInstanceName = name; }
    public void setBackupType(String type) { this.backupType = type; }
    public void setStatus(String status) { this.status = status; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public void setCreated(Date created) { this.created = created; }
    public void setExpires(Date expires) { this.expires = expires; }
}
