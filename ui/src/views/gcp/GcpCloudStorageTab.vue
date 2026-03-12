<template>
  <div>
    <a-card title="Cloud Storage Bucket Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Location">{{ resource.location }}</a-descriptions-item>
        <a-descriptions-item label="Storage Class">{{ resource.storageclass }}</a-descriptions-item>
        <a-descriptions-item label="Versioning">
          <a-tag :color="resource.versioningenabled ? 'green' : 'default'">
            {{ resource.versioningenabled ? 'Enabled' : 'Disabled' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Objects" :bordered="false" style="margin-top: 16px">
      <a-table :columns="objectColumns" :data-source="objects" :pagination="{ pageSize: 20 }" size="small">
      </a-table>
    </a-card>

    <a-card title="Lifecycle Rules" :bordered="false" style="margin-top: 16px">
      <a-empty v-if="!lifecycleRules.length" description="No lifecycle rules configured" />
      <a-list v-else :data-source="lifecycleRules" size="small">
        <template #renderItem="{ item }">
          <a-list-item>{{ item.action }} - {{ item.condition }}</a-list-item>
        </template>
      </a-list>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpCloudStorageTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      objects: [],
      lifecycleRules: [],
      objectColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Size', dataIndex: 'size', key: 'size' },
        { title: 'Content Type', dataIndex: 'contentType', key: 'contentType' },
        { title: 'Updated', dataIndex: 'updated', key: 'updated' }
      ]
    }
  }
}
</script>
