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

import org.apache.cloudstack.gcp.kubernetes.GcpKubernetesService;
import org.apache.cloudstack.gcp.api.response.GcpServiceResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * API command to create a GKE cluster.
 * Usage: createGcpGkeCluster projectid=<project> zone=<zone> name=<name>
 *        nodecount=<count> machinetype=<type>
 */
public class CreateGcpGkeClusterCmd {

    private static final Logger logger = LogManager.getLogger(CreateGcpGkeClusterCmd.class);

    @Inject
    private GcpKubernetesService kubernetesService;

    private String projectId;
    private String zone;
    private String clusterName;
    private String networkName;
    private String subnetworkName;
    private String initialNodeCount;
    private String machineType;
    private String clusterVersion;
    private Map<String, String> labels;

    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setZone(String zone) { this.zone = zone; }
    public void setClusterName(String clusterName) { this.clusterName = clusterName; }
    public void setNetworkName(String networkName) { this.networkName = networkName; }
    public void setSubnetworkName(String subnetworkName) { this.subnetworkName = subnetworkName; }
    public void setInitialNodeCount(String initialNodeCount) { this.initialNodeCount = initialNodeCount; }
    public void setMachineType(String machineType) { this.machineType = machineType; }
    public void setClusterVersion(String clusterVersion) { this.clusterVersion = clusterVersion; }
    public void setLabels(Map<String, String> labels) { this.labels = labels; }

    public GcpServiceResponse execute() {
        logger.info("Executing createGcpGkeCluster: {}", clusterName);

        Map<String, Object> result = kubernetesService.createGkeCluster(
            projectId, zone, clusterName, networkName, subnetworkName,
            initialNodeCount, machineType, clusterVersion, labels);

        GcpServiceResponse response = new GcpServiceResponse();
        response.setName((String) result.get("name"));
        response.setZone((String) result.get("zone"));
        response.setState((String) result.get("status"));
        response.setProjectId(projectId);
        response.setServiceType("kubernetes-engine");
        response.setDetails(result);

        return response;
    }
}
