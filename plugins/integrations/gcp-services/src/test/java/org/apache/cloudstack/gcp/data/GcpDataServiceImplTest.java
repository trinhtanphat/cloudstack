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

package org.apache.cloudstack.gcp.data;

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
public class GcpDataServiceImplTest {

    @InjectMocks
    private GcpDataServiceImpl service;

    private static final String PROJECT = "my-gcp-project";
    private static final String LOCATION = "us-central1";

    // ==================== Datastream ====================

    @Test
    public void testCreateStream_returnsRunning() {
        Map<String, Object> result = service.createStream(PROJECT, LOCATION, "test-stream",
            "Test Stream", "source-profile", "dest-profile",
            new java.util.HashMap<>(), new java.util.HashMap<>());
        assertNotNull(result);
        assertEquals("test-stream", result.get("name"));
        assertEquals("NOT_STARTED", result.get("state"));
    }

    @Test
    public void testListStreams_returnsNonEmpty() {
        List<Map<String, Object>> streams = service.listStreams(PROJECT, LOCATION);
        assertNotNull(streams);
        assertFalse("Expected sample streams", streams.isEmpty());
    }

    @Test
    public void testListStreams_containsRequiredFields() {
        List<Map<String, Object>> streams = service.listStreams(PROJECT, LOCATION);
        Map<String, Object> s = streams.get(0);
        assertNotNull(s.get("name"));
        assertNotNull(s.get("state"));
        assertNotNull(s.get("sourceprofile"));
        assertNotNull(s.get("destprofile"));
    }

    @Test
    public void testStartStream_returnsTrue() {
        assertTrue(service.startStream(PROJECT, LOCATION, "mysql-to-bq"));
    }

    @Test
    public void testPauseStream_returnsTrue() {
        assertTrue(service.pauseStream(PROJECT, LOCATION, "mysql-to-bq"));
    }

    @Test
    public void testDeleteStream_returnsTrue() {
        assertTrue(service.deleteStream(PROJECT, LOCATION, "old-stream"));
    }

    // ==================== Dataplex ====================

    @Test
    public void testCreateLake_returnsCreating() {
        Map<String, Object> result = service.createLake(PROJECT, LOCATION, "test-lake", "Test Lake", "Test description");
        assertNotNull(result);
        assertEquals("test-lake", result.get("name"));
        assertEquals("CREATING", result.get("state"));
    }

    @Test
    public void testListLakes_returnsNonEmpty() {
        List<Map<String, Object>> lakes = service.listLakes(PROJECT, LOCATION);
        assertNotNull(lakes);
        assertFalse("Expected sample lakes", lakes.isEmpty());
    }

    @Test
    public void testListLakes_containsRequiredFields() {
        List<Map<String, Object>> lakes = service.listLakes(PROJECT, LOCATION);
        Map<String, Object> l = lakes.get(0);
        assertNotNull(l.get("name"));
        assertNotNull(l.get("state"));
        assertNotNull(l.get("location"));
    }

    @Test
    public void testDeleteLake_returnsTrue() {
        assertTrue(service.deleteLake(PROJECT, LOCATION, "old-lake"));
    }

    // ==================== Cloud Composer ====================

    @Test
    public void testCreateComposerEnvironment_returnsCreating() {
        Map<String, Object> result = service.createComposerEnvironment(PROJECT, LOCATION,
                "test-env", "composer-2.6.6-airflow-2.7.3", null, null);
        assertNotNull(result);
        assertEquals("test-env", result.get("name"));
        assertEquals("CREATING", result.get("state"));
    }

    @Test
    public void testGetComposerEnvironment_returnsRunning() {
        Map<String, Object> result = service.getComposerEnvironment(PROJECT, LOCATION, "data-pipeline-env");
        assertNotNull(result);
        assertEquals("RUNNING", result.get("state"));
    }

    @Test
    public void testListComposerEnvironments_returnsNonEmpty() {
        List<Map<String, Object>> envs = service.listComposerEnvironments(PROJECT, LOCATION);
        assertNotNull(envs);
        assertFalse("Expected sample Composer environments", envs.isEmpty());
    }

    @Test
    public void testListComposerEnvironments_containsRequiredFields() {
        List<Map<String, Object>> envs = service.listComposerEnvironments(PROJECT, LOCATION);
        Map<String, Object> e = envs.get(0);
        assertNotNull(e.get("name"));
        assertNotNull(e.get("state"));
        assertNotNull(e.get("location"));
        assertNotNull(e.get("imageversion"));
    }

    @Test
    public void testDeleteComposerEnvironment_returnsTrue() {
        assertTrue(service.deleteComposerEnvironment(PROJECT, LOCATION, "old-env"));
    }
}
