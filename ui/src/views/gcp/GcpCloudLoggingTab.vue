<template>
  <div>
    <a-card title="Cloud Logging" :bordered="false">
      <a-space style="margin-bottom: 16px">
        <a-select v-model:value="severity" placeholder="Severity" style="width: 160px" @change="refresh">
          <a-select-option value="">All</a-select-option>
          <a-select-option value="ERROR">ERROR</a-select-option>
          <a-select-option value="WARNING">WARNING</a-select-option>
          <a-select-option value="INFO">INFO</a-select-option>
          <a-select-option value="DEBUG">DEBUG</a-select-option>
        </a-select>
        <a-input-search v-model:value="filter" placeholder="Filter logs..." style="width: 300px" @search="refresh" />
      </a-space>

      <a-table :columns="logColumns" :data-source="resource.entries || []" :pagination="{ pageSize: 20 }" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'severity'">
            <a-tag :color="severityColor(record.severity)">{{ record.severity }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-card title="Log Sinks" :bordered="false" style="margin-top: 16px" v-if="resource.sinks">
      <a-table :columns="sinkColumns" :data-source="resource.sinks" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpCloudLoggingTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      severity: '',
      filter: '',
      logColumns: [
        { title: 'Timestamp', dataIndex: 'timestamp', key: 'timestamp', width: 180 },
        { title: 'Severity', dataIndex: 'severity', key: 'severity', width: 100 },
        { title: 'Log Name', dataIndex: 'logname', key: 'logname', width: 200 },
        { title: 'Message', dataIndex: 'message', key: 'message' }
      ],
      sinkColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Destination', dataIndex: 'destination', key: 'destination' },
        { title: 'Filter', dataIndex: 'filter', key: 'filter' }
      ]
    }
  },
  methods: {
    severityColor (severity) {
      const colors = { ERROR: 'red', WARNING: 'orange', INFO: 'blue', DEBUG: 'grey', CRITICAL: 'red', EMERGENCY: 'red' }
      return colors[severity] || 'default'
    },
    refresh () {
      this.$emit('refresh')
    }
  }
}
</script>
