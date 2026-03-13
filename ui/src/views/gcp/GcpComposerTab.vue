<template>
  <div>
    <a-card title="Cloud Composer Environment" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Location">{{ resource.location }}</a-descriptions-item>
        <a-descriptions-item label="Image Version">{{ resource.imageversion }}</a-descriptions-item>
        <a-descriptions-item label="Airflow URI">
          <a v-if="resource.airflowuri" :href="resource.airflowuri" target="_blank">{{ resource.airflowuri }}</a>
        </a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="DAGs" :bordered="false" style="margin-top: 16px" v-if="resource.dags">
      <a-table :columns="dagColumns" :data-source="resource.dags" :pagination="false" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'isPaused'">
            <a-tag :color="record.isPaused ? 'orange' : 'green'">{{ record.isPaused ? 'Paused' : 'Active' }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpComposerTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = { RUNNING: 'green', CREATING: 'orange', UPDATING: 'blue', DELETING: 'red', ERROR: 'red' }
      return colors[this.resource.state] || 'default'
    }
  },
  data () {
    return {
      dagColumns: [
        { title: 'DAG ID', dataIndex: 'dagId', key: 'dagId' },
        { title: 'Status', dataIndex: 'isPaused', key: 'isPaused' },
        { title: 'Schedule', dataIndex: 'schedule', key: 'schedule' },
        { title: 'Last Run', dataIndex: 'lastRun', key: 'lastRun' }
      ]
    }
  }
}
</script>
