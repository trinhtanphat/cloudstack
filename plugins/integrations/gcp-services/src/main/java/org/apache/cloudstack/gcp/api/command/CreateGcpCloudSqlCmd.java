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

import org.apache.cloudstack.gcp.database.GcpDatabaseService;
import org.apache.cloudstack.gcp.api.response.GcpServiceResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * API command to create a Cloud SQL instance.
 * Usage: createGcpCloudSqlInstance projectid=<project> name=<name> version=<version>
 *        tier=<tier> region=<region> storagesize=<gb>
 */
public class CreateGcpCloudSqlCmd {

    private static final Logger logger = LogManager.getLogger(CreateGcpCloudSqlCmd.class);

    @Inject
    private GcpDatabaseService databaseService;

    private String projectId;
    private String instanceName;
    private String databaseVersion;
    private String tier;
    private String region;
    private long storageSizeGb;

    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }
    public void setDatabaseVersion(String databaseVersion) { this.databaseVersion = databaseVersion; }
    public void setTier(String tier) { this.tier = tier; }
    public void setRegion(String region) { this.region = region; }
    public void setStorageSizeGb(long storageSizeGb) { this.storageSizeGb = storageSizeGb; }

    public GcpServiceResponse execute() {
        logger.info("Executing createGcpCloudSqlInstance: {}", instanceName);

        Map<String, Object> result = databaseService.createCloudSqlInstance(
            projectId, instanceName, databaseVersion, tier, region, storageSizeGb);

        GcpServiceResponse response = new GcpServiceResponse();
        response.setName((String) result.get("name"));
        response.setRegion((String) result.get("region"));
        response.setState((String) result.get("state"));
        response.setProjectId(projectId);
        response.setServiceType("cloud-sql");
        response.setDetails(result);

        return response;
    }
}
