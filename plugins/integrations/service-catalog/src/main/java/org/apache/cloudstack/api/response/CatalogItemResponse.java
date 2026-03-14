// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

import java.util.List;

public class CatalogItemResponse extends BaseResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("category")
    private String category;

    @SerializedName("description")
    private String description;

    @SerializedName("icon")
    private String icon;

    @SerializedName("version")
    private String version;

    @SerializedName("mincpu")
    private int minCpu;

    @SerializedName("minmemorymb")
    private int minMemoryMb;

    @SerializedName("minstoragegb")
    private int minStorageGb;

    @SerializedName("defaultport")
    private int defaultPort;

    @SerializedName("features")
    private List<String> features;

    @SerializedName("popular")
    private boolean popular;

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setVersion(String version) { this.version = version; }
    public void setMinCpu(int minCpu) { this.minCpu = minCpu; }
    public void setMinMemoryMb(int minMemoryMb) { this.minMemoryMb = minMemoryMb; }
    public void setMinStorageGb(int minStorageGb) { this.minStorageGb = minStorageGb; }
    public void setDefaultPort(int defaultPort) { this.defaultPort = defaultPort; }
    public void setFeatures(List<String> features) { this.features = features; }
    public void setPopular(boolean popular) { this.popular = popular; }

    public String getCategory() { return category; }
}
