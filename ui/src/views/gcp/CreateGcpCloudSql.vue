<template>
  <a-form layout="vertical" @finish="handleSubmit">
    <a-form-item label="Instance Name" name="name" :rules="[{ required: true, message: 'Please enter instance name' }]">
      <a-input v-model:value="form.name" placeholder="my-cloudsql-instance" />
    </a-form-item>
    <a-form-item label="Database Version" name="databaseversion" :rules="[{ required: true }]">
      <a-select v-model:value="form.databaseversion" placeholder="Select database version">
        <a-select-option value="MYSQL_8_0">MySQL 8.0</a-select-option>
        <a-select-option value="MYSQL_5_7">MySQL 5.7</a-select-option>
        <a-select-option value="POSTGRES_16">PostgreSQL 16</a-select-option>
        <a-select-option value="POSTGRES_15">PostgreSQL 15</a-select-option>
        <a-select-option value="POSTGRES_14">PostgreSQL 14</a-select-option>
        <a-select-option value="SQLSERVER_2022_STANDARD">SQL Server 2022 Standard</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Machine Tier" name="tier" :rules="[{ required: true }]">
      <a-select v-model:value="form.tier" placeholder="Select tier">
        <a-select-option value="db-f1-micro">db-f1-micro (Shared)</a-select-option>
        <a-select-option value="db-g1-small">db-g1-small (Shared)</a-select-option>
        <a-select-option value="db-custom-1-3840">db-custom-1-3840 (1 vCPU, 3.75 GB)</a-select-option>
        <a-select-option value="db-custom-2-7680">db-custom-2-7680 (2 vCPU, 7.5 GB)</a-select-option>
        <a-select-option value="db-custom-4-15360">db-custom-4-15360 (4 vCPU, 15 GB)</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Region" name="region" :rules="[{ required: true }]">
      <a-select v-model:value="form.region" placeholder="Select region">
        <a-select-option value="us-central1">us-central1</a-select-option>
        <a-select-option value="us-east1">us-east1</a-select-option>
        <a-select-option value="europe-west1">europe-west1</a-select-option>
        <a-select-option value="asia-southeast1">asia-southeast1</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item label="Storage Size (GB)" name="storagesizegb">
      <a-input-number v-model:value="form.storagesizegb" :min="10" :max="65536" style="width: 100%" />
    </a-form-item>
    <a-form-item label="High Availability">
      <a-switch v-model:checked="form.highavailability" />
    </a-form-item>
  </a-form>
</template>

<script>
export default {
  name: 'CreateGcpCloudSql',
  data () {
    return {
      form: {
        name: '',
        databaseversion: 'MYSQL_8_0',
        tier: 'db-custom-1-3840',
        region: 'us-central1',
        storagesizegb: 20,
        highavailability: false
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
