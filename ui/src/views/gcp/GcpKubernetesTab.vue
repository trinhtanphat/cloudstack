<template>
  <div>
    <a-card title="GKE Cluster Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Cluster Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Status">
          <a-tag :color="statusColor">{{ resource.status }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Zone">{{ resource.zone }}</a-descriptions-item>
        <a-descriptions-item label="Cluster Version">{{ resource.clusterversion }}</a-descriptions-item>
        <a-descriptions-item label="Node Count">{{ resource.nodecount }}</a-descriptions-item>
        <a-descriptions-item label="Machine Type">{{ resource.machineType }}</a-descriptions-item>
        <a-descriptions-item label="Network">{{ resource.network }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-tabs default-active-key="nodepools" style="margin-top: 16px">
      <a-tab-pane key="nodepools" tab="Node Pools">
        <a-table :columns="nodePoolColumns" :data-source="nodePools" :pagination="false" size="small" />
      </a-tab-pane>
      <a-tab-pane key="workloads" tab="Workloads">
        <a-empty description="Connect kubectl to view workloads" />
      </a-tab-pane>
      <a-tab-pane key="services" tab="Services & Ingress">
        <a-empty description="Connect kubectl to view services" />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
export default {
  name: 'GcpKubernetesTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    statusColor () {
      const colors = {
        RUNNING: 'green',
        PROVISIONING: 'orange',
        RECONCILING: 'blue',
        STOPPING: 'red',
        ERROR: 'red'
      }
      return colors[this.resource.status] || 'default'
    }
  },
  data () {
    return {
      nodePools: [],
      nodePoolColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Status', dataIndex: 'status', key: 'status' },
        { title: 'Node Count', dataIndex: 'nodeCount', key: 'nodeCount' },
        { title: 'Machine Type', dataIndex: 'machineType', key: 'machineType' },
        { title: 'Auto Scaling', dataIndex: 'autoScaling', key: 'autoScaling' }
      ]
    }
  }
}
</script>
