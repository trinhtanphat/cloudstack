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

import { shallowRef, defineAsyncComponent } from 'vue'

export default {
  name: 'gcp',
  title: 'Google Cloud Services',
  icon: 'cloud-server-outlined',
  permission: ['listGcpServices'],
  children: [
    {
      name: 'gcpcomputeengine',
      title: 'Compute Engine',
      icon: 'desktop-outlined',
      permission: ['listGcpInstances'],
      columns: ['name', 'state', 'machinetype', 'zone', 'internalip', 'externalip'],
      details: ['name', 'id', 'state', 'machinetype', 'zone', 'region', 'internalip', 'externalip', 'networkname', 'created'],
      searchFilters: ['name', 'state', 'zone'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpComputeEngineTab.vue'))
      }],
      actions: [
        {
          api: 'createGcpInstance',
          icon: 'plus-outlined',
          label: 'Create Instance',
          listView: true,
          component: defineAsyncComponent(() => import('@/views/gcp/CreateGcpInstance.vue'))
        },
        {
          api: 'startGcpInstance',
          icon: 'caret-right-outlined',
          label: 'Start',
          show: (record) => record.state === 'STOPPED'
        },
        {
          api: 'stopGcpInstance',
          icon: 'pause-outlined',
          label: 'Stop',
          show: (record) => record.state === 'RUNNING'
        },
        {
          api: 'deleteGcpInstance',
          icon: 'delete-outlined',
          label: 'Delete',
          dataView: true
        }
      ]
    },
    {
      name: 'gcpcloudstorage',
      title: 'Cloud Storage',
      icon: 'database-outlined',
      permission: ['listGcpBuckets'],
      columns: ['name', 'location', 'storageclass', 'versioningenabled', 'created'],
      details: ['name', 'location', 'storageclass', 'versioningenabled', 'created', 'labels'],
      searchFilters: ['name', 'location'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpCloudStorageTab.vue'))
      }],
      actions: [
        {
          api: 'createGcpBucket',
          icon: 'plus-outlined',
          label: 'Create Bucket',
          listView: true,
          component: defineAsyncComponent(() => import('@/views/gcp/CreateGcpBucket.vue'))
        },
        {
          api: 'deleteGcpBucket',
          icon: 'delete-outlined',
          label: 'Delete',
          dataView: true
        }
      ]
    },
    {
      name: 'gcpcloudsql',
      title: 'Cloud SQL',
      icon: 'container-outlined',
      permission: ['listGcpCloudSqlInstances'],
      columns: ['name', 'state', 'databaseversion', 'tier', 'region', 'storagesizegb'],
      details: ['name', 'state', 'databaseversion', 'tier', 'region', 'storagesizegb', 'created'],
      searchFilters: ['name', 'state', 'region'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpCloudSqlTab.vue'))
      }],
      actions: [
        {
          api: 'createGcpCloudSqlInstance',
          icon: 'plus-outlined',
          label: 'Create Cloud SQL Instance',
          listView: true,
          component: defineAsyncComponent(() => import('@/views/gcp/CreateGcpCloudSql.vue'))
        },
        {
          api: 'deleteGcpCloudSqlInstance',
          icon: 'delete-outlined',
          label: 'Delete',
          dataView: true
        }
      ]
    },
    {
      name: 'gcpalloydb',
      title: 'AlloyDB',
      icon: 'cluster-outlined',
      permission: ['listGcpAlloyDbClusters'],
      columns: ['name', 'state', 'region', 'network'],
      details: ['name', 'state', 'region', 'network', 'created'],
      searchFilters: ['name', 'state', 'region'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpAlloyDbTab.vue'))
      }]
    },
    {
      name: 'gcpspanner',
      title: 'Cloud Spanner',
      icon: 'deployment-unit-outlined',
      permission: ['listGcpSpannerInstances'],
      columns: ['name', 'state', 'config', 'nodecount', 'displayname'],
      details: ['name', 'state', 'config', 'nodecount', 'displayname', 'created'],
      searchFilters: ['name', 'state'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpSpannerTab.vue'))
      }]
    },
    {
      name: 'gcpbigquery',
      title: 'BigQuery',
      icon: 'bar-chart-outlined',
      permission: ['listGcpBigQueryDatasets'],
      columns: ['datasetid', 'location', 'description', 'created'],
      details: ['datasetid', 'location', 'description', 'created'],
      searchFilters: ['datasetid', 'location'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpBigQueryTab.vue'))
      }]
    },
    {
      name: 'gcpbigqueryreservation',
      title: 'BigQuery Reservation',
      icon: 'schedule-outlined',
      permission: ['listGcpBigQueryReservations'],
      columns: ['name', 'slotcapacity', 'location'],
      details: ['name', 'slotcapacity', 'location', 'created'],
      searchFilters: ['name', 'location'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpBigQueryReservationTab.vue'))
      }]
    },
    {
      name: 'gcpnetworking',
      title: 'Networking',
      icon: 'apartment-outlined',
      permission: ['listGcpVpcNetworks'],
      columns: ['name', 'routingmode', 'autocreatesubnetworks', 'status'],
      details: ['name', 'routingmode', 'autocreatesubnetworks', 'status', 'created'],
      searchFilters: ['name'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpNetworkingTab.vue'))
      }]
    },
    {
      name: 'gcpkubernetesengine',
      title: 'Kubernetes Engine',
      icon: 'kubernetes-outlined',
      permission: ['listGcpGkeClusters'],
      columns: ['name', 'status', 'zone', 'clusterversion', 'nodecount'],
      details: ['name', 'status', 'zone', 'clusterversion', 'nodecount', 'machineType', 'network', 'created'],
      searchFilters: ['name', 'status', 'zone'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpKubernetesTab.vue'))
      }],
      actions: [
        {
          api: 'createGcpGkeCluster',
          icon: 'plus-outlined',
          label: 'Create GKE Cluster',
          listView: true,
          component: defineAsyncComponent(() => import('@/views/gcp/CreateGcpGkeCluster.vue'))
        },
        {
          api: 'deleteGcpGkeCluster',
          icon: 'delete-outlined',
          label: 'Delete',
          dataView: true
        }
      ]
    },
    {
      name: 'gcpcloudrun',
      title: 'Cloud Run',
      icon: 'rocket-outlined',
      permission: ['listGcpCloudRunServices'],
      columns: ['name', 'status', 'region', 'url', 'image'],
      details: ['name', 'status', 'region', 'url', 'image', 'memory', 'cpu', 'maxinstances', 'created'],
      searchFilters: ['name', 'status', 'region'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpCloudRunTab.vue'))
      }]
    },
    {
      name: 'gcpmonitoring',
      title: 'Cloud Monitoring',
      icon: 'monitor-outlined',
      permission: ['listGcpAlertPolicies'],
      columns: ['displayname', 'enabled', 'conditionfilter', 'thresholdvalue'],
      details: ['displayname', 'enabled', 'conditionfilter', 'thresholdvalue', 'created'],
      searchFilters: ['displayname'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpMonitoringTab.vue'))
      }]
    },
    {
      name: 'gcplogging',
      title: 'Cloud Logging',
      icon: 'file-text-outlined',
      permission: ['listGcpLogEntries'],
      columns: ['logname', 'severity', 'timestamp', 'message'],
      details: ['logname', 'severity', 'timestamp', 'message', 'labels'],
      searchFilters: ['logname', 'severity'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpCloudLoggingTab.vue'))
      }]
    },
    {
      name: 'gcpvertexai',
      title: 'Vertex AI',
      icon: 'experiment-outlined',
      permission: ['listGcpVertexAiDatasets'],
      columns: ['displayname', 'state', 'region', 'metadataschemauri'],
      details: ['displayname', 'state', 'region', 'metadataschemauri', 'created'],
      searchFilters: ['displayname', 'state'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpVertexAiTab.vue'))
      }]
    },
    {
      name: 'gcpgemini',
      title: 'Gemini API',
      icon: 'robot-outlined',
      permission: ['listGcpGeminiModels'],
      component: defineAsyncComponent(() => import('@/views/gcp/GcpGeminiConsole.vue'))
    },
    {
      name: 'gcpnotebooks',
      title: 'Notebooks',
      icon: 'read-outlined',
      permission: ['listGcpNotebookInstances'],
      columns: ['name', 'state', 'location', 'machinetype', 'framework'],
      details: ['name', 'state', 'location', 'machinetype', 'vmimage', 'framework', 'created'],
      searchFilters: ['name', 'state', 'location'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpNotebooksTab.vue'))
      }]
    },
    {
      name: 'gcpmemorystore',
      title: 'Memorystore for Redis',
      icon: 'thunderbolt-outlined',
      permission: ['listGcpRedisInstances'],
      columns: ['name', 'state', 'tier', 'memorysizegb', 'redisversion', 'location'],
      details: ['name', 'state', 'tier', 'memorysizegb', 'redisversion', 'location', 'connectmode', 'created'],
      searchFilters: ['name', 'state', 'location'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpMemorystoreTab.vue'))
      }]
    },
    {
      name: 'gcpdatastream',
      title: 'Datastream',
      icon: 'swap-outlined',
      permission: ['listGcpStreams'],
      columns: ['name', 'displayname', 'state', 'sourceprofile', 'destprofile'],
      details: ['name', 'displayname', 'state', 'sourceprofile', 'destprofile', 'created'],
      searchFilters: ['name', 'state'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpDatastreamTab.vue'))
      }]
    },
    {
      name: 'gcpdataplex',
      title: 'Dataplex',
      icon: 'gold-outlined',
      permission: ['listGcpDataplexLakes'],
      columns: ['name', 'displayname', 'state', 'location', 'description'],
      details: ['name', 'displayname', 'state', 'location', 'description', 'created'],
      searchFilters: ['name', 'state', 'location'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpDataplexTab.vue'))
      }]
    },
    {
      name: 'gcpcomposer',
      title: 'Cloud Composer',
      icon: 'branches-outlined',
      permission: ['listGcpComposerEnvironments'],
      columns: ['name', 'state', 'location', 'imageversion'],
      details: ['name', 'state', 'location', 'imageversion', 'created'],
      searchFilters: ['name', 'state', 'location'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpComposerTab.vue'))
      }]
    },
    {
      name: 'gcpbackupgke',
      title: 'Backup for GKE',
      icon: 'save-outlined',
      permission: ['listGcpBackupPlans'],
      columns: ['name', 'cluster', 'schedule', 'retaindays', 'state'],
      details: ['name', 'cluster', 'schedule', 'retaindays', 'state', 'created'],
      searchFilters: ['name', 'cluster'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpBackupGkeTab.vue'))
      }]
    },
    {
      name: 'gcpartifactregistry',
      title: 'Artifact Registry',
      icon: 'inbox-outlined',
      permission: ['listGcpRepositories'],
      columns: ['name', 'format', 'location', 'description'],
      details: ['name', 'format', 'location', 'description', 'created'],
      searchFilters: ['name', 'format', 'location'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpArtifactRegistryTab.vue'))
      }]
    },
    {
      name: 'gcpsecretmanager',
      title: 'Secret Manager',
      icon: 'lock-outlined',
      permission: ['listGcpSecrets'],
      columns: ['name', 'replication', 'createtime', 'labels'],
      details: ['name', 'replication', 'createtime', 'labels'],
      searchFilters: ['name'],
      tabs: [{
        name: 'details',
        component: defineAsyncComponent(() => import('@/views/gcp/GcpSecretManagerTab.vue'))
      }]
    }
  ]
}
