// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.cloudstack.gcp.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Response object for GCP Compute Engine instance operations.
 */
public class GcpComputeInstanceResponse extends GcpServiceResponse {

    @SerializedName("machinetype")
    private String machineType;

    @SerializedName("imagefamily")
    private String imageFamily;

    @SerializedName("networkname")
    private String networkName;

    @SerializedName("internalip")
    private String internalIp;

    @SerializedName("externalip")
    private String externalIp;

    @SerializedName("disks")
    private List<Map<String, Object>> disks;

    @SerializedName("labels")
    private Map<String, String> labels;

    @SerializedName("metadata")
    private Map<String, String> metadata;

    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }

    public String getImageFamily() { return imageFamily; }
    public void setImageFamily(String imageFamily) { this.imageFamily = imageFamily; }

    public String getNetworkName() { return networkName; }
    public void setNetworkName(String networkName) { this.networkName = networkName; }

    public String getInternalIp() { return internalIp; }
    public void setInternalIp(String internalIp) { this.internalIp = internalIp; }

    public String getExternalIp() { return externalIp; }
    public void setExternalIp(String externalIp) { this.externalIp = externalIp; }

    public List<Map<String, Object>> getDisks() { return disks; }
    public void setDisks(List<Map<String, Object>> disks) { this.disks = disks; }

    public Map<String, String> getLabels() { return labels; }
    public void setLabels(Map<String, String> labels) { this.labels = labels; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}
