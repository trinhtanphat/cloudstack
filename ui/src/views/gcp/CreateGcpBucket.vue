<template>
  <a-form layout="vertical" @finish="handleSubmit">
    <a-form-item label="Bucket Name" name="name" :rules="[{ required: true, message: 'Please enter bucket name' }]">
      <a-input v-model:value="form.name" placeholder="my-bucket" />
    </a-form-item>
    <a-form-item label="Location" name="location" :rules="[{ required: true }]">
      <a-select v-model:value="form.location" placeholder="Select location">
        <a-select-option value="US">US (Multi-region)</a-select-option>
        <a-select-option value="EU">EU (Multi-region)</a-select-option>
        <a-select-option value="ASIA">Asia (Multi-region)</a-select-option>
        <a-select-option value="us-central1">us-central1</a-select-option>
        <a-select-option value="us-east1">us-east1</a-select-option>
        <a-select-option value="europe-west1">europe-west1</a-select-option>
        <a-select-option value="asia-southeast1">asia-southeast1</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Storage Class" name="storageclass">
      <a-select v-model:value="form.storageclass" placeholder="Select storage class">
        <a-select-option value="STANDARD">Standard</a-select-option>
        <a-select-option value="NEARLINE">Nearline</a-select-option>
        <a-select-option value="COLDLINE">Coldline</a-select-option>
        <a-select-option value="ARCHIVE">Archive</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Versioning">
      <a-switch v-model:checked="form.versioningenabled" />
    </a-form-item>
  </a-form>
</template>

<script>
export default {
  name: 'CreateGcpBucket',
  data () {
    return {
      form: {
        name: '',
        location: 'US',
        storageclass: 'STANDARD',
        versioningenabled: false
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
