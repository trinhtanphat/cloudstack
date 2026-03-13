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

import java.util.List;
import java.util.Map;

/**
 * Service interface for GCP Compute Engine operations.
 * Manages virtual machine instances, instance templates, instance groups,
 * machine images, and related compute resources.
 */
public interface GcpComputeEngineService {

    // --- Instance Management ---
    Map<String, Object> createInstance(String projectId, String zone, String instanceName,
                                       String machineType, String imageFamily, String imageProject,
                                       String networkName, Map<String, String> labels,
                                       Map<String, String> metadata);

    Map<String, Object> getInstance(String projectId, String zone, String instanceName);

    List<Map<String, Object>> listInstances(String projectId, String zone, String filter);

    boolean startInstance(String projectId, String zone, String instanceName);

    boolean stopInstance(String projectId, String zone, String instanceName);

    boolean deleteInstance(String projectId, String zone, String instanceName);

    boolean resetInstance(String projectId, String zone, String instanceName);

    Map<String, Object> resizeInstance(String projectId, String zone, String instanceName,
                                       String newMachineType);

    // --- Instance Templates ---
    Map<String, Object> createInstanceTemplate(String projectId, String templateName,
                                                String machineType, String sourceImage,
                                                Map<String, String> labels);

    List<Map<String, Object>> listInstanceTemplates(String projectId);

    boolean deleteInstanceTemplate(String projectId, String templateName);

    // --- Instance Groups ---
    Map<String, Object> createInstanceGroup(String projectId, String zone, String groupName,
                                             String templateName, int targetSize);

    Map<String, Object> getInstanceGroup(String projectId, String zone, String groupName);

    boolean resizeInstanceGroup(String projectId, String zone, String groupName, int newSize);

    boolean deleteInstanceGroup(String projectId, String zone, String groupName);

    // --- Machine Types ---
    List<Map<String, Object>> listMachineTypes(String projectId, String zone);

    // --- Disks ---
    Map<String, Object> createDisk(String projectId, String zone, String diskName,
                                    String diskType, long sizeGb);

    Map<String, Object> getDisk(String projectId, String zone, String diskName);

    boolean attachDisk(String projectId, String zone, String instanceName, String diskName);

    boolean detachDisk(String projectId, String zone, String instanceName, String diskName);

    boolean deleteDisk(String projectId, String zone, String diskName);

    boolean resizeDisk(String projectId, String zone, String diskName, long newSizeGb);

    // --- Snapshots ---
    Map<String, Object> createSnapshot(String projectId, String zone, String diskName,
                                        String snapshotName);

    List<Map<String, Object>> listSnapshots(String projectId);

    boolean deleteSnapshot(String projectId, String snapshotName);

    // --- Images ---
    List<Map<String, Object>> listImages(String projectId);

    Map<String, Object> createImage(String projectId, String imageName, String sourceDisk,
                                     String sourceZone);

    boolean deleteImage(String projectId, String imageName);
}
