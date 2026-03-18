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

package org.apache.cloudstack.gcp.compute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GcpComputeEngineServiceImplTest {

    @InjectMocks
    private GcpComputeEngineServiceImpl service;

    private static final String PROJECT = "my-gcp-project";
    private static final String ZONE = "us-central1-a";

    @Before
    public void setUp() {
        // No external dependencies — service uses stub data
    }

    // ==================== createInstance ====================

    @Test
    public void testCreateInstance_returnsInstanceMap() {
        Map<String, Object> result = service.createInstance(PROJECT, ZONE, "test-vm",
                "n2-standard-2", "debian-11", "debian-cloud", "default", null, null);
        assertNotNull(result);
        assertEquals("test-vm", result.get("name"));
        assertEquals(ZONE, result.get("zone"));
        assertEquals("n2-standard-2", result.get("machineType"));
        assertEquals("STAGING", result.get("status"));
    }

    @Test
    public void testCreateInstance_selfLinkContainsProjectAndZone() {
        Map<String, Object> result = service.createInstance(PROJECT, ZONE, "link-test-vm",
                "e2-medium", "debian-11", "debian-cloud", "default", null, null);
        String selfLink = (String) result.get("selfLink");
        assertNotNull(selfLink);
        assertTrue(selfLink.contains(PROJECT));
        assertTrue(selfLink.contains(ZONE));
        assertTrue(selfLink.contains("link-test-vm"));
    }

    @Test
    public void testCreateInstance_withLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("env", "test");
        labels.put("team", "backend");
        Map<String, Object> result = service.createInstance(PROJECT, ZONE, "labeled-vm",
                "e2-small", "debian-11", "debian-cloud", "default", labels, null);
        assertNotNull(result);
        assertEquals("labeled-vm", result.get("name"));
    }

    // ==================== getInstance ====================

    @Test
    public void testGetInstance_returnsRunningStatus() {
        Map<String, Object> result = service.getInstance(PROJECT, ZONE, "web-server-01");
        assertNotNull(result);
        assertEquals("web-server-01", result.get("name"));
        assertEquals("RUNNING", result.get("status"));
    }

    // ==================== listInstances ====================

    @Test
    public void testListInstances_returnsNonEmptyList() {
        List<Map<String, Object>> instances = service.listInstances(PROJECT, ZONE, null);
        assertNotNull(instances);
        assertFalse("Expected mock instances to be returned", instances.isEmpty());
    }

    @Test
    public void testListInstances_containsExpectedFields() {
        List<Map<String, Object>> instances = service.listInstances(PROJECT, null, null);
        for (Map<String, Object> inst : instances) {
            assertNotNull("name must be present", inst.get("name"));
            assertNotNull("state must be present", inst.get("state"));
            assertNotNull("machinetype must be present", inst.get("machinetype"));
        }
    }

    @Test
    public void testListInstances_hasRunningInstance() {
        List<Map<String, Object>> instances = service.listInstances(PROJECT, null, null);
        boolean hasRunning = instances.stream().anyMatch(i -> "RUNNING".equals(i.get("state")));
        assertTrue("At least one RUNNING instance expected", hasRunning);
    }

    // ==================== startInstance ====================

    @Test
    public void testStartInstance_returnsTrue() {
        assertTrue(service.startInstance(PROJECT, ZONE, "web-server-01"));
    }

    // ==================== stopInstance ====================

    @Test
    public void testStopInstance_returnsTrue() {
        assertTrue(service.stopInstance(PROJECT, ZONE, "web-server-01"));
    }

    // ==================== deleteInstance ====================

    @Test
    public void testDeleteInstance_returnsTrue() {
        assertTrue(service.deleteInstance(PROJECT, ZONE, "old-vm"));
    }

    // ==================== resetInstance ====================

    @Test
    public void testResetInstance_returnsTrue() {
        assertTrue(service.resetInstance(PROJECT, ZONE, "web-server-01"));
    }

    // ==================== resizeInstance ====================

    @Test
    public void testResizeInstance_returnsUpdatedMachineType() {
        Map<String, Object> result = service.resizeInstance(PROJECT, ZONE, "web-server-01", "n2-standard-4");
        assertNotNull(result);
        assertEquals("web-server-01", result.get("name"));
        assertEquals("n2-standard-4", result.get("machineType"));
    }
}
