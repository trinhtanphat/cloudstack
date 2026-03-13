<template>
  <div>
    <a-card title="Vertex AI Dataset Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Display Name">{{ resource.displayname }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Region">{{ resource.region }}</a-descriptions-item>
        <a-descriptions-item label="Metadata Schema URI">{{ resource.metadataschemauri }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Training Pipelines" :bordered="false" style="margin-top: 16px" v-if="resource.pipelines">
      <a-table :columns="pipelineColumns" :data-source="resource.pipelines" :pagination="false" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'state'">
            <a-tag :color="pipelineStateColor(record.state)">{{ record.state }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-card title="Endpoints" :bordered="false" style="margin-top: 16px" v-if="resource.endpoints">
      <a-table :columns="endpointColumns" :data-source="resource.endpoints" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpVertexAiTab',
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
      pipelineColumns: [
        { title: 'Name', dataIndex: 'displayName', key: 'displayName' },
        { title: 'State', dataIndex: 'state', key: 'state' },
        { title: 'Created', dataIndex: 'created', key: 'created' }
      ],
      endpointColumns: [
        { title: 'Name', dataIndex: 'displayName', key: 'displayName' },
        { title: 'Model', dataIndex: 'model', key: 'model' },
        { title: 'Machine Type', dataIndex: 'machineType', key: 'machineType' }
      ]
    }
  },
  methods: {
    pipelineStateColor (state) {
      const colors = { PIPELINE_STATE_SUCCEEDED: 'green', PIPELINE_STATE_RUNNING: 'blue', PIPELINE_STATE_FAILED: 'red', PIPELINE_STATE_PENDING: 'orange' }
      return colors[state] || 'default'
    }
  }
}
</script>
