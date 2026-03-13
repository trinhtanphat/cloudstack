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

package org.apache.cloudstack.gcp.infrastructure;

import java.util.List;
import java.util.Map;

/**
 * Service interface for GCP Infrastructure services.
 * Manages Secret Manager, Artifact Registry, Cloud Memorystore for Redis.
 */
public interface GcpInfrastructureService {

    // ==================== Secret Manager ====================
    Map<String, Object> createSecret(String projectId, String secretId,
                                      Map<String, String> labels, String replication);

    Map<String, Object> getSecret(String projectId, String secretId);

    List<Map<String, Object>> listSecrets(String projectId);

    boolean deleteSecret(String projectId, String secretId);

    Map<String, Object> addSecretVersion(String projectId, String secretId, byte[] payload);

    Map<String, Object> accessSecretVersion(String projectId, String secretId, String versionId);

    List<Map<String, Object>> listSecretVersions(String projectId, String secretId);

    boolean disableSecretVersion(String projectId, String secretId, String versionId);

    boolean enableSecretVersion(String projectId, String secretId, String versionId);

    boolean destroySecretVersion(String projectId, String secretId, String versionId);

    // ==================== Artifact Registry ====================
    Map<String, Object> createRepository(String projectId, String location,
                                          String repositoryId, String format,
                                          String description);

    Map<String, Object> getRepository(String projectId, String location, String repositoryId);

    List<Map<String, Object>> listRepositories(String projectId, String location);

    boolean deleteRepository(String projectId, String location, String repositoryId);

    List<Map<String, Object>> listPackages(String projectId, String location, String repositoryId);

    List<Map<String, Object>> listVersions(String projectId, String location,
                                             String repositoryId, String packageName);

    boolean deleteVersion(String projectId, String location, String repositoryId,
                           String packageName, String versionName);

    List<Map<String, Object>> listDockerImages(String projectId, String location,
                                                String repositoryId);

    Map<String, Object> getDockerImage(String projectId, String location, String repositoryId,
                                        String imageName);

    // ==================== Cloud Memorystore for Redis ====================
    Map<String, Object> createRedisInstance(String projectId, String location,
                                             String instanceId, String tier,
                                             int memorySizeGb, String redisVersion,
                                             String networkName, String connectMode);

    Map<String, Object> getRedisInstance(String projectId, String location, String instanceId);

    List<Map<String, Object>> listRedisInstances(String projectId, String location);

    boolean deleteRedisInstance(String projectId, String location, String instanceId);

    boolean upgradeRedisInstance(String projectId, String location, String instanceId,
                                  String redisVersion);

    Map<String, Object> scaleRedisInstance(String projectId, String location,
                                            String instanceId, int newMemorySizeGb);

    Map<String, Object> createRedisBackup(String projectId, String location, String instanceId);

    boolean restoreRedisBackup(String projectId, String location, String instanceId,
                                String backupId);

    Map<String, Object> failoverRedisInstance(String projectId, String location, String instanceId);

    Map<String, Object> getRedisInstanceAuth(String projectId, String location, String instanceId);
}
