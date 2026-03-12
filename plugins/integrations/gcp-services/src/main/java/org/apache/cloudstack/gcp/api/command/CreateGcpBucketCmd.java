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

import org.apache.cloudstack.gcp.storage.GcpCloudStorageService;
import org.apache.cloudstack.gcp.api.response.GcpServiceResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * API command to create a GCP Cloud Storage bucket.
 * Usage: createGcpBucket projectid=<project> name=<name> location=<location>
 *        storageclass=<class> versioning=<true|false>
 */
public class CreateGcpBucketCmd {

    private static final Logger logger = LogManager.getLogger(CreateGcpBucketCmd.class);

    @Inject
    private GcpCloudStorageService cloudStorageService;

    private String projectId;
    private String bucketName;
    private String location;
    private String storageClass;
    private boolean versioningEnabled;

    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public void setLocation(String location) { this.location = location; }
    public void setStorageClass(String storageClass) { this.storageClass = storageClass; }
    public void setVersioningEnabled(boolean versioningEnabled) { this.versioningEnabled = versioningEnabled; }

    public GcpServiceResponse execute() {
        logger.info("Executing createGcpBucket: {}", bucketName);

        Map<String, Object> result = cloudStorageService.createBucket(
            projectId, bucketName, location, storageClass, versioningEnabled);

        GcpServiceResponse response = new GcpServiceResponse();
        response.setName((String) result.get("name"));
        response.setRegion((String) result.get("location"));
        response.setState("ACTIVE");
        response.setProjectId(projectId);
        response.setServiceType("cloud-storage");
        response.setDetails(result);

        return response;
    }
}
