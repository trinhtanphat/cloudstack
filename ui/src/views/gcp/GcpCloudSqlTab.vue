<template>
  <div>
    <a-card title="Cloud SQL Instance Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Database Version">{{ resource.databaseversion }}</a-descriptions-item>
        <a-descriptions-item label="Tier">{{ resource.tier }}</a-descriptions-item>
        <a-descriptions-item label="Region">{{ resource.region }}</a-descriptions-item>
        <a-descriptions-item label="Storage Size (GB)">{{ resource.storagesizegb }}</a-descriptions-item>
        <a-descriptions-item label="Connection Name">{{ resource.connectionname }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Databases" :bordered="false" style="margin-top: 16px" v-if="resource.databases">
      <a-table :columns="dbColumns" :data-source="resource.databases" :pagination="false" size="small" />
    </a-card>

    <a-card title="Backups" :bordered="false" style="margin-top: 16px" v-if="resource.backups">
      <a-table :columns="backupColumns" :data-source="resource.backups" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpCloudSqlTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = { RUNNABLE: 'green', STOPPED: 'red', PENDING_CREATE: 'orange', MAINTENANCE: 'yellow', FAILED: 'red' }
      return colors[this.resource.state] || 'default'
    }
  },
  data () {
    return {
      dbColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Charset', dataIndex: 'charset', key: 'charset' },
        { title: 'Collation', dataIndex: 'collation', key: 'collation' }
      ],
      backupColumns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: 'Status', dataIndex: 'status', key: 'status' },
        { title: 'Created', dataIndex: 'created', key: 'created' }
      ]
    }
  }
}
</script>
