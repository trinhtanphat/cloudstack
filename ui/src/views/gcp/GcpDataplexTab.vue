<template>
  <div>
    <a-card title="Dataplex Lake Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Display Name">{{ resource.displayname }}</a-descriptions-item>
        <a-descriptions-item label="State">
          <a-tag :color="stateColor">{{ resource.state }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Location">{{ resource.location }}</a-descriptions-item>
        <a-descriptions-item label="Description">{{ resource.description }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Zones" :bordered="false" style="margin-top: 16px" v-if="resource.zones">
      <a-table :columns="zoneColumns" :data-source="resource.zones" :pagination="false" size="small" />
    </a-card>

    <a-card title="Tasks" :bordered="false" style="margin-top: 16px" v-if="resource.tasks">
      <a-table :columns="taskColumns" :data-source="resource.tasks" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpDataplexTab',
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
      zoneColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Type', dataIndex: 'type', key: 'type' },
        { title: 'State', dataIndex: 'state', key: 'state' }
      ],
      taskColumns: [
        { title: 'Task ID', dataIndex: 'taskId', key: 'taskId' },
        { title: 'State', dataIndex: 'state', key: 'state' },
        { title: 'Created', dataIndex: 'created', key: 'created' }
      ]
    }
  }
}
</script>
