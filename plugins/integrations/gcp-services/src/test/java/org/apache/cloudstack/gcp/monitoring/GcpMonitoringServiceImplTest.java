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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GcpMonitoringServiceImplTest {

    @InjectMocks
    private GcpMonitoringServiceImpl service;

    private static final String PROJECT = "my-gcp-project";

    // ==================== Alert Policies ====================

    @Test
    public void testCreateAlertPolicy_returnsEnabled() {
        Map<String, Object> result = service.createAlertPolicy(PROJECT, "High CPU",
                "metric.type=\"compute.googleapis.com/instance/cpu/utilization\"",
                0.9, "COMPARISON_GT", "60s", null);
        assertNotNull(result);
        assertEquals("High CPU", result.get("displayName"));
        assertEquals(true, result.get("enabled"));
    }

    @Test
    public void testGetAlertPolicy_returnsEnabled() {
        Map<String, Object> result = service.getAlertPolicy(PROJECT, "policy-123");
        assertNotNull(result);
        assertEquals(true, result.get("enabled"));
    }

    @Test
    public void testListAlertPolicies_returnsNonEmpty() {
        List<Map<String, Object>> policies = service.listAlertPolicies(PROJECT);
        assertNotNull(policies);
        assertFalse("Expected sample alert policies", policies.isEmpty());
    }

    @Test
    public void testListAlertPolicies_containsRequiredFields() {
        List<Map<String, Object>> policies = service.listAlertPolicies(PROJECT);
        for (Map<String, Object> p : policies) {
            assertNotNull(p.get("displayname"));
            assertNotNull(p.get("enabled"));
            assertNotNull(p.get("conditionfilter"));
            assertNotNull(p.get("thresholdvalue"));
        }
    }

    @Test
    public void testDeleteAlertPolicy_returnsTrue() {
        assertTrue(service.deleteAlertPolicy(PROJECT, "policy-123"));
    }

    @Test
    public void testEnableAlertPolicy_returnsTrue() {
        assertTrue(service.enableAlertPolicy(PROJECT, "policy-123", true));
    }

    // ==================== Cloud Logging ====================

    @Test
    public void testListLogEntries_returnsNonEmpty() {
        List<Map<String, Object>> entries = service.listLogEntries(PROJECT, null, null, 50);
        assertNotNull(entries);
        assertFalse("Expected sample log entries", entries.isEmpty());
    }

    @Test
    public void testListLogEntries_containsRequiredFields() {
        List<Map<String, Object>> entries = service.listLogEntries(PROJECT, null, null, 10);
        for (Map<String, Object> e : entries) {
            assertNotNull(e.get("logname"));
            assertNotNull(e.get("severity"));
            assertNotNull(e.get("timestamp"));
            assertNotNull(e.get("message"));
        }
    }

    @Test
    public void testListLogEntries_hasErrorEntry() {
        List<Map<String, Object>> entries = service.listLogEntries(PROJECT, null, null, 10);
        boolean hasError = entries.stream().anyMatch(e -> "ERROR".equals(e.get("severity")));
        assertTrue("Expected at least one ERROR log entry", hasError);
    }

    @Test
    public void testWriteLogEntry_returnsTrue() {
        assertTrue(service.writeLogEntry(PROJECT, "projects/myproject/logs/app", "INFO", "App started", null));
    }

    @Test
    public void testCreateLogSink_returnsSink() {
        Map<String, Object> result = service.createLogSink(PROJECT, "bq-sink",
                "bigquery.googleapis.com/projects/myproject/datasets/logs_ds", null);
        assertNotNull(result);
        assertEquals("bq-sink", result.get("name"));
    }
}
