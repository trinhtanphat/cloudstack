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

package org.apache.cloudstack.gcp.kubernetes;

import java.util.List;
import java.util.Map;

/**
 * Service interface for GCP Kubernetes Engine (GKE) and Cloud Run operations.
 * Manages GKE clusters, node pools, workloads, Cloud Run services, and Backup for GKE.
 */
public interface GcpKubernetesService {

    // ==================== GKE Clusters ====================
    Map<String, Object> createGkeCluster(String projectId, String zone, String clusterName,
                                          String networkName, String subnetworkName,
                                          String initialNodeCount, String machineType,
                                          String clusterVersion, Map<String, String> labels);

    Map<String, Object> getGkeCluster(String projectId, String zone, String clusterName);

    List<Map<String, Object>> listGkeClusters(String projectId, String zone);

    boolean deleteGkeCluster(String projectId, String zone, String clusterName);

    Map<String, Object> updateGkeCluster(String projectId, String zone, String clusterName,
                                          Map<String, Object> updateSpec);

    // ==================== Node Pools ====================
    Map<String, Object> createNodePool(String projectId, String zone, String clusterName,
                                        String nodePoolName, int nodeCount, String machineType,
                                        boolean autoScaling, int minNodes, int maxNodes);

    Map<String, Object> getNodePool(String projectId, String zone, String clusterName,
                                     String nodePoolName);

    List<Map<String, Object>> listNodePools(String projectId, String zone, String clusterName);

    boolean deleteNodePool(String projectId, String zone, String clusterName, String nodePoolName);

    boolean resizeNodePool(String projectId, String zone, String clusterName,
                           String nodePoolName, int newSize);

    // ==================== Cloud Run ====================
    Map<String, Object> deployCloudRunService(String projectId, String region,
                                               String serviceName, String imageUrl,
                                               int memory, int cpu, int maxInstances,
                                               Map<String, String> envVars);

    Map<String, Object> getCloudRunService(String projectId, String region, String serviceName);

    List<Map<String, Object>> listCloudRunServices(String projectId, String region);

    boolean deleteCloudRunService(String projectId, String region, String serviceName);

    Map<String, Object> getCloudRunRevision(String projectId, String region,
                                             String serviceName, String revisionName);

    List<Map<String, Object>> listCloudRunRevisions(String projectId, String region,
                                                     String serviceName);

    boolean setCloudRunIamPolicy(String projectId, String region, String serviceName,
                                  String member, String role);

    // ==================== Backup for GKE ====================
    Map<String, Object> createBackupPlan(String projectId, String location, String clusterName,
                                          String backupPlanName, String cronSchedule,
                                          int retainDays);

    List<Map<String, Object>> listBackupPlans(String projectId, String location);

    boolean deleteBackupPlan(String projectId, String location, String backupPlanName);

    Map<String, Object> createBackup(String projectId, String location, String backupPlanName,
                                      String backupName);

    List<Map<String, Object>> listBackups(String projectId, String location, String backupPlanName);

    Map<String, Object> restoreBackup(String projectId, String location, String backupPlanName,
                                       String backupName, String restorePlanName);
}
