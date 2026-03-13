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

package org.apache.cloudstack.gcp.database;

import java.util.List;
import java.util.Map;

/**
 * Service interface for GCP Database operations.
 * Manages AlloyDB, Cloud SQL, Cloud Spanner, BigQuery, and BigQuery Reservation resources.
 */
public interface GcpDatabaseService {

    // ==================== AlloyDB ====================
    Map<String, Object> createAlloyDbCluster(String projectId, String region, String clusterId,
                                              String password, String network);

    Map<String, Object> getAlloyDbCluster(String projectId, String region, String clusterId);

    List<Map<String, Object>> listAlloyDbClusters(String projectId, String region);

    boolean deleteAlloyDbCluster(String projectId, String region, String clusterId);

    Map<String, Object> createAlloyDbInstance(String projectId, String region, String clusterId,
                                               String instanceId, String instanceType, int cpuCount);

    List<Map<String, Object>> listAlloyDbInstances(String projectId, String region, String clusterId);

    boolean deleteAlloyDbInstance(String projectId, String region, String clusterId, String instanceId);

    // ==================== Cloud SQL ====================
    Map<String, Object> createCloudSqlInstance(String projectId, String instanceName,
                                                String databaseVersion, String tier,
                                                String region, long storageSizeGb);

    Map<String, Object> getCloudSqlInstance(String projectId, String instanceName);

    List<Map<String, Object>> listCloudSqlInstances(String projectId);

    boolean deleteCloudSqlInstance(String projectId, String instanceName);

    boolean restartCloudSqlInstance(String projectId, String instanceName);

    Map<String, Object> createCloudSqlDatabase(String projectId, String instanceName,
                                                String databaseName, String charset);

    List<Map<String, Object>> listCloudSqlDatabases(String projectId, String instanceName);

    boolean deleteCloudSqlDatabase(String projectId, String instanceName, String databaseName);

    Map<String, Object> createCloudSqlBackup(String projectId, String instanceName, String description);

    List<Map<String, Object>> listCloudSqlBackups(String projectId, String instanceName);

    boolean restoreCloudSqlBackup(String projectId, String instanceName, String backupId);

    // ==================== Cloud Spanner ====================
    Map<String, Object> createSpannerInstance(String projectId, String instanceId,
                                               String displayName, String config,
                                               int nodeCount);

    Map<String, Object> getSpannerInstance(String projectId, String instanceId);

    List<Map<String, Object>> listSpannerInstances(String projectId);

    boolean deleteSpannerInstance(String projectId, String instanceId);

    Map<String, Object> createSpannerDatabase(String projectId, String instanceId,
                                               String databaseId, List<String> ddlStatements);

    List<Map<String, Object>> listSpannerDatabases(String projectId, String instanceId);

    boolean deleteSpannerDatabase(String projectId, String instanceId, String databaseId);

    // ==================== BigQuery ====================
    Map<String, Object> createBigQueryDataset(String projectId, String datasetId,
                                               String location, String description);

    Map<String, Object> getBigQueryDataset(String projectId, String datasetId);

    List<Map<String, Object>> listBigQueryDatasets(String projectId);

    boolean deleteBigQueryDataset(String projectId, String datasetId, boolean deleteContents);

    Map<String, Object> createBigQueryTable(String projectId, String datasetId,
                                             String tableId, Map<String, String> schema);

    List<Map<String, Object>> listBigQueryTables(String projectId, String datasetId);

    boolean deleteBigQueryTable(String projectId, String datasetId, String tableId);

    Map<String, Object> runBigQueryQuery(String projectId, String query, boolean useLegacySql);

    List<Map<String, Object>> getBigQueryJobResults(String projectId, String jobId);

    // ==================== BigQuery Reservation ====================
    Map<String, Object> createBigQueryReservation(String projectId, String location,
                                                   String reservationName, long slotCapacity);

    List<Map<String, Object>> listBigQueryReservations(String projectId, String location);

    boolean deleteBigQueryReservation(String projectId, String location, String reservationName);

    Map<String, Object> createCapacityCommitment(String projectId, String location,
                                                  long slotCount, String plan);

    List<Map<String, Object>> listCapacityCommitments(String projectId, String location);

    Map<String, Object> createReservationAssignment(String projectId, String location,
                                                     String reservationName, String jobType,
                                                     String assigneeId);

    List<Map<String, Object>> listReservationAssignments(String projectId, String location,
                                                          String reservationName);
}
