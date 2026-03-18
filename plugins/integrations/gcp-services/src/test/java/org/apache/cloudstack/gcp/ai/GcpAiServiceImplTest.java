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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GcpAiServiceImplTest {

    @InjectMocks
    private GcpAiServiceImpl service;

    private static final String PROJECT = "my-gcp-project";
    private static final String REGION = "us-central1";

    // ==================== Vertex AI ====================

    @Test
    public void testCreateVertexAiDataset_returnsActive() {
        Map<String, Object> result = service.createVertexAiDataset(PROJECT, REGION,
                "image-ds", "gs://schema/image_1.0.0.yaml");
        assertNotNull(result);
        assertEquals("image-ds", result.get("displayName"));
        assertEquals("ACTIVE", result.get("state"));
        assertEquals(REGION, result.get("region"));
    }

    @Test
    public void testListVertexAiDatasets_returnsNonEmpty() {
        List<Map<String, Object>> datasets = service.listVertexAiDatasets(PROJECT, REGION);
        assertNotNull(datasets);
        assertFalse("Expected sample Vertex AI datasets", datasets.isEmpty());
    }

    @Test
    public void testListVertexAiDatasets_containsRequiredFields() {
        List<Map<String, Object>> datasets = service.listVertexAiDatasets(PROJECT, REGION);
        for (Map<String, Object> ds : datasets) {
            assertNotNull(ds.get("displayname"));
            assertNotNull(ds.get("state"));
            assertNotNull(ds.get("region"));
            assertNotNull(ds.get("metadataschemauri"));
        }
    }

    @Test
    public void testDeleteVertexAiDataset_returnsTrue() {
        assertTrue(service.deleteVertexAiDataset(PROJECT, REGION, "ds-123"));
    }

    // ==================== Gemini ====================

    @Test
    public void testListGeminiModels_returnsNonEmpty() {
        List<Map<String, Object>> models = service.listGeminiModels(PROJECT, REGION);
        assertNotNull(models);
        assertFalse("Expected Gemini models to be returned", models.isEmpty());
    }

    @Test
    public void testListGeminiModels_containsNameField() {
        List<Map<String, Object>> models = service.listGeminiModels(PROJECT, REGION);
        for (Map<String, Object> m : models) {
            assertNotNull(m.get("name"));
        }
    }

    @Test
    public void testListGeminiModels_containsKnownModel() {
        List<Map<String, Object>> models = service.listGeminiModels(PROJECT, REGION);
        boolean hasGemini15Pro = models.stream().anyMatch(m -> "gemini-1.5-pro".equals(m.get("name")));
        assertTrue("Expected gemini-1.5-pro in model list", hasGemini15Pro);
    }

    @Test
    public void testGenerateContent_returnsResponse() {
        Map<String, Object> result = service.generateContent(PROJECT, REGION, "gemini-1.5-pro",
                "What is 2+2?", null);
        assertNotNull(result);
        assertEquals("gemini-1.5-pro", result.get("model"));
    }

    @Test
    public void testCountTokens_returnsZeroByDefault() {
        Map<String, Object> result = service.countTokens(PROJECT, REGION, "gemini-1.5-pro", "Hello world");
        assertNotNull(result);
        assertEquals(0, result.get("totalTokens"));
    }

    // ==================== Notebooks ====================

    @Test
    public void testCreateNotebookInstance_returnsProvisioning() {
        Map<String, Object> result = service.createNotebookInstance(PROJECT, REGION,
                "research-nb", "n1-standard-4", "tf-2-13-cu113", "TensorFlow");
        assertNotNull(result);
        assertEquals("research-nb", result.get("name"));
        assertEquals("PROVISIONING", result.get("state"));
    }

    @Test
    public void testGetNotebookInstance_returnsActive() {
        Map<String, Object> result = service.getNotebookInstance(PROJECT, REGION, "research-notebook");
        assertNotNull(result);
        assertEquals("ACTIVE", result.get("state"));
    }

    @Test
    public void testListNotebookInstances_returnsNonEmpty() {
        List<Map<String, Object>> instances = service.listNotebookInstances(PROJECT, REGION);
        assertNotNull(instances);
        assertFalse("Expected sample notebook instances", instances.isEmpty());
    }

    @Test
    public void testListNotebookInstances_containsRequiredFields() {
        List<Map<String, Object>> instances = service.listNotebookInstances(PROJECT, REGION);
        Map<String, Object> nb = instances.get(0);
        assertNotNull(nb.get("name"));
        assertNotNull(nb.get("state"));
        assertNotNull(nb.get("location"));
        assertNotNull(nb.get("machinetype"));
        assertNotNull(nb.get("framework"));
    }

    @Test
    public void testStartNotebookInstance_returnsTrue() {
        assertTrue(service.startNotebookInstance(PROJECT, REGION, "research-notebook"));
    }

    @Test
    public void testStopNotebookInstance_returnsTrue() {
        assertTrue(service.stopNotebookInstance(PROJECT, REGION, "research-notebook"));
    }

    @Test
    public void testDeleteNotebookInstance_returnsTrue() {
        assertTrue(service.deleteNotebookInstance(PROJECT, REGION, "old-notebook"));
    }
}
