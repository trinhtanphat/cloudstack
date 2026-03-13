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

package org.apache.cloudstack.gcp.api.command;

import java.util.Map;

import javax.inject.Inject;

import org.apache.cloudstack.gcp.compute.GcpComputeEngineService;
import org.apache.cloudstack.gcp.api.response.GcpComputeInstanceResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * API command to create a GCP Compute Engine instance.
 * Usage: createGcpInstance projectid=<project> zone=<zone> name=<name>
 *        machinetype=<type> imagefamily=<family> imageproject=<project>
 */
public class CreateGcpInstanceCmd {

    private static final Logger logger = LogManager.getLogger(CreateGcpInstanceCmd.class);

    @Inject
    private GcpComputeEngineService computeEngineService;

    private String projectId;
    private String zone;
    private String instanceName;
    private String machineType;
    private String imageFamily;
    private String imageProject;
    private String networkName;
    private Map<String, String> labels;
    private Map<String, String> metadata;

    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setZone(String zone) { this.zone = zone; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }
    public void setMachineType(String machineType) { this.machineType = machineType; }
    public void setImageFamily(String imageFamily) { this.imageFamily = imageFamily; }
    public void setImageProject(String imageProject) { this.imageProject = imageProject; }
    public void setNetworkName(String networkName) { this.networkName = networkName; }
    public void setLabels(Map<String, String> labels) { this.labels = labels; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public GcpComputeInstanceResponse execute() {
        logger.info("Executing createGcpInstance: {} in zone: {}", instanceName, zone);

        Map<String, Object> result = computeEngineService.createInstance(
            projectId, zone, instanceName, machineType,
            imageFamily, imageProject, networkName, labels, metadata);

        GcpComputeInstanceResponse response = new GcpComputeInstanceResponse();
        response.setName((String) result.get("name"));
        response.setZone((String) result.get("zone"));
        response.setMachineType((String) result.get("machineType"));
        response.setState((String) result.get("status"));
        response.setProjectId(projectId);
        response.setServiceType("compute-engine");

        return response;
    }
}
