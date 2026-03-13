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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.cloudstack.gcp.compute.GcpComputeEngineService;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * API command to list GCP Compute Engine instances.
 * Usage: listGcpInstances projectid=<project> zone=<zone>
 */
public class ListGcpInstancesCmd {

    private static final Logger logger = LogManager.getLogger(ListGcpInstancesCmd.class);

    @Inject
    private GcpComputeEngineService computeEngineService;

    private String projectId;
    private String zone;
    private String filter;

    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setZone(String zone) { this.zone = zone; }
    public void setFilter(String filter) { this.filter = filter; }

    public List<Map<String, Object>> execute() {
        logger.info("Executing listGcpInstances for project: {} zone: {}", projectId, zone);
        return computeEngineService.listInstances(projectId, zone, filter);
    }
}
