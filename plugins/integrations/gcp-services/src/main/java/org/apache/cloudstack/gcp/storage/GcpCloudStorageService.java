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

/**
 * Service interface for GCP Cloud Storage operations.
 * Manages buckets, objects, and storage lifecycle policies.
 */
public interface GcpCloudStorageService {

    // --- Bucket Management ---
    Map<String, Object> createBucket(String projectId, String bucketName, String location,
                                      String storageClass, boolean versioningEnabled);

    Map<String, Object> getBucket(String bucketName);

    List<Map<String, Object>> listBuckets(String projectId);

    boolean deleteBucket(String bucketName);

    Map<String, Object> updateBucketLabels(String bucketName, Map<String, String> labels);

    // --- Object Management ---
    Map<String, Object> uploadObject(String bucketName, String objectName,
                                      byte[] content, String contentType);

    byte[] downloadObject(String bucketName, String objectName);

    Map<String, Object> getObjectMetadata(String bucketName, String objectName);

    List<Map<String, Object>> listObjects(String bucketName, String prefix, int maxResults);

    boolean deleteObject(String bucketName, String objectName);

    boolean copyObject(String sourceBucket, String sourceObject,
                       String destBucket, String destObject);

    boolean moveObject(String sourceBucket, String sourceObject,
                       String destBucket, String destObject);

    // --- Lifecycle Management ---
    boolean setLifecyclePolicy(String bucketName, List<Map<String, Object>> rules);

    List<Map<String, Object>> getLifecyclePolicy(String bucketName);

    // --- Access Control ---
    boolean setBucketIamPolicy(String bucketName, String member, String role);

    List<Map<String, Object>> getBucketIamPolicy(String bucketName);

    // --- Signed URLs ---
    String generateSignedUrl(String bucketName, String objectName,
                              long expirationMinutes, String httpMethod);

    // --- Storage Transfer ---
    Map<String, Object> createTransferJob(String projectId, String sourceBucket,
                                           String destinationBucket, String description);

    List<Map<String, Object>> listTransferJobs(String projectId);
}
