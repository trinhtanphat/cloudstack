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

package org.apache.cloudstack.gcp.manager;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.cloudstack.gcp.GcpServiceType;
import org.apache.cloudstack.gcp.ai.GcpAiService;
import org.apache.cloudstack.gcp.compute.GcpComputeEngineService;
import org.apache.cloudstack.gcp.data.GcpDataService;
import org.apache.cloudstack.gcp.database.GcpDatabaseService;
import org.apache.cloudstack.gcp.infrastructure.GcpInfrastructureService;
import org.apache.cloudstack.gcp.kubernetes.GcpKubernetesService;
import org.apache.cloudstack.gcp.monitoring.GcpMonitoringService;
import org.apache.cloudstack.gcp.networking.GcpNetworkingService;
import org.apache.cloudstack.gcp.storage.GcpCloudStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GcpServiceManagerImplTest {

    @InjectMocks
    private GcpServiceManagerImpl manager;

    @Mock private GcpComputeEngineService computeEngineService;
    @Mock private GcpCloudStorageService cloudStorageService;
    @Mock private GcpDatabaseService databaseService;
    @Mock private GcpKubernetesService kubernetesService;
    @Mock private GcpAiService aiService;
    @Mock private GcpNetworkingService networkingService;
    @Mock private GcpMonitoringService monitoringService;
    @Mock private GcpDataService dataService;
    @Mock private GcpInfrastructureService infrastructureService;

    private static final String PROJECT = "my-gcp-project";

    // ==================== configure ====================

    @Test
    public void testConfigure_returnsTrue() {
        assertTrue(manager.configure("gcp-manager", null));
    }

    @Test
    public void testConfigure_withCredentials_returnsTrue() {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("credentials", "{\"type\": \"service_account\"}");
        assertTrue(manager.configure("gcp-manager", params));
    }

    // ==================== start/stop ====================

    @Test
    public void testStart_returnsTrue() {
        assertTrue(manager.start());
    }

    @Test
    public void testStop_returnsTrue() {
        assertTrue(manager.stop());
    }

    // ==================== validateCredentials ====================

    @Test
    public void testValidateCredentials_withValidJson_returnsTrue() {
        assertTrue(manager.validateCredentials("{\"type\": \"service_account\", \"project_id\": \"p\"}"));
    }

    @Test
    public void testValidateCredentials_withNull_returnsFalse() {
        assertFalse(manager.validateCredentials(null));
    }

    @Test
    public void testValidateCredentials_withEmptyString_returnsFalse() {
        assertFalse(manager.validateCredentials(""));
    }

    // ==================== getProjectInfo ====================

    @Test
    public void testGetProjectInfo_returnsProjectMap() {
        Map<String, Object> info = manager.getProjectInfo(PROJECT);
        assertNotNull(info);
        assertEquals(PROJECT, info.get("projectId"));
        assertEquals("ACTIVE", info.get("state"));
    }

    // ==================== listRegions ====================

    @Test
    public void testListRegions_returnsNonEmptyList() {
        List<String> regions = manager.listRegions(PROJECT);
        assertNotNull(regions);
        assertFalse("Expected at least one region", regions.isEmpty());
    }

    @Test
    public void testListRegions_containsUsCentral1() {
        List<String> regions = manager.listRegions(PROJECT);
        assertTrue("Expected us-central1 in regions", regions.contains("us-central1"));
    }

    // ==================== listZones ====================

    @Test
    public void testListZones_returnsNonEmptyList() {
        List<String> zones = manager.listZones(PROJECT, "us-central1");
        assertNotNull(zones);
        assertFalse("Expected at least one zone", zones.isEmpty());
    }

    @Test
    public void testListZones_containsRegionPrefix() {
        List<String> zones = manager.listZones(PROJECT, "us-central1");
        assertTrue("All zones should start with the region",
                zones.stream().allMatch(z -> z.startsWith("us-central1")));
    }

    // ==================== getServiceStatus ====================

    @Test
    public void testGetServiceStatus_returnsEnabledStatus() {
        Map<String, Object> status = manager.getServiceStatus(PROJECT, GcpServiceType.COMPUTE_ENGINE);
        assertNotNull(status);
        assertEquals(true, status.get("enabled"));
        assertEquals(GcpServiceType.COMPUTE_ENGINE.getId(), status.get("serviceType"));
    }

    // ==================== listEnabledServices ====================

    @Test
    public void testListEnabledServices_returnsAllServiceTypes() {
        List<Map<String, Object>> services = manager.listEnabledServices(PROJECT);
        assertNotNull(services);
        assertFalse("Expected all service types listed", services.isEmpty());
        assertEquals(GcpServiceType.values().length, services.size());
    }

    @Test
    public void testListEnabledServices_allEnabled() {
        List<Map<String, Object>> services = manager.listEnabledServices(PROJECT);
        assertTrue("All services should be enabled",
                services.stream().allMatch(s -> Boolean.TRUE.equals(s.get("enabled"))));
    }

    // ==================== getBillingInfo ====================

    @Test
    public void testGetBillingInfo_returnsBillingEnabled() {
        Map<String, Object> billing = manager.getBillingInfo(PROJECT);
        assertNotNull(billing);
        assertEquals(PROJECT, billing.get("projectId"));
        assertEquals(true, billing.get("billingEnabled"));
    }

    // ==================== enableService / disableService ====================

    @Test
    public void testEnableService_returnsTrue() {
        assertTrue(manager.enableService(PROJECT, GcpServiceType.CLOUD_STORAGE));
    }

    @Test
    public void testDisableService_returnsTrue() {
        assertTrue(manager.disableService(PROJECT, GcpServiceType.CLOUD_STORAGE));
    }
}
