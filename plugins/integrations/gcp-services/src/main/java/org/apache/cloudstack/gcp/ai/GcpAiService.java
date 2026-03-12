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

package org.apache.cloudstack.gcp.ai;

import java.util.List;
import java.util.Map;

/**
 * Service interface for GCP AI/ML operations.
 * Manages Vertex AI, Gemini API, and Notebooks resources.
 */
public interface GcpAiService {

    // ==================== Vertex AI ====================
    Map<String, Object> createVertexAiDataset(String projectId, String region,
                                               String displayName, String metadataSchemaUri);

    List<Map<String, Object>> listVertexAiDatasets(String projectId, String region);

    boolean deleteVertexAiDataset(String projectId, String region, String datasetId);

    Map<String, Object> createTrainingPipeline(String projectId, String region,
                                                String displayName, String trainingTaskDefinition,
                                                Map<String, Object> trainingTaskInputs,
                                                String modelDisplayName);

    List<Map<String, Object>> listTrainingPipelines(String projectId, String region);

    Map<String, Object> getTrainingPipeline(String projectId, String region, String pipelineId);

    boolean cancelTrainingPipeline(String projectId, String region, String pipelineId);

    Map<String, Object> deployModel(String projectId, String region, String endpointId,
                                     String modelId, String machineType, int minReplicaCount,
                                     int maxReplicaCount);

    Map<String, Object> createEndpoint(String projectId, String region, String displayName);

    List<Map<String, Object>> listEndpoints(String projectId, String region);

    boolean deleteEndpoint(String projectId, String region, String endpointId);

    Map<String, Object> predict(String projectId, String region, String endpointId,
                                 List<Map<String, Object>> instances);

    // ==================== Gemini API ====================
    Map<String, Object> generateContent(String projectId, String region, String model,
                                         String prompt, Map<String, Object> generationConfig);

    Map<String, Object> generateContentWithImage(String projectId, String region, String model,
                                                   String prompt, byte[] imageData,
                                                   String mimeType);

    Map<String, Object> startChat(String projectId, String region, String model,
                                   List<Map<String, Object>> history);

    Map<String, Object> sendChatMessage(String projectId, String region, String model,
                                         String sessionId, String message);

    Map<String, Object> countTokens(String projectId, String region, String model, String text);

    List<Map<String, Object>> listGeminiModels(String projectId, String region);

    // ==================== Notebooks (Vertex AI Workbench) ====================
    Map<String, Object> createNotebookInstance(String projectId, String location,
                                                String instanceName, String machineType,
                                                String vmImage, String framework);

    Map<String, Object> getNotebookInstance(String projectId, String location, String instanceName);

    List<Map<String, Object>> listNotebookInstances(String projectId, String location);

    boolean startNotebookInstance(String projectId, String location, String instanceName);

    boolean stopNotebookInstance(String projectId, String location, String instanceName);

    boolean deleteNotebookInstance(String projectId, String location, String instanceName);

    Map<String, Object> createNotebookRuntime(String projectId, String location,
                                               String runtimeName, String runtimeTemplate);

    List<Map<String, Object>> listNotebookRuntimes(String projectId, String location);

    boolean deleteNotebookRuntime(String projectId, String location, String runtimeName);
}
