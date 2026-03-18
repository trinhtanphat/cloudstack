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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GcpKubernetesServiceImplTest {

    @InjectMocks
    private GcpKubernetesServiceImpl service;

    private static final String PROJECT = "my-gcp-project";
    private static final String ZONE = "us-central1-a";
    private static final String REGION = "us-central1";

    // ==================== GKE Clusters ====================

    @Test
    public void testCreateGkeCluster_returnsProvisioning() {
        Map<String, Object> result = service.createGkeCluster(PROJECT, ZONE, "test-cluster",
                "default", null, "3", "e2-standard-4", "1.28", null);
        assertNotNull(result);
        assertEquals("test-cluster", result.get("name"));
        assertEquals("PROVISIONING", result.get("status"));
    }

    @Test
    public void testGetGkeCluster_returnsRunning() {
        Map<String, Object> result = service.getGkeCluster(PROJECT, ZONE, "prod-cluster");
        assertNotNull(result);
        assertEquals("RUNNING", result.get("status"));
    }

    @Test
    public void testListGkeClusters_returnsNonEmpty() {
        List<Map<String, Object>> clusters = service.listGkeClusters(PROJECT, null);
        assertNotNull(clusters);
        assertFalse("Expected sample GKE clusters", clusters.isEmpty());
    }

    @Test
    public void testListGkeClusters_containsRequiredFields() {
        List<Map<String, Object>> clusters = service.listGkeClusters(PROJECT, null);
        for (Map<String, Object> c : clusters) {
            assertNotNull(c.get("name"));
            assertNotNull(c.get("status"));
            assertNotNull(c.get("zone"));
            assertNotNull(c.get("clusterversion"));
            assertNotNull(c.get("nodecount"));
        }
    }

    @Test
    public void testListGkeClusters_hasRunningCluster() {
        List<Map<String, Object>> clusters = service.listGkeClusters(PROJECT, null);
        boolean hasRunning = clusters.stream().anyMatch(c -> "RUNNING".equals(c.get("status")));
        assertTrue("Expected at least one RUNNING GKE cluster", hasRunning);
    }

    @Test
    public void testDeleteGkeCluster_returnsTrue() {
        assertTrue(service.deleteGkeCluster(PROJECT, ZONE, "old-cluster"));
    }

    @Test
    public void testUpdateGkeCluster_returnsReconciling() {
        java.util.Map<String, Object> spec = new java.util.HashMap<>();
        spec.put("nodeCount", 5);
        Map<String, Object> result = service.updateGkeCluster(PROJECT, ZONE, "prod-cluster", spec);
        assertNotNull(result);
        assertEquals("RECONCILING", result.get("status"));
    }

    // ==================== Cloud Run ====================

    @Test
    public void testCreateCloudRunService_returnsService() {
        Map<String, Object> result = service.deployCloudRunService(PROJECT, REGION, "my-api",
            "gcr.io/project/img:v1", 512, 1, 10, new java.util.HashMap<String, String>());
        assertNotNull(result);
        assertEquals("my-api", result.get("name"));
        assertEquals("DEPLOYING", result.get("status"));
    }

    @Test
    public void testGetCloudRunService_returnsActive() {
        Map<String, Object> result = service.getCloudRunService(PROJECT, REGION, "api-service");
        assertNotNull(result);
        assertEquals("ACTIVE", result.get("status"));
    }

    @Test
    public void testListCloudRunServices_returnsNonEmpty() {
        List<Map<String, Object>> services = service.listCloudRunServices(PROJECT, REGION);
        assertNotNull(services);
        assertFalse("Expected sample Cloud Run services", services.isEmpty());
    }

    @Test
    public void testListCloudRunServices_containsRequiredFields() {
        List<Map<String, Object>> services = service.listCloudRunServices(PROJECT, REGION);
        for (Map<String, Object> svc : services) {
            assertNotNull(svc.get("name"));
            assertNotNull(svc.get("status"));
            assertNotNull(svc.get("region"));
            assertNotNull(svc.get("url"));
            assertNotNull(svc.get("image"));
        }
    }

    @Test
    public void testDeleteCloudRunService_returnsTrue() {
        assertTrue(service.deleteCloudRunService(PROJECT, REGION, "old-service"));
    }

    // ==================== Backup for GKE ====================

    @Test
    public void testCreateBackupPlan_returnsReady() {
        Map<String, Object> result = service.createBackupPlan(PROJECT, REGION, "daily-plan",
                "prod-cluster", "0 2 * * *", 30);
        assertNotNull(result);
        assertEquals("daily-plan", result.get("name"));
        assertEquals("READY", result.get("state"));
    }

    @Test
    public void testListBackupPlans_returnsNonEmpty() {
        List<Map<String, Object>> plans = service.listBackupPlans(PROJECT, REGION);
        assertNotNull(plans);
        assertFalse("Expected sample backup plans", plans.isEmpty());
    }

    @Test
    public void testListBackupPlans_containsRequiredFields() {
        List<Map<String, Object>> plans = service.listBackupPlans(PROJECT, REGION);
        Map<String, Object> p = plans.get(0);
        assertNotNull(p.get("name"));
        assertNotNull(p.get("cluster"));
        assertNotNull(p.get("schedule"));
        assertNotNull(p.get("retaindays"));
        assertNotNull(p.get("state"));
    }

    @Test
    public void testDeleteBackupPlan_returnsTrue() {
        assertTrue(service.deleteBackupPlan(PROJECT, REGION, "old-plan"));
    }
}
