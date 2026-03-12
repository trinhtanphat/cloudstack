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

package org.apache.cloudstack.gcp;

/**
 * Enumeration of all supported GCP service types in CloudStack.
 */
public enum GcpServiceType {
    COMPUTE_ENGINE("compute-engine", "Compute Engine"),
    CLOUD_STORAGE("cloud-storage", "Cloud Storage"),
    ALLOY_DB("alloydb", "AlloyDB"),
    BIGQUERY("bigquery", "BigQuery"),
    BIGQUERY_RESERVATION("bigquery-reservation", "BigQuery Reservation API"),
    CLOUD_SQL("cloud-sql", "Cloud SQL"),
    CLOUD_SPANNER("cloud-spanner", "Cloud Spanner"),
    NETWORKING("networking", "Networking"),
    KUBERNETES_ENGINE("kubernetes-engine", "Kubernetes Engine"),
    CLOUD_RUN("cloud-run", "Cloud Run"),
    CLOUD_MONITORING("cloud-monitoring", "Cloud Monitoring"),
    CLOUD_LOGGING("cloud-logging", "Cloud Logging"),
    ARTIFACT_REGISTRY("artifact-registry", "Artifact Registry"),
    VERTEX_AI("vertex-ai", "Vertex AI"),
    GEMINI_API("gemini-api", "Gemini API"),
    NOTEBOOKS("notebooks", "Notebooks"),
    CLOUD_MEMORYSTORE("cloud-memorystore", "Cloud Memorystore for Redis"),
    DATASTREAM("datastream", "Datastream"),
    DATAPLEX("dataplex", "Dataplex"),
    CLOUD_COMPOSER("cloud-composer", "Cloud Composer"),
    BACKUP_FOR_GKE("backup-for-gke", "Backup for GKE"),
    SECRET_MANAGER("secret-manager", "Secret Manager");

    private final String id;
    private final String displayName;

    GcpServiceType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
