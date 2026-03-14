<template>
  <div>
    <a-alert
      v-if="resource"
      :message="'Deploy: ' + resource.name"
      :description="resource.description"
      type="info"
      show-icon
      style="margin-bottom: 16px"
    />
    <a-form :model="form" layout="vertical" @finish="handleSubmit">
      <a-form-item name="catalogitemid" :label="$t('label.catalog.item')" required>
        <a-input v-model:value="form.catalogitemid" :disabled="!!resource" />
      </a-form-item>
      <a-form-item name="name" :label="$t('label.instance.name')" required>
        <a-input v-model:value="form.name" placeholder="my-service-01" />
      </a-form-item>
      <a-form-item name="zoneid" :label="$t('label.zone')" required>
        <a-select v-model:value="form.zoneid">
          <a-select-option v-for="zone in zones" :key="zone.id" :value="zone.id">
            {{ zone.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item name="serviceofferingid" :label="$t('label.serviceofferingid')" required>
        <a-select v-model:value="form.serviceofferingid">
          <a-select-option v-for="o in serviceOfferings" :key="o.id" :value="o.id">
            {{ o.name }} ({{ o.cpunumber }} CPU, {{ o.memory }}MB)
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item name="templateid" :label="$t('label.template')" required>
        <a-select v-model:value="form.templateid" show-search :filter-option="filterOption">
          <a-select-option v-for="template in templates" :key="template.id" :value="template.id">
            {{ template.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item name="assignpublicip" :label="$t('label.assign.public.ip')">
        <a-switch v-model:checked="form.assignpublicip" />
      </a-form-item>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item name="ipmode" :label="$t('label.public.access.mode')">
            <a-select v-model:value="form.ipmode" :disabled="!form.assignpublicip">
              <a-select-option value="STATIC_NAT">{{ $t('label.public.access.mode.staticnat') }}</a-select-option>
              <a-select-option value="PORT_FORWARD">{{ $t('label.public.access.mode.portforward') }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item v-if="form.assignpublicip && form.ipmode === 'PORT_FORWARD'" name="privateports" :label="$t('label.private.ports')">
            <a-input v-model:value="form.privateports" placeholder="80,443" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item name="allowedcidr" :label="$t('label.allowed.cidr')">
        <a-input v-model:value="form.allowedcidr" placeholder="0.0.0.0/0" />
      </a-form-item>
      <a-form-item name="count" :label="$t('label.number.of.instances')">
        <a-input-number v-model:value="form.count" :min="1" :max="1000" style="width: 100%" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading">{{ $t('label.catalog.deploy') }}</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { api } from '@/api'

export default {
  name: 'DeployCatalogItem',
  props: {
    resource: { type: Object, default: () => ({}) }
  },
  setup (props, { emit }) {
    const loading = ref(false)
    const zones = ref([])
    const serviceOfferings = ref([])
    const templates = ref([])
    const form = reactive({
      catalogitemid: props.resource?.id || '',
      name: '',
      zoneid: undefined,
      serviceofferingid: undefined,
      templateid: undefined,
      assignpublicip: false,
      ipmode: 'STATIC_NAT',
      privateports: '',
      allowedcidr: '0.0.0.0/0',
      count: 1
    })

    onMounted(() => {
      api('listZones', { available: true }).then(r => { zones.value = r.listzonesresponse?.zone || [] })
      api('listServiceOfferings', {}).then(r => { serviceOfferings.value = r.listserviceofferingsresponse?.serviceoffering || [] })
      api('listTemplates', { templatefilter: 'featured' }).then(r => { templates.value = r.listtemplatesresponse?.template || [] })
    })

    const filterOption = (input, option) => {
      return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
    }

    const handleSubmit = () => {
      loading.value = true
      api('deployCatalogItem', form).then(() => {
        emit('close-action')
        emit('refresh')
      }).finally(() => { loading.value = false })
    }

    return { form, loading, zones, serviceOfferings, templates, handleSubmit, filterOption }
  }
}
</script>
