<template>
  <div>
    <a-card title="Artifact Registry Repository" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Format">
          <a-tag color="blue">{{ resource.format }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Location">{{ resource.location }}</a-descriptions-item>
        <a-descriptions-item label="Description">{{ resource.description }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Packages" :bordered="false" style="margin-top: 16px" v-if="resource.packages">
      <a-table :columns="packageColumns" :data-source="resource.packages" :pagination="false" size="small" />
    </a-card>

    <a-card title="Docker Images" :bordered="false" style="margin-top: 16px" v-if="resource.images">
      <a-table :columns="imageColumns" :data-source="resource.images" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpArtifactRegistryTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      packageColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Latest Version', dataIndex: 'latestVersion', key: 'latestVersion' },
        { title: 'Created', dataIndex: 'created', key: 'created' }
      ],
      imageColumns: [
        { title: 'Image', dataIndex: 'name', key: 'name' },
        { title: 'Tags', dataIndex: 'tags', key: 'tags' },
        { title: 'Size', dataIndex: 'size', key: 'size' },
        { title: 'Uploaded', dataIndex: 'uploaded', key: 'uploaded' }
      ]
    }
  }
}
</script>
