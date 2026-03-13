<template>
  <div>
    <a-card title="Secret Manager" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Replication">{{ resource.replication }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.createtime }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Labels" :bordered="false" style="margin-top: 16px" v-if="resource.labels">
      <a-tag v-for="(value, key) in resource.labels" :key="key" color="blue">
        {{ key }}: {{ value }}
      </a-tag>
    </a-card>

    <a-card title="Versions" :bordered="false" style="margin-top: 16px" v-if="resource.versions">
      <a-table :columns="versionColumns" :data-source="resource.versions" :pagination="false" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'state'">
            <a-tag :color="versionStateColor(record.state)">{{ record.state }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpSecretManagerTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      versionColumns: [
        { title: 'Version', dataIndex: 'name', key: 'name' },
        { title: 'State', dataIndex: 'state', key: 'state' },
        { title: 'Created', dataIndex: 'created', key: 'created' }
      ]
    }
  },
  methods: {
    versionStateColor (state) {
      const colors = { ENABLED: 'green', DISABLED: 'orange', DESTROYED: 'red' }
      return colors[state] || 'default'
    }
  }
}
</script>
