<template>
  <div>
    <a-card title="Compute Engine Instance Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Machine Type">{{ resource.machinetype }}</a-descriptions-item>
        <a-descriptions-item label="Zone">{{ resource.zone }}</a-descriptions-item>
        <a-descriptions-item label="Internal IP">{{ resource.internalip }}</a-descriptions-item>
        <a-descriptions-item label="External IP">{{ resource.externalip }}</a-descriptions-item>
        <a-descriptions-item label="Network">{{ resource.networkname }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Disks" :bordered="false" style="margin-top: 16px" v-if="resource.disks">
      <a-table :columns="diskColumns" :data-source="resource.disks" :pagination="false" size="small">
      </a-table>
    </a-card>

    <a-card title="Labels" :bordered="false" style="margin-top: 16px" v-if="resource.labels">
      <a-tag v-for="(value, key) in resource.labels" :key="key" color="blue">
        {{ key }}: {{ value }}
      </a-tag>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpComputeEngineTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = {
        RUNNING: 'green',
        STOPPED: 'red',
        STAGING: 'orange',
        TERMINATED: 'grey',
        SUSPENDED: 'yellow'
      }
      return colors[this.resource.state] || 'default'
    }
  },
  data () {
    return {
      diskColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Type', dataIndex: 'type', key: 'type' },
        { title: 'Size (GB)', dataIndex: 'sizeGb', key: 'sizeGb' },
        { title: 'Status', dataIndex: 'status', key: 'status' }
      ]
    }
  }
}
</script>
