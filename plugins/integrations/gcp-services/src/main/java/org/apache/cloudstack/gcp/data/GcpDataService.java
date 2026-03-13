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

package org.apache.cloudstack.gcp.data;

import java.util.List;
import java.util.Map;

/**
 * Service interface for GCP Data services.
 * Manages Datastream, Dataplex, and Cloud Composer resources.
 */
public interface GcpDataService {

    // ==================== Datastream ====================
    Map<String, Object> createConnectionProfile(String projectId, String location,
                                                  String profileId, String displayName,
                                                  Map<String, Object> connectionConfig);

    List<Map<String, Object>> listConnectionProfiles(String projectId, String location);

    boolean deleteConnectionProfile(String projectId, String location, String profileId);

    Map<String, Object> createStream(String projectId, String location, String streamId,
                                      String displayName, String sourceProfileId,
                                      String destProfileId, Map<String, Object> sourceConfig,
                                      Map<String, Object> destConfig);

    Map<String, Object> getStream(String projectId, String location, String streamId);

    List<Map<String, Object>> listStreams(String projectId, String location);

    boolean startStream(String projectId, String location, String streamId);

    boolean pauseStream(String projectId, String location, String streamId);

    boolean deleteStream(String projectId, String location, String streamId);

    List<Map<String, Object>> listStreamObjects(String projectId, String location, String streamId);

    // ==================== Dataplex ====================
    Map<String, Object> createLake(String projectId, String location, String lakeId,
                                    String displayName, String description);

    Map<String, Object> getLake(String projectId, String location, String lakeId);

    List<Map<String, Object>> listLakes(String projectId, String location);

    boolean deleteLake(String projectId, String location, String lakeId);

    Map<String, Object> createZone(String projectId, String location, String lakeId,
                                    String zoneId, String type, String locationType);

    List<Map<String, Object>> listZones(String projectId, String location, String lakeId);

    boolean deleteZone(String projectId, String location, String lakeId, String zoneId);

    Map<String, Object> createAsset(String projectId, String location, String lakeId,
                                     String zoneId, String assetId, String resourceSpec);

    List<Map<String, Object>> listAssets(String projectId, String location, String lakeId,
                                          String zoneId);

    boolean deleteAsset(String projectId, String location, String lakeId,
                        String zoneId, String assetId);

    Map<String, Object> createDataplexTask(String projectId, String location, String lakeId,
                                            String taskId, Map<String, Object> taskConfig);

    List<Map<String, Object>> listDataplexTasks(String projectId, String location, String lakeId);

    boolean deleteDataplexTask(String projectId, String location, String lakeId, String taskId);

    // ==================== Cloud Composer ====================
    Map<String, Object> createComposerEnvironment(String projectId, String location,
                                                    String environmentName,
                                                    String imageVersion,
                                                    Map<String, Object> nodeConfig,
                                                    Map<String, Object> softwareConfig);

    Map<String, Object> getComposerEnvironment(String projectId, String location,
                                                String environmentName);

    List<Map<String, Object>> listComposerEnvironments(String projectId, String location);

    boolean deleteComposerEnvironment(String projectId, String location, String environmentName);

    Map<String, Object> updateComposerEnvironment(String projectId, String location,
                                                    String environmentName,
                                                    Map<String, Object> updates);

    List<Map<String, Object>> listComposerDags(String projectId, String location,
                                                String environmentName);

    Map<String, Object> triggerDag(String projectId, String location, String environmentName,
                                    String dagId, Map<String, Object> conf);

    List<Map<String, Object>> listDagRuns(String projectId, String location,
                                           String environmentName, String dagId);
}
