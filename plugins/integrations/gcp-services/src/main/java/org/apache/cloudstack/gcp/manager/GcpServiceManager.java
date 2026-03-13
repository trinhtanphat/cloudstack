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
import org.apache.cloudstack.gcp.GcpServiceType;

/**
 * Central manager interface for all GCP services.
 * Handles authentication, project management, and orchestration across service types.
 */
public interface GcpServiceManager {

    boolean configure(String name, Map<String, Object> params);

    boolean start();

    boolean stop();

    // --- Authentication & Project ---
    boolean validateCredentials(String credentialsJson);

    Map<String, Object> getProjectInfo(String projectId);

    List<Map<String, Object>> listProjects();

    List<String> listRegions(String projectId);

    List<String> listZones(String projectId, String region);

    // --- Service Status ---
    Map<String, Object> getServiceStatus(String projectId, GcpServiceType serviceType);

    boolean enableService(String projectId, GcpServiceType serviceType);

    boolean disableService(String projectId, GcpServiceType serviceType);

    List<Map<String, Object>> listEnabledServices(String projectId);

    // --- Billing & Quotas ---
    Map<String, Object> getBillingInfo(String projectId);

    Map<String, Object> getQuotas(String projectId, String region);

    List<Map<String, Object>> getCostBreakdown(String projectId, String startDate, String endDate);
}
