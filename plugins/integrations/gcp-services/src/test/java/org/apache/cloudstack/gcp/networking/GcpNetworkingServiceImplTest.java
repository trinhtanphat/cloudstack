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

package org.apache.cloudstack.gcp.networking;

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
public class GcpNetworkingServiceImplTest {

    @InjectMocks
    private GcpNetworkingServiceImpl service;

    private static final String PROJECT = "my-gcp-project";
    private static final String REGION = "us-central1";

    // ==================== VPC Networks ====================

    @Test
    public void testCreateVpcNetwork_returnsCreating() {
        Map<String, Object> result = service.createVpcNetwork(PROJECT, "prod-vpc", false, "GLOBAL");
        assertNotNull(result);
        assertEquals("prod-vpc", result.get("name"));
        assertEquals("GLOBAL", result.get("routingMode"));
        assertEquals(false, result.get("autoCreateSubnetworks"));
        assertEquals("CREATING", result.get("status"));
    }

    @Test
    public void testGetVpcNetwork_returnsActive() {
        Map<String, Object> result = service.getVpcNetwork(PROJECT, "default");
        assertNotNull(result);
        assertEquals("ACTIVE", result.get("status"));
    }

    @Test
    public void testListVpcNetworks_returnsNonEmpty() {
        List<Map<String, Object>> networks = service.listVpcNetworks(PROJECT);
        assertNotNull(networks);
        assertFalse("Expected sample VPC networks", networks.isEmpty());
    }

    @Test
    public void testListVpcNetworks_containsRequiredFields() {
        List<Map<String, Object>> networks = service.listVpcNetworks(PROJECT);
        for (Map<String, Object> n : networks) {
            assertNotNull(n.get("name"));
            assertNotNull(n.get("routingmode"));
            assertNotNull(n.get("autocreatesubnetworks"));
            assertNotNull(n.get("status"));
        }
    }

    @Test
    public void testListVpcNetworks_containsDefaultNetwork() {
        List<Map<String, Object>> networks = service.listVpcNetworks(PROJECT);
        boolean hasDefault = networks.stream().anyMatch(n -> "default".equals(n.get("name")));
        assertTrue("Expected 'default' VPC network", hasDefault);
    }

    @Test
    public void testDeleteVpcNetwork_returnsTrue() {
        assertTrue(service.deleteVpcNetwork(PROJECT, "old-vpc"));
    }

    // ==================== Subnets ====================

    @Test
    public void testCreateSubnet_returnsSubnet() {
        Map<String, Object> result = service.createSubnet(PROJECT, REGION, "app-subnet",
                "prod-vpc", "10.100.0.0/24", true);
        assertNotNull(result);
        assertEquals("app-subnet", result.get("name"));
        assertEquals(REGION, result.get("region"));
        assertEquals("10.100.0.0/24", result.get("ipCidrRange"));
    }

    @Test
    public void testGetSubnet_returnsSubnet() {
        Map<String, Object> result = service.getSubnet(PROJECT, REGION, "app-subnet");
        assertNotNull(result);
        assertEquals("app-subnet", result.get("name"));
    }

    @Test
    public void testDeleteSubnet_returnsTrue() {
        assertTrue(service.deleteSubnet(PROJECT, REGION, "old-subnet"));
    }

    @Test
    public void testExpandSubnetRange_returnsTrue() {
        assertTrue(service.expandSubnetRange(PROJECT, REGION, "app-subnet", "10.100.0.0/20"));
    }
}
