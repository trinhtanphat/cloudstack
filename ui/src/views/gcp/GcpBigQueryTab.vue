<template>
  <div>
    <a-card title="BigQuery Dataset Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Dataset ID">{{ resource.datasetid }}</a-descriptions-item>
        <a-descriptions-item label="Location">{{ resource.location }}</a-descriptions-item>
        <a-descriptions-item label="Description">{{ resource.description }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Tables" :bordered="false" style="margin-top: 16px">
      <a-table :columns="tableColumns" :data-source="tables" :pagination="{ pageSize: 20 }" size="small">
      </a-table>
    </a-card>

    <a-card title="Query Console" :bordered="false" style="margin-top: 16px">
      <a-textarea v-model:value="queryText" placeholder="Enter SQL query..." :rows="4" style="margin-bottom: 12px" />
      <a-button type="primary" @click="runQuery" :loading="queryLoading">
        <template #icon><caret-right-outlined /></template>
        Run Query
      </a-button>
      <a-table
        v-if="queryResults.length > 0"
        :columns="queryResultColumns"
        :data-source="queryResults"
        :pagination="{ pageSize: 50 }"
        size="small"
        style="margin-top: 12px"
        :scroll="{ x: true }" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpBigQueryTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      tables: [],
      queryText: '',
      queryLoading: false,
      queryResults: [],
      queryResultColumns: [],
      tableColumns: [
        { title: 'Table ID', dataIndex: 'tableId', key: 'tableId' },
        { title: 'Type', dataIndex: 'type', key: 'type' },
        { title: 'Rows', dataIndex: 'numRows', key: 'numRows' },
        { title: 'Size', dataIndex: 'numBytes', key: 'numBytes' },
        { title: 'Created', dataIndex: 'created', key: 'created' }
      ]
    }
  },
  methods: {
    runQuery () {
      this.queryLoading = true
      // TODO: Implement via API call
      setTimeout(() => {
        this.queryLoading = false
      }, 1000)
    }
  }
}
</script>
