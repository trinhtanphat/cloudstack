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

package org.apache.cloudstack.gcp.monitoring;

import java.util.List;
import java.util.Map;

/**
 * Service interface for GCP Cloud Monitoring and Cloud Logging operations.
 */
public interface GcpMonitoringService {

    // ==================== Cloud Monitoring ====================
    Map<String, Object> createAlertPolicy(String projectId, String displayName,
                                           String conditionFilter, double thresholdValue,
                                           String comparisonType, String duration,
                                           List<String> notificationChannels);

    Map<String, Object> getAlertPolicy(String projectId, String policyId);

    List<Map<String, Object>> listAlertPolicies(String projectId);

    boolean deleteAlertPolicy(String projectId, String policyId);

    boolean enableAlertPolicy(String projectId, String policyId, boolean enabled);

    Map<String, Object> createNotificationChannel(String projectId, String displayName,
                                                    String type, Map<String, String> labels);

    List<Map<String, Object>> listNotificationChannels(String projectId);

    boolean deleteNotificationChannel(String projectId, String channelId);

    List<Map<String, Object>> queryTimeSeries(String projectId, String filter,
                                               String startTime, String endTime,
                                               String alignmentPeriod, String aggregation);

    Map<String, Object> createDashboard(String projectId, String displayName,
                                         List<Map<String, Object>> widgets);

    List<Map<String, Object>> listDashboards(String projectId);

    boolean deleteDashboard(String projectId, String dashboardId);

    Map<String, Object> createUptimeCheck(String projectId, String displayName,
                                           String monitoredResourceType, String host,
                                           String path, int periodSeconds);

    List<Map<String, Object>> listUptimeChecks(String projectId);

    boolean deleteUptimeCheck(String projectId, String checkId);

    // ==================== Cloud Logging ====================
    List<Map<String, Object>> listLogEntries(String projectId, String filter,
                                              String orderBy, int pageSize);

    boolean writeLogEntry(String projectId, String logName, String severity,
                           String message, Map<String, String> labels);

    Map<String, Object> createLogSink(String projectId, String sinkName,
                                       String destination, String filter);

    List<Map<String, Object>> listLogSinks(String projectId);

    boolean deleteLogSink(String projectId, String sinkName);

    Map<String, Object> createLogMetric(String projectId, String metricName,
                                          String description, String filter);

    List<Map<String, Object>> listLogMetrics(String projectId);

    boolean deleteLogMetric(String projectId, String metricName);

    List<Map<String, Object>> listLogBuckets(String projectId, String location);

    Map<String, Object> createLogBucket(String projectId, String location, String bucketId,
                                          int retentionDays, String description);

    boolean deleteLogBucket(String projectId, String location, String bucketId);

    Map<String, Object> createExclusion(String projectId, String exclusionName,
                                          String description, String filter);

    List<Map<String, Object>> listExclusions(String projectId);

    boolean deleteExclusion(String projectId, String exclusionName);
}
