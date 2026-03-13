<template>
  <a-form :form="form" layout="vertical" @finish="handleSubmit">
    <a-form-item label="Instance Name" name="name" :rules="[{ required: true, message: 'Please enter instance name' }]">
      <a-input v-model:value="form.name" placeholder="my-instance" />
    </a-form-item>
    <a-form-item label="Machine Type" name="machinetype" :rules="[{ required: true }]">
      <a-select v-model:value="form.machinetype" placeholder="Select machine type">
        <a-select-option value="e2-micro">e2-micro (2 vCPU, 1 GB)</a-select-option>
        <a-select-option value="e2-small">e2-small (2 vCPU, 2 GB)</a-select-option>
        <a-select-option value="e2-medium">e2-medium (2 vCPU, 4 GB)</a-select-option>
        <a-select-option value="e2-standard-2">e2-standard-2 (2 vCPU, 8 GB)</a-select-option>
        <a-select-option value="e2-standard-4">e2-standard-4 (4 vCPU, 16 GB)</a-select-option>
        <a-select-option value="e2-standard-8">e2-standard-8 (8 vCPU, 32 GB)</a-select-option>
        <a-select-option value="n2-standard-2">n2-standard-2 (2 vCPU, 8 GB)</a-select-option>
        <a-select-option value="n2-standard-4">n2-standard-4 (4 vCPU, 16 GB)</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Zone" name="zone" :rules="[{ required: true }]">
      <a-select v-model:value="form.zone" placeholder="Select zone">
        <a-select-option value="us-central1-a">us-central1-a</a-select-option>
        <a-select-option value="us-east1-b">us-east1-b</a-select-option>
        <a-select-option value="europe-west1-b">europe-west1-b</a-select-option>
        <a-select-option value="asia-southeast1-a">asia-southeast1-a</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Boot Disk Image" name="image">
      <a-select v-model:value="form.image" placeholder="Select image">
        <a-select-option value="debian-12">Debian 12</a-select-option>
        <a-select-option value="ubuntu-2204">Ubuntu 22.04 LTS</a-select-option>
        <a-select-option value="centos-stream-9">CentOS Stream 9</a-select-option>
        <a-select-option value="windows-server-2022">Windows Server 2022</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Boot Disk Size (GB)" name="disksizegb">
      <a-input-number v-model:value="form.disksizegb" :min="10" :max="65536" style="width: 100%" />
    </a-form-item>
    <a-form-item label="Network" name="network">
      <a-input v-model:value="form.network" placeholder="default" />
    </a-form-item>
  </a-form>
</template>

<script>
export default {
  name: 'CreateGcpInstance',
  data () {
    return {
      form: {
        name: '',
        machinetype: 'e2-medium',
        zone: 'us-central1-a',
        image: 'debian-12',
        disksizegb: 20,
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
