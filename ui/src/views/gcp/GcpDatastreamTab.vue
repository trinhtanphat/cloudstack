<template>
  <div>
    <a-card title="Datastream Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Display Name">{{ resource.displayname }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Source Profile">{{ resource.sourceprofile }}</a-descriptions-item>
        <a-descriptions-item label="Destination Profile">{{ resource.destprofile }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Stream Objects" :bordered="false" style="margin-top: 16px" v-if="resource.objects">
      <a-table :columns="objectColumns" :data-source="resource.objects" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpDatastreamTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = { RUNNING: 'green', PAUSED: 'orange', NOT_STARTED: 'grey', FAILED: 'red' }
      return colors[this.resource.state] || 'default'
    }
  },
  data () {
    return {
      objectColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Source Table', dataIndex: 'sourceTable', key: 'sourceTable' },
        { title: 'Status', dataIndex: 'status', key: 'status' }
      ]
    }
  }
}
</script>
