<template>
  <a-form layout="vertical" @finish="handleSubmit">
    <a-form-item label="Cluster Name" name="name" :rules="[{ required: true, message: 'Please enter cluster name' }]">
      <a-input v-model:value="form.name" placeholder="my-gke-cluster" />
    </a-form-item>
    <a-form-item label="Zone" name="zone" :rules="[{ required: true }]">
      <a-select v-model:value="form.zone" placeholder="Select zone">
        <a-select-option value="us-central1-a">us-central1-a</a-select-option>
        <a-select-option value="us-east1-b">us-east1-b</a-select-option>
        <a-select-option value="europe-west1-b">europe-west1-b</a-select-option>
        <a-select-option value="asia-southeast1-a">asia-southeast1-a</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Cluster Version" name="clusterversion">
      <a-select v-model:value="form.clusterversion" placeholder="Select version">
        <a-select-option value="1.29">1.29 (Stable)</a-select-option>
        <a-select-option value="1.28">1.28 (Stable)</a-select-option>
        <a-select-option value="1.27">1.27 (Stable)</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Node Count" name="nodecount">
      <a-input-number v-model:value="form.nodecount" :min="1" :max="100" style="width: 100%" />
    </a-form-item>
    <a-form-item label="Machine Type" name="machinetype">
      <a-select v-model:value="form.machinetype" placeholder="Select machine type">
        <a-select-option value="e2-medium">e2-medium (2 vCPU, 4 GB)</a-select-option>
        <a-select-option value="e2-standard-2">e2-standard-2 (2 vCPU, 8 GB)</a-select-option>
        <a-select-option value="e2-standard-4">e2-standard-4 (4 vCPU, 16 GB)</a-select-option>
        <a-select-option value="n2-standard-2">n2-standard-2 (2 vCPU, 8 GB)</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Network" name="network">
      <a-input v-model:value="form.network" placeholder="default" />
    </a-form-item>
  </a-form>
</template>

<script>
export default {
  name: 'CreateGcpGkeCluster',
  data () {
    return {
      form: {
        name: '',
        zone: 'us-central1-a',
        clusterversion: '1.29',
        nodecount: 3,
        machinetype: 'e2-standard-2',
        network: 'default'
      }
    }
  },
  methods: {
    handleSubmit () {
      this.$emit('submit', this.form)
    }
  }
}
</script>
