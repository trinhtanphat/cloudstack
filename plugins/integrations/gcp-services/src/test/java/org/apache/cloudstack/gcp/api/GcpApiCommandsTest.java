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

package org.apache.cloudstack.gcp.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.apache.cloudstack.gcp.api.command.CreateGcpBucketCmd;
import org.apache.cloudstack.gcp.api.command.CreateGcpInstanceCmd;
import org.apache.cloudstack.gcp.api.command.ListGcpBucketsCmd;
import org.apache.cloudstack.gcp.api.command.ListGcpInstancesCmd;
import org.apache.cloudstack.gcp.api.response.GcpComputeInstanceResponse;
import org.apache.cloudstack.gcp.api.response.GcpServiceResponse;
import org.apache.cloudstack.gcp.compute.GcpComputeEngineService;
import org.apache.cloudstack.gcp.storage.GcpCloudStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GcpApiCommandsTest {

    @Mock private GcpComputeEngineService computeService;
    @Mock private GcpCloudStorageService storageService;

    private static final String PROJECT = "my-gcp-project";
    private static final String ZONE = "us-central1-a";

    // ====== Helper to inject mocks into commands via reflection ======

    private void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ==================== CreateGcpInstanceCmd ====================

    @Test
    public void testCreateGcpInstanceCmd_execute_returnsResponse() throws Exception {
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("name", "test-vm");
        serviceResult.put("zone", ZONE);
        serviceResult.put("machineType", "n2-standard-2");
        serviceResult.put("status", "STAGING");
        serviceResult.put("selfLink", "projects/" + PROJECT + "/zones/" + ZONE + "/instances/test-vm");

        Mockito.when(computeService.createInstance(
                Mockito.eq(PROJECT), Mockito.eq(ZONE), Mockito.eq("test-vm"),
                Mockito.eq("n2-standard-2"), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(serviceResult);

        CreateGcpInstanceCmd cmd = new CreateGcpInstanceCmd();
        inject(cmd, "computeEngineService", computeService);
        cmd.setProjectId(PROJECT);
        cmd.setZone(ZONE);
        cmd.setInstanceName("test-vm");
        cmd.setMachineType("n2-standard-2");

        GcpComputeInstanceResponse response = cmd.execute();
        assertNotNull(response);
        assertEquals("test-vm", response.getName());
        assertEquals(ZONE, response.getZone());
        assertEquals("n2-standard-2", response.getMachineType());
        assertEquals("STAGING", response.getState());
        assertEquals(PROJECT, response.getProjectId());
        assertEquals("compute-engine", response.getServiceType());
    }

    // ==================== ListGcpInstancesCmd ====================

    @Test
    public void testListGcpInstancesCmd_execute_returnsInstances() throws Exception {
        List<Map<String, Object>> mockInstances = new ArrayList<>();
        Map<String, Object> inst = new HashMap<>();
        inst.put("name", "web-server-01");
        inst.put("state", "RUNNING");
        mockInstances.add(inst);

        Mockito.when(computeService.listInstances(PROJECT, ZONE, null)).thenReturn(mockInstances);

        ListGcpInstancesCmd cmd = new ListGcpInstancesCmd();
        inject(cmd, "computeEngineService", computeService);
        cmd.setProjectId(PROJECT);
        cmd.setZone(ZONE);

        List<Map<String, Object>> result = cmd.execute();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("web-server-01", result.get(0).get("name"));
    }

    @Test
    public void testListGcpInstancesCmd_execute_withFilter() throws Exception {
        List<Map<String, Object>> mockInstances = new ArrayList<>();
        Mockito.when(computeService.listInstances(PROJECT, null, "status=RUNNING")).thenReturn(mockInstances);

        ListGcpInstancesCmd cmd = new ListGcpInstancesCmd();
        inject(cmd, "computeEngineService", computeService);
        cmd.setProjectId(PROJECT);
        cmd.setFilter("status=RUNNING");

        List<Map<String, Object>> result = cmd.execute();
        assertNotNull(result);
    }

    // ==================== CreateGcpBucketCmd ====================

    @Test
    public void testCreateGcpBucketCmd_execute_returnsBucket() throws Exception {
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("name", "my-bucket");
        serviceResult.put("location", "US");
        serviceResult.put("storageClass", "STANDARD");
        serviceResult.put("versioningEnabled", true);

        Mockito.when(storageService.createBucket(PROJECT, "my-bucket", "US", "STANDARD", true))
                .thenReturn(serviceResult);

        CreateGcpBucketCmd cmd = new CreateGcpBucketCmd();
        inject(cmd, "cloudStorageService", storageService);
        cmd.setProjectId(PROJECT);
        cmd.setBucketName("my-bucket");
        cmd.setLocation("US");
        cmd.setStorageClass("STANDARD");
        cmd.setVersioningEnabled(true);

        GcpServiceResponse response = cmd.execute();
        assertNotNull(response);
        assertEquals("my-bucket", response.getName());
        assertEquals("cloud-storage", response.getServiceType());
    }

    // ==================== ListGcpBucketsCmd ====================

    @Test
    public void testListGcpBucketsCmd_execute_returnsBuckets() throws Exception {
        List<Map<String, Object>> mockBuckets = new ArrayList<>();
        Map<String, Object> b = new HashMap<>();
        b.put("name", "my-app-assets");
        b.put("location", "US");
        mockBuckets.add(b);

        Mockito.when(storageService.listBuckets(PROJECT)).thenReturn(mockBuckets);

        ListGcpBucketsCmd cmd = new ListGcpBucketsCmd();
        inject(cmd, "cloudStorageService", storageService);
        cmd.setProjectId(PROJECT);

        List<Map<String, Object>> result = cmd.execute();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("my-app-assets", result.get(0).get("name"));
    }
}
