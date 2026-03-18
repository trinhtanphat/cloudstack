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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GcpDatabaseServiceImplTest {

    @InjectMocks
    private GcpDatabaseServiceImpl service;

    private static final String PROJECT = "my-gcp-project";

    // ==================== AlloyDB ====================

    @Test
    public void testCreateAlloyDbCluster_returnsCreatingState() {
        Map<String, Object> result = service.createAlloyDbCluster(PROJECT, "us-central1", "prod-cluster", "secret123", "default");
        assertNotNull(result);
        assertEquals("prod-cluster", result.get("name"));
        assertEquals("CREATING", result.get("state"));
        assertEquals("us-central1", result.get("region"));
    }

    @Test
    public void testGetAlloyDbCluster_returnsReady() {
        Map<String, Object> result = service.getAlloyDbCluster(PROJECT, "us-central1", "prod-cluster");
        assertEquals("READY", result.get("state"));
    }

    @Test
    public void testListAlloyDbClusters_returnsNonEmpty() {
        List<Map<String, Object>> clusters = service.listAlloyDbClusters(PROJECT, "us-central1");
        assertNotNull(clusters);
        assertFalse("Expected sample clusters", clusters.isEmpty());
    }

    @Test
    public void testListAlloyDbClusters_containsRequiredFields() {
        List<Map<String, Object>> clusters = service.listAlloyDbClusters(PROJECT, "us-central1");
        for (Map<String, Object> c : clusters) {
            assertNotNull(c.get("name"));
            assertNotNull(c.get("state"));
            assertNotNull(c.get("region"));
            assertNotNull(c.get("network"));
        }
    }

    @Test
    public void testDeleteAlloyDbCluster_returnsTrue() {
        assertTrue(service.deleteAlloyDbCluster(PROJECT, "us-central1", "old-cluster"));
    }

    // ==================== Cloud SQL ====================

    @Test
    public void testCreateCloudSqlInstance_returnsPendingCreate() {
        Map<String, Object> result = service.createCloudSqlInstance(PROJECT, "mysql-test", "MYSQL_8_0", "db-n1-standard-2", "us-central1", 20L);
        assertNotNull(result);
        assertEquals("mysql-test", result.get("name"));
        assertEquals("MYSQL_8_0", result.get("databaseVersion"));
        assertEquals("PENDING_CREATE", result.get("state"));
    }

    @Test
    public void testGetCloudSqlInstance_returnsRunnable() {
        Map<String, Object> result = service.getCloudSqlInstance(PROJECT, "mysql-prod");
        assertEquals("RUNNABLE", result.get("state"));
    }

    @Test
    public void testListCloudSqlInstances_returnsNonEmpty() {
        List<Map<String, Object>> instances = service.listCloudSqlInstances(PROJECT);
        assertNotNull(instances);
        assertFalse("Expected sample instances", instances.isEmpty());
    }

    @Test
    public void testListCloudSqlInstances_containsRequiredFields() {
        List<Map<String, Object>> instances = service.listCloudSqlInstances(PROJECT);
        for (Map<String, Object> inst : instances) {
            assertNotNull(inst.get("name"));
            assertNotNull(inst.get("state"));
            assertNotNull(inst.get("databaseversion"));
            assertNotNull(inst.get("tier"));
            assertNotNull(inst.get("region"));
        }
    }

    @Test
    public void testDeleteCloudSqlInstance_returnsTrue() {
        assertTrue(service.deleteCloudSqlInstance(PROJECT, "old-instance"));
    }

    @Test
    public void testRestartCloudSqlInstance_returnsTrue() {
        assertTrue(service.restartCloudSqlInstance(PROJECT, "mysql-prod"));
    }

    // ==================== Cloud Spanner ====================

    @Test
    public void testCreateSpannerInstance_returnsCreating() {
        Map<String, Object> result = service.createSpannerInstance(PROJECT, "spanner-test", "Test Spanner", "regional-us-central1", 1);
        assertNotNull(result);
        assertEquals("spanner-test", result.get("name"));
        assertEquals("CREATING", result.get("state"));
    }

    @Test
    public void testGetSpannerInstance_returnsReady() {
        Map<String, Object> result = service.getSpannerInstance(PROJECT, "spanner-main");
        assertEquals("READY", result.get("state"));
    }

    @Test
    public void testListSpannerInstances_returnsNonEmpty() {
        List<Map<String, Object>> instances = service.listSpannerInstances(PROJECT);
        assertNotNull(instances);
        assertFalse("Expected sample Spanner instances", instances.isEmpty());
    }

    @Test
    public void testListSpannerInstances_containsRequiredFields() {
        List<Map<String, Object>> instances = service.listSpannerInstances(PROJECT);
        Map<String, Object> inst = instances.get(0);
        assertNotNull(inst.get("name"));
        assertNotNull(inst.get("state"));
        assertNotNull(inst.get("config"));
        assertNotNull(inst.get("nodecount"));
    }

    @Test
    public void testDeleteSpannerInstance_returnsTrue() {
        assertTrue(service.deleteSpannerInstance(PROJECT, "old-spanner"));
    }

    // ==================== BigQuery ====================

    @Test
    public void testCreateBigQueryDataset_returnsDataset() {
        Map<String, Object> result = service.createBigQueryDataset(PROJECT, "test_ds", "US", "Test dataset");
        assertNotNull(result);
        assertEquals("test_ds", result.get("datasetId"));
        assertEquals("US", result.get("location"));
    }

    @Test
    public void testListBigQueryDatasets_returnsNonEmpty() {
        List<Map<String, Object>> datasets = service.listBigQueryDatasets(PROJECT);
        assertNotNull(datasets);
        assertFalse("Expected sample datasets", datasets.isEmpty());
    }

    @Test
    public void testListBigQueryDatasets_containsRequiredFields() {
        List<Map<String, Object>> datasets = service.listBigQueryDatasets(PROJECT);
        for (Map<String, Object> ds : datasets) {
            assertNotNull(ds.get("datasetid"));
            assertNotNull(ds.get("location"));
        }
    }

    @Test
    public void testDeleteBigQueryDataset_returnsTrue() {
        assertTrue(service.deleteBigQueryDataset(PROJECT, "old_ds", true));
    }

    // ==================== BigQuery Reservations ====================

    @Test
    public void testCreateBigQueryReservation_returnsReservation() {
        Map<String, Object> result = service.createBigQueryReservation(PROJECT, "US", "test-reservation", 100L);
        assertNotNull(result);
        assertEquals("test-reservation", result.get("name"));
        assertEquals(100L, result.get("slotCapacity"));
        assertEquals("US", result.get("location"));
    }

    @Test
    public void testListBigQueryReservations_returnsNonEmpty() {
        List<Map<String, Object>> reservations = service.listBigQueryReservations(PROJECT, "US");
        assertNotNull(reservations);
        assertFalse("Expected sample reservations", reservations.isEmpty());
    }

    @Test
    public void testListBigQueryReservations_containsRequiredFields() {
        List<Map<String, Object>> reservations = service.listBigQueryReservations(PROJECT, "US");
        Map<String, Object> r = reservations.get(0);
        assertNotNull(r.get("name"));
        assertNotNull(r.get("slotcapacity"));
        assertNotNull(r.get("location"));
    }

    @Test
    public void testDeleteBigQueryReservation_returnsTrue() {
        assertTrue(service.deleteBigQueryReservation(PROJECT, "US", "old-reservation"));
    }
}
