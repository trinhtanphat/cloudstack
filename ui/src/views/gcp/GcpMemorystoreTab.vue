<template>
  <div>
    <a-card title="Memorystore for Redis Instance" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Tier">{{ resource.tier }}</a-descriptions-item>
        <a-descriptions-item label="Memory Size (GB)">{{ resource.memorysizegb }}</a-descriptions-item>
        <a-descriptions-item label="Redis Version">{{ resource.redisversion }}</a-descriptions-item>
        <a-descriptions-item label="Location">{{ resource.location }}</a-descriptions-item>
        <a-descriptions-item label="Connect Mode">{{ resource.connectmode }}</a-descriptions-item>
        <a-descriptions-item label="Host">{{ resource.host }}</a-descriptions-item>
        <a-descriptions-item label="Port">{{ resource.port }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpMemorystoreTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = { READY: 'green', CREATING: 'orange', UPDATING: 'blue', DELETING: 'red' }
      return colors[this.resource.state] || 'default'
    }
  }
}
</script>
