<template>
  <div>
    <a-card title="Cloud Monitoring" :bordered="false">
      <a-tabs default-active-key="alerts">
        <a-tab-pane key="alerts" tab="Alert Policies">
          <a-table :columns="alertColumns" :data-source="alerts" :pagination="{ pageSize: 20 }" size="small" />
        </a-tab-pane>
        <a-tab-pane key="dashboards" tab="Dashboards">
          <a-table :columns="dashboardColumns" :data-source="dashboards" :pagination="false" size="small" />
        </a-tab-pane>
        <a-tab-pane key="uptime" tab="Uptime Checks">
          <a-table :columns="uptimeColumns" :data-source="uptimeChecks" :pagination="false" size="small" />
        </a-tab-pane>
        <a-tab-pane key="channels" tab="Notification Channels">
          <a-table :columns="channelColumns" :data-source="channels" :pagination="false" size="small" />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpMonitoringTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      alerts: [],
      dashboards: [],
      uptimeChecks: [],
      channels: [],
      alertColumns: [
        { title: 'Name', dataIndex: 'displayName', key: 'displayName' },
        { title: 'Enabled', dataIndex: 'enabled', key: 'enabled' },
        { title: 'Condition', dataIndex: 'conditionFilter', key: 'conditionFilter' },
        { title: 'Threshold', dataIndex: 'thresholdValue', key: 'thresholdValue' }
      ],
      dashboardColumns: [
        { title: 'Name', dataIndex: 'displayName', key: 'displayName' },
        { title: 'Widgets', dataIndex: 'widgetCount', key: 'widgetCount' }
      ],
      uptimeColumns: [
        { title: 'Name', dataIndex: 'displayName', key: 'displayName' },
        { title: 'Host', dataIndex: 'host', key: 'host' },
        { title: 'Path', dataIndex: 'path', key: 'path' },
        { title: 'Period (s)', dataIndex: 'periodSeconds', key: 'periodSeconds' }
      ],
      channelColumns: [
        { title: 'Name', dataIndex: 'displayName', key: 'displayName' },
        { title: 'Type', dataIndex: 'type', key: 'type' }
      ]
    }
  }
}
</script>
