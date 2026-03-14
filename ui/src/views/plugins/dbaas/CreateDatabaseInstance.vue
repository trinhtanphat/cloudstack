<template>
  <div>
    <a-form
      :model="form"
      :rules="rules"
      layout="vertical"
      @finish="handleSubmit">

      <a-form-item name="name" :label="$t('label.name')" required>
        <a-input v-model:value="form.name" :placeholder="$t('label.dbaas.name.placeholder')" />
      </a-form-item>

      <a-form-item name="dbengine" :label="$t('label.dbaas.engine')" required>
        <a-select v-model:value="form.dbengine" @change="onEngineChange">
          <a-select-option value="MYSQL">MySQL 8.0</a-select-option>
          <a-select-option value="POSTGRESQL">PostgreSQL 16</a-select-option>
          <a-select-option value="MONGODB">MongoDB 7.0</a-select-option>
          <a-select-option value="SQLSERVER">SQL Server 2022</a-select-option>
          <a-select-option value="REDIS">Redis 7.2</a-select-option>
          <a-select-option value="PHPMYADMIN">phpMyAdmin 5.2</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item name="dbversion" :label="$t('label.dbaas.version')">
        <a-input v-model:value="form.dbversion" :placeholder="defaultVersion" />
      </a-form-item>

      <a-form-item name="zoneid" :label="$t('label.zone')" required>
        <a-select v-model:value="form.zoneid" :loading="zonesLoading">
          <a-select-option v-for="zone in zones" :key="zone.id" :value="zone.id">
            {{ zone.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item name="serviceofferingid" :label="$t('label.serviceofferingid')" required>
        <a-select v-model:value="form.serviceofferingid" :loading="offeringsLoading">
          <a-select-option v-for="offering in serviceOfferings" :key="offering.id" :value="offering.id">
            {{ offering.name }} ({{ offering.cpunumber }} CPU, {{ offering.memory }} MB RAM)
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item name="templateid" :label="$t('label.template')" required>
        <a-select v-model:value="form.templateid" :loading="templatesLoading" show-search :filter-option="filterOption">
          <a-select-option v-for="template in templates" :key="template.id" :value="template.id">
            {{ template.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item name="storagesizegb" :label="$t('label.dbaas.storage.size')">
        <a-input-number v-model:value="form.storagesizegb" :min="5" :max="10000" :step="5" style="width: 100%" />
        <span class="ant-form-text">GB</span>
      </a-form-item>

      <a-form-item name="adminusername" :label="$t('label.dbaas.admin.username')">
        <a-input v-model:value="form.adminusername" placeholder="dbadmin" />
      </a-form-item>

      <a-form-item name="adminpassword" :label="$t('label.dbaas.admin.password')">
        <a-input-password v-model:value="form.adminpassword" placeholder="Auto-generated if empty" />
      </a-form-item>

      <a-form-item name="assignpublicip" :label="$t('label.dbaas.public.ip')">
        <a-switch v-model:checked="form.assignpublicip" />
        <span class="ant-form-text">{{ $t('label.dbaas.public.ip.desc') }}</span>
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item name="ipmode" :label="$t('label.public.access.mode')">
            <a-select v-model:value="form.ipmode" :disabled="!form.assignpublicip">
              <a-select-option value="STATIC_NAT">{{ $t('label.public.access.mode.staticnat') }}</a-select-option>
              <a-select-option value="PORT_FORWARD">{{ $t('label.public.access.mode.portforward.full') }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="allowedcidr" :label="$t('label.allowed.cidr')">
            <a-input v-model:value="form.allowedcidr" placeholder="0.0.0.0/0" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item name="backupenabled" :label="$t('label.dbaas.backup.enabled')">
        <a-switch v-model:checked="form.backupenabled" />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading">
          {{ $t('label.dbaas.create') }}
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { api } from '@/api'

export default {
  name: 'CreateDatabaseInstance',
  props: {
    resource: {
      type: Object,
      default: () => ({})
    }
  },
  setup (props, { emit }) {
    const loading = ref(false)
    const zonesLoading = ref(false)
    const offeringsLoading = ref(false)
    const templatesLoading = ref(false)
    const zones = ref([])
    const serviceOfferings = ref([])
    const templates = ref([])
    const defaultVersion = ref('8.0')

    const form = reactive({
      name: '',
      dbengine: 'MYSQL',
      dbversion: '',
      zoneid: undefined,
      serviceofferingid: undefined,
      templateid: undefined,
      storagesizegb: 20,
      adminusername: 'dbadmin',
      adminpassword: '',
      assignpublicip: false,
      ipmode: 'STATIC_NAT',
      allowedcidr: '0.0.0.0/0',
      backupenabled: true
    })

    const rules = {
      name: [{ required: true, message: 'Please enter a name' }],
      dbengine: [{ required: true, message: 'Please select a database engine' }],
      zoneid: [{ required: true, message: 'Please select a zone' }],
      serviceofferingid: [{ required: true, message: 'Please select a service offering' }],
      templateid: [{ required: true, message: 'Please select a template' }]
    }

    const versionMap = {
      MYSQL: '8.0',
      POSTGRESQL: '16',
      MONGODB: '7.0',
      SQLSERVER: '2022',
      REDIS: '7.2',
      PHPMYADMIN: '5.2'
    }

    const onEngineChange = (value) => {
      defaultVersion.value = versionMap[value] || ''
      form.dbversion = ''
    }

    const fetchZones = () => {
      zonesLoading.value = true
      api('listZones', { available: true }).then(response => {
        zones.value = response.listzonesresponse.zone || []
      }).finally(() => {
        zonesLoading.value = false
      })
    }

    const fetchServiceOfferings = () => {
      offeringsLoading.value = true
      api('listServiceOfferings', {}).then(response => {
        serviceOfferings.value = response.listserviceofferingsresponse.serviceoffering || []
      }).finally(() => {
        offeringsLoading.value = false
      })
    }

    const fetchTemplates = () => {
      templatesLoading.value = true
      api('listTemplates', { templatefilter: 'featured' }).then(response => {
        templates.value = response.listtemplatesresponse?.template || []
      }).finally(() => {
        templatesLoading.value = false
      })
    }

    const filterOption = (input, option) => {
      return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
    }

    const handleSubmit = () => {
      loading.value = true
      const params = { ...form }
      if (!params.dbversion) {
        params.dbversion = defaultVersion.value
      }
      api('createDatabaseInstance', params).then(response => {
        emit('close-action')
        emit('refresh')
      }).catch(error => {
        console.error('Failed to create database instance', error)
      }).finally(() => {
        loading.value = false
      })
    }

    onMounted(() => {
      fetchZones()
      fetchServiceOfferings()
      fetchTemplates()
    })

    return {
      form,
      rules,
      loading,
      zonesLoading,
      offeringsLoading,
      templatesLoading,
      zones,
      serviceOfferings,
      templates,
      defaultVersion,
      onEngineChange,
      handleSubmit,
      filterOption
    }
  }
}
</script>
