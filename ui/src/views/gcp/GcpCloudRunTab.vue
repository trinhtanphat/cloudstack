<template>
  <div>
    <a-card title="Cloud Run Service Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Status">
          <a-tag :color="stateColor">{{ resource.status }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Region">{{ resource.region }}</a-descriptions-item>
        <a-descriptions-item label="URL">
          <a v-if="resource.url" :href="resource.url" target="_blank">{{ resource.url }}</a>
        </a-descriptions-item>
        <a-descriptions-item label="Image">{{ resource.image }}</a-descriptions-item>
        <a-descriptions-item label="Memory">{{ resource.memory }}</a-descriptions-item>
        <a-descriptions-item label="CPU">{{ resource.cpu }}</a-descriptions-item>
        <a-descriptions-item label="Max Instances">{{ resource.maxinstances }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Revisions" :bordered="false" style="margin-top: 16px" v-if="resource.revisions">
      <a-table :columns="revisionColumns" :data-source="resource.revisions" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpCloudRunTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  computed: {
    stateColor () {
      const colors = { ACTIVE: 'green', DEPLOYING: 'orange', FAILED: 'red' }
      return colors[this.resource.status] || 'default'
    }
  },
  data () {
    return {
      revisionColumns: [
        { title: 'Revision', dataIndex: 'name', key: 'name' },
        { title: 'Traffic %', dataIndex: 'traffic', key: 'traffic' },
        { title: 'Created', dataIndex: 'created', key: 'created' }
      ]
    }
  }
}
</script>
