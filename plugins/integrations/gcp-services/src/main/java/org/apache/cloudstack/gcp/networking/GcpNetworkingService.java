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

/**
 * Service interface for GCP Networking operations.
 * Manages VPC networks, subnets, firewalls, load balancers, VPN, Cloud DNS, and Cloud NAT.
 */
public interface GcpNetworkingService {

    // ==================== VPC Networks ====================
    Map<String, Object> createVpcNetwork(String projectId, String networkName,
                                          boolean autoCreateSubnetworks, String routingMode);

    Map<String, Object> getVpcNetwork(String projectId, String networkName);

    List<Map<String, Object>> listVpcNetworks(String projectId);

    boolean deleteVpcNetwork(String projectId, String networkName);

    // ==================== Subnets ====================
    Map<String, Object> createSubnet(String projectId, String region, String subnetName,
                                      String networkName, String ipCidrRange,
                                      boolean privateIpGoogleAccess);

    Map<String, Object> getSubnet(String projectId, String region, String subnetName);

    List<Map<String, Object>> listSubnets(String projectId, String region);

    boolean deleteSubnet(String projectId, String region, String subnetName);

    boolean expandSubnetRange(String projectId, String region, String subnetName,
                               String newIpCidrRange);

    // ==================== Firewall Rules ====================
    Map<String, Object> createFirewallRule(String projectId, String ruleName, String networkName,
                                            String direction, String priority,
                                            List<String> sourceRanges, List<String> targetTags,
                                            List<Map<String, Object>> allowed);

    Map<String, Object> getFirewallRule(String projectId, String ruleName);

    List<Map<String, Object>> listFirewallRules(String projectId);

    boolean deleteFirewallRule(String projectId, String ruleName);

    boolean updateFirewallRule(String projectId, String ruleName, Map<String, Object> updates);

    // ==================== Load Balancers ====================
    Map<String, Object> createHttpLoadBalancer(String projectId, String lbName,
                                                Map<String, Object> backendConfig,
                                                Map<String, Object> frontendConfig);

    Map<String, Object> createTcpLoadBalancer(String projectId, String lbName,
                                               String region, Map<String, Object> config);

    List<Map<String, Object>> listLoadBalancers(String projectId);

    boolean deleteLoadBalancer(String projectId, String lbName);

    // ==================== External IP Addresses ====================
    Map<String, Object> reserveStaticIp(String projectId, String region, String ipName);

    Map<String, Object> reserveGlobalStaticIp(String projectId, String ipName);

    List<Map<String, Object>> listStaticIps(String projectId, String region);

    boolean releaseStaticIp(String projectId, String region, String ipName);

    // ==================== Cloud VPN ====================
    Map<String, Object> createVpnGateway(String projectId, String region, String gatewayName,
                                          String networkName);

    Map<String, Object> createVpnTunnel(String projectId, String region, String tunnelName,
                                          String gatewayName, String peerIp,
                                          String sharedSecret, String ikeVersion);

    List<Map<String, Object>> listVpnTunnels(String projectId, String region);

    boolean deleteVpnTunnel(String projectId, String region, String tunnelName);

    // ==================== Cloud DNS ====================
    Map<String, Object> createDnsZone(String projectId, String zoneName, String dnsName,
                                       String description);

    List<Map<String, Object>> listDnsZones(String projectId);

    boolean deleteDnsZone(String projectId, String zoneName);

    Map<String, Object> createDnsRecord(String projectId, String zoneName, String recordName,
                                          String recordType, int ttl, List<String> rrdatas);

    List<Map<String, Object>> listDnsRecords(String projectId, String zoneName);

    boolean deleteDnsRecord(String projectId, String zoneName, String recordName, String recordType);

    // ==================== Cloud NAT ====================
    Map<String, Object> createCloudNat(String projectId, String region, String routerName,
                                        String natName, String networkName);

    List<Map<String, Object>> listCloudNats(String projectId, String region);

    boolean deleteCloudNat(String projectId, String region, String routerName, String natName);

    // ==================== Cloud Router ====================
    Map<String, Object> createCloudRouter(String projectId, String region, String routerName,
                                           String networkName, long asn);

    List<Map<String, Object>> listCloudRouters(String projectId, String region);

    boolean deleteCloudRouter(String projectId, String region, String routerName);
}
