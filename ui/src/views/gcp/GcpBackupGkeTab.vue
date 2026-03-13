<template>
  <div>
    <a-card title="Backup for GKE Plan Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Cluster">{{ resource.cluster }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Schedule">{{ resource.schedule }}</a-descriptions-item>
        <a-descriptions-item label="Retain Days">{{ resource.retaindays }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Backups" :bordered="false" style="margin-top: 16px" v-if="resource.backups">
      <a-table :columns="backupColumns" :data-source="resource.backups" :pagination="false" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'state'">
            <a-tag :color="backupStateColor(record.state)">{{ record.state }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpBackupGkeTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = { ACTIVE: 'green', CREATING: 'orange', DELETING: 'red' }
      return colors[this.resource.state] || 'default'
    }
  },
  data () {
    return {
      backupColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'State', dataIndex: 'state', key: 'state' },
        { title: 'Size', dataIndex: 'size', key: 'size' },
        { title: 'Created', dataIndex: 'created', key: 'created' }
      ]
    }
  },
  methods: {
    backupStateColor (state) {
      const colors = { SUCCEEDED: 'green', IN_PROGRESS: 'blue', FAILED: 'red' }
      return colors[state] || 'default'
    }
  }
}
</script>
