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

package org.apache.cloudstack.gcp.storage;

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
public class GcpCloudStorageServiceImplTest {

    @InjectMocks
    private GcpCloudStorageServiceImpl service;

    private static final String PROJECT = "my-gcp-project";

    // ==================== createBucket ====================

    @Test
    public void testCreateBucket_returnsPopulatedMap() {
        Map<String, Object> result = service.createBucket(PROJECT, "test-bucket", "US", "STANDARD", false);
        assertNotNull(result);
        assertEquals("test-bucket", result.get("name"));
        assertEquals("US", result.get("location"));
        assertEquals("STANDARD", result.get("storageClass"));
        assertEquals(false, result.get("versioningEnabled"));
        assertNotNull(result.get("timeCreated"));
    }

    @Test
    public void testCreateBucket_withVersioningEnabled() {
        Map<String, Object> result = service.createBucket(PROJECT, "versioned-bucket", "EU", "NEARLINE", true);
        assertEquals(true, result.get("versioningEnabled"));
        assertEquals("NEARLINE", result.get("storageClass"));
    }

    // ==================== getBucket ====================

    @Test
    public void testGetBucket_returnsActiveStatus() {
        Map<String, Object> result = service.getBucket("my-assets");
        assertNotNull(result);
        assertEquals("my-assets", result.get("name"));
        assertEquals("ACTIVE", result.get("status"));
    }

    // ==================== listBuckets ====================

    @Test
    public void testListBuckets_returnsNonEmptyList() {
        List<Map<String, Object>> buckets = service.listBuckets(PROJECT);
        assertNotNull(buckets);
        assertFalse("Expected sample buckets to be returned", buckets.isEmpty());
    }

    @Test
    public void testListBuckets_containsRequiredFields() {
        List<Map<String, Object>> buckets = service.listBuckets(PROJECT);
        for (Map<String, Object> bucket : buckets) {
            assertNotNull("name must be present", bucket.get("name"));
            assertNotNull("location must be present", bucket.get("location"));
            assertNotNull("storageclass must be present", bucket.get("storageclass"));
        }
    }

    @Test
    public void testListBuckets_hasStandardStorageClass() {
        List<Map<String, Object>> buckets = service.listBuckets(PROJECT);
        boolean hasStandard = buckets.stream().anyMatch(b -> "STANDARD".equals(b.get("storageclass")));
        assertTrue("At least one STANDARD bucket expected", hasStandard);
    }

    // ==================== deleteBucket ====================

    @Test
    public void testDeleteBucket_returnsTrue() {
        assertTrue(service.deleteBucket("old-bucket"));
    }

    // ==================== updateBucketLabels ====================

    @Test
    public void testUpdateBucketLabels_returnsUpdatedBucket() {
        java.util.Map<String, String> labels = new java.util.HashMap<>();
        labels.put("env", "prod");
        Map<String, Object> result = service.updateBucketLabels("my-bucket", labels);
        assertNotNull(result);
        assertEquals("my-bucket", result.get("name"));
        assertEquals(labels, result.get("labels"));
    }
}
