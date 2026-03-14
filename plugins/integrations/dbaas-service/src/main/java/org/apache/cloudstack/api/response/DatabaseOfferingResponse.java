// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class DatabaseOfferingResponse extends BaseResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("dbengine")
    private String dbEngine;

    @SerializedName("dbversion")
    private String dbVersion;

    @SerializedName("description")
    private String description;

    @SerializedName("defaultport")
    private int defaultPort;

    @SerializedName("mincpu")
    private int minCpu;

    @SerializedName("minmemorymb")
    private int minMemoryMb;

    @SerializedName("minstoragegb")
    private int minStorageGb;

    @SerializedName("templateid")
    private String templateId;

    @SerializedName("cloudinitscript")
    private String cloudInitScript;

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public void setDbEngine(String dbEngine) { this.dbEngine = dbEngine; }
    public String getDbEngine() { return dbEngine; }
    public void setDbVersion(String dbVersion) { this.dbVersion = dbVersion; }
    public String getDbVersion() { return dbVersion; }
    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }
    public void setDefaultPort(int defaultPort) { this.defaultPort = defaultPort; }
    public int getDefaultPort() { return defaultPort; }
    public void setMinCpu(int minCpu) { this.minCpu = minCpu; }
    public int getMinCpu() { return minCpu; }
    public void setMinMemoryMb(int minMemoryMb) { this.minMemoryMb = minMemoryMb; }
    public int getMinMemoryMb() { return minMemoryMb; }
    public void setMinStorageGb(int minStorageGb) { this.minStorageGb = minStorageGb; }
    public int getMinStorageGb() { return minStorageGb; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getTemplateId() { return templateId; }
    public void setCloudInitScript(String cloudInitScript) { this.cloudInitScript = cloudInitScript; }
    public String getCloudInitScript() { return cloudInitScript; }
}
