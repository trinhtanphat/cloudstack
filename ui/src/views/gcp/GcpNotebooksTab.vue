<template>
  <div>
    <a-card title="Notebook Instance Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Location">{{ resource.location }}</a-descriptions-item>
        <a-descriptions-item label="Machine Type">{{ resource.machinetype }}</a-descriptions-item>
        <a-descriptions-item label="VM Image">{{ resource.vmimage }}</a-descriptions-item>
        <a-descriptions-item label="Framework">{{ resource.framework }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpNotebooksTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = { ACTIVE: 'green', STOPPED: 'red', PROVISIONING: 'orange', INITIALIZING: 'orange', DELETED: 'grey' }
      return colors[this.resource.state] || 'default'
    }
  }
}
</script>
