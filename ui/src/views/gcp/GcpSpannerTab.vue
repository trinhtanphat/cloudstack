<template>
  <div>
    <a-card title="Cloud Spanner Instance Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Display Name">{{ resource.displayname }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Config">{{ resource.config }}</a-descriptions-item>
        <a-descriptions-item label="Node Count">{{ resource.nodecount }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Databases" :bordered="false" style="margin-top: 16px" v-if="resource.databases">
      <a-table :columns="dbColumns" :data-source="resource.databases" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpSpannerTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = { READY: 'green', CREATING: 'orange' }
      return colors[this.resource.state] || 'default'
    }
  },
  data () {
    return {
      dbColumns: [
        { title: 'Database ID', dataIndex: 'databaseId', key: 'databaseId' },
        { title: 'State', dataIndex: 'state', key: 'state' },
        { title: 'Created', dataIndex: 'created', key: 'created' }
      ]
    }
  }
}
</script>
