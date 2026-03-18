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

package org.apache.cloudstack.gcp.infrastructure;

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
public class GcpInfrastructureServiceImplTest {

    @InjectMocks
    private GcpInfrastructureServiceImpl service;

    private static final String PROJECT = "my-gcp-project";
    private static final String LOCATION = "us-central1";

    // ==================== Secret Manager ====================

    @Test
    public void testCreateSecret_returnsPopulatedMap() {
        Map<String, Object> result = service.createSecret(PROJECT, "app-secret", null, "AUTOMATIC");
        assertNotNull(result);
        assertEquals("app-secret", result.get("name"));
        assertEquals("AUTOMATIC", result.get("replication"));
        assertNotNull(result.get("createTime"));
    }

    @Test
    public void testGetSecret_returnsSecret() {
        Map<String, Object> result = service.getSecret(PROJECT, "db-password");
        assertNotNull(result);
        assertEquals("db-password", result.get("name"));
    }

    @Test
    public void testListSecrets_returnsNonEmpty() {
        List<Map<String, Object>> secrets = service.listSecrets(PROJECT);
        assertNotNull(secrets);
        assertFalse("Expected sample secrets", secrets.isEmpty());
    }

    @Test
    public void testListSecrets_containsRequiredFields() {
        List<Map<String, Object>> secrets = service.listSecrets(PROJECT);
        for (Map<String, Object> s : secrets) {
            assertNotNull(s.get("name"));
            assertNotNull(s.get("replication"));
            assertNotNull(s.get("createtime"));
        }
    }

    @Test
    public void testDeleteSecret_returnsTrue() {
        assertTrue(service.deleteSecret(PROJECT, "old-secret"));
    }

    @Test
    public void testAddSecretVersion_returnsEnabled() {
        Map<String, Object> result = service.addSecretVersion(PROJECT, "app-secret", "mypassword123".getBytes());
        assertNotNull(result);
        assertEquals("ENABLED", result.get("state"));
    }

    // ==================== Artifact Registry ====================

    @Test
    public void testCreateRepository_returnsRepo() {
        Map<String, Object> result = service.createRepository(PROJECT, LOCATION,
                "docker-repo", "DOCKER", "Docker images");
        assertNotNull(result);
        assertEquals("docker-repo", result.get("name"));
        assertEquals("DOCKER", result.get("format"));
        assertEquals(LOCATION, result.get("location"));
    }

    @Test
    public void testGetRepository_returnsRepo() {
        Map<String, Object> result = service.getRepository(PROJECT, LOCATION, "docker-registry");
        assertNotNull(result);
        assertEquals("docker-registry", result.get("name"));
    }

    @Test
    public void testListRepositories_returnsNonEmpty() {
        List<Map<String, Object>> repos = service.listRepositories(PROJECT, LOCATION);
        assertNotNull(repos);
        assertFalse("Expected sample repositories", repos.isEmpty());
    }

    @Test
    public void testListRepositories_containsRequiredFields() {
        List<Map<String, Object>> repos = service.listRepositories(PROJECT, LOCATION);
        Map<String, Object> r = repos.get(0);
        assertNotNull(r.get("name"));
        assertNotNull(r.get("format"));
        assertNotNull(r.get("location"));
    }

    @Test
    public void testDeleteRepository_returnsTrue() {
        assertTrue(service.deleteRepository(PROJECT, LOCATION, "old-repo"));
    }

    // ==================== Cloud Memorystore for Redis ====================

    @Test
    public void testCreateRedisInstance_returnsCreating() {
        Map<String, Object> result = service.createRedisInstance(PROJECT, LOCATION,
                "cache-test", "STANDARD_HA", 2, "REDIS_7_0", "default", "DIRECT_PEERING");
        assertNotNull(result);
        assertEquals("cache-test", result.get("name"));
        assertEquals("CREATING", result.get("state"));
        assertEquals("STANDARD_HA", result.get("tier"));
        assertEquals(2, result.get("memorySizeGb"));
    }

    @Test
    public void testGetRedisInstance_returnsReady() {
        Map<String, Object> result = service.getRedisInstance(PROJECT, LOCATION, "cache-prod");
        assertNotNull(result);
        assertEquals("READY", result.get("state"));
    }

    @Test
    public void testListRedisInstances_returnsNonEmpty() {
        List<Map<String, Object>> instances = service.listRedisInstances(PROJECT, LOCATION);
        assertNotNull(instances);
        assertFalse("Expected sample Redis instances", instances.isEmpty());
    }

    @Test
    public void testListRedisInstances_containsRequiredFields() {
        List<Map<String, Object>> instances = service.listRedisInstances(PROJECT, LOCATION);
        Map<String, Object> inst = instances.get(0);
        assertNotNull(inst.get("name"));
        assertNotNull(inst.get("state"));
        assertNotNull(inst.get("tier"));
        assertNotNull(inst.get("memorysizegb"));
        assertNotNull(inst.get("redisversion"));
        assertNotNull(inst.get("location"));
    }

    @Test
    public void testDeleteRedisInstance_returnsTrue() {
        assertTrue(service.deleteRedisInstance(PROJECT, LOCATION, "old-cache"));
    }
}
