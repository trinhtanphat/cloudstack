<template>
  <div>
    <a-form
      :model="form"
      :rules="rules"
      layout="vertical"
      @finish="handleSubmit">

      <a-form-item name="name" label="Name" required>
        <a-input v-model:value="form.name" placeholder="gpu-inference-01" />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item name="provider" label="Provider" required>
            <a-select v-model:value="form.provider">
              <a-select-option value="AWS">AWS</a-select-option>
              <a-select-option value="AZURE">Azure</a-select-option>
              <a-select-option value="GCP">GCP</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="gpuprofileid" label="GPU Profile" required>
            <a-select v-model:value="form.gpuprofileid" :loading="profilesLoading" show-search option-filter-prop="children">
              <a-select-option v-for="profile in profiles" :key="profile.id" :value="profile.id">
                {{ profile.id }} ({{ profile.gpumodel }}, {{ profile.memorygb }} GB)
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item name="gpucount" label="GPU Count" required>
            <a-input-number v-model:value="form.gpucount" :min="1" :max="16" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="zoneid" :label="$t('label.zone')" required>
            <a-select v-model:value="form.zoneid" :loading="zonesLoading">
              <a-select-option v-for="zone in zones" :key="zone.id" :value="zone.id">
                {{ zone.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item name="serviceofferingid" :label="$t('label.serviceofferingid')" required>
        <a-select v-model:value="form.serviceofferingid" :loading="offeringsLoading">
          <a-select-option v-for="offering in serviceOfferings" :key="offering.id" :value="offering.id">
            {{ offering.name }} ({{ offering.cpunumber }} CPU, {{ offering.memory }} MB RAM)
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item name="templateid" :label="$t('label.template')" required>
        <a-select v-model:value="form.templateid" :loading="templatesLoading" show-search option-filter-prop="children">
          <a-select-option v-for="template in templates" :key="template.id" :value="template.id">
            {{ template.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item name="networkid" :label="$t('label.networkid')">
        <a-input v-model:value="form.networkid" placeholder="Optional network UUID" />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading">
          Create GPU Instance
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import { api } from '@/api'

export default {
  name: 'CreateGpuInstance',
  setup (props, { emit }) {
    const loading = ref(false)
    const zonesLoading = ref(false)
    const offeringsLoading = ref(false)
    const templatesLoading = ref(false)
    const profilesLoading = ref(false)

    const zones = ref([])
    const serviceOfferings = ref([])
    const templates = ref([])
    const profiles = ref([])

    const form = reactive({
      name: '',
      provider: 'GCP',
      gpuprofileid: undefined,
      gpucount: 1,
      zoneid: undefined,
      serviceofferingid: undefined,
      templateid: undefined,
      networkid: ''
    })

    const rules = {
      name: [{ required: true, message: 'Please enter a name' }],
      provider: [{ required: true, message: 'Please select a provider' }],
      gpuprofileid: [{ required: true, message: 'Please select a GPU profile' }],
      gpucount: [{ required: true, message: 'Please enter gpu count' }],
      zoneid: [{ required: true, message: 'Please select a zone' }],
      serviceofferingid: [{ required: true, message: 'Please select a service offering' }],
      templateid: [{ required: true, message: 'Please select a template' }]
    }

    const fetchZones = () => {
      zonesLoading.value = true
      api('listZones', { available: true }).then(response => {
        zones.value = response.listzonesresponse?.zone || []
      }).finally(() => {
        zonesLoading.value = false
      })
    }

    const fetchServiceOfferings = () => {
      offeringsLoading.value = true
      api('listServiceOfferings', {}).then(response => {
        serviceOfferings.value = response.listserviceofferingsresponse?.serviceoffering || []
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

    const fetchProfiles = () => {
      profilesLoading.value = true
      api('listGpuProfiles', { provider: form.provider }).then(response => {
        profiles.value = response.listgpuprofilesresponse?.gpuprofile || []
        if (profiles.value.length > 0) {
          form.gpuprofileid = profiles.value[0].id
        } else {
          form.gpuprofileid = undefined
        }
      }).finally(() => {
        profilesLoading.value = false
      })
    }

    const handleSubmit = () => {
      loading.value = true
      const params = { ...form }
      if (!params.networkid) {
        delete params.networkid
      }
      api('createGpuInstance', params).then(() => {
        emit('close-action')
        emit('refresh')
      }).catch(error => {
        console.error('Failed to create gpu instance', error)
      }).finally(() => {
        loading.value = false
      })
    }

    watch(() => form.provider, () => {
      fetchProfiles()
    })

    onMounted(() => {
      fetchZones()
      fetchServiceOfferings()
      fetchTemplates()
      fetchProfiles()
    })

    return {
      form,
      rules,
      loading,
      zonesLoading,
      offeringsLoading,
      templatesLoading,
      profilesLoading,
      zones,
      serviceOfferings,
      templates,
      profiles,
      handleSubmit
    }
  }
}
</script>
