<template>
  <div>
    <a-card title="AlloyDB Cluster Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Cluster Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Region">{{ resource.region }}</a-descriptions-item>
        <a-descriptions-item label="Network">{{ resource.network }}</a-descriptions-item>
        <a-descriptions-item label="Database Version">{{ resource.databaseversion }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Instances" :bordered="false" style="margin-top: 16px" v-if="resource.instances">
      <a-table :columns="instanceColumns" :data-source="resource.instances" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpAlloyDbTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = { READY: 'green', CREATING: 'orange', DELETING: 'red', MAINTENANCE: 'yellow' }
      return colors[this.resource.state] || 'default'
    }
  },
  data () {
    return {
      instanceColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Type', dataIndex: 'instanceType', key: 'instanceType' },
        { title: 'State', dataIndex: 'state', key: 'state' },
        { title: 'CPU Count', dataIndex: 'cpuCount', key: 'cpuCount' }
      ]
    }
  }
}
</script>
