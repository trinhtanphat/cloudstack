<template>
  <div>
    <a-form :model="form" :rules="rules" layout="vertical" @finish="handleSubmit">
      <a-alert
        :message="$t('label.bulk.vm.deployment')"
        :description="$t('label.bulk.vm.description')"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item name="count" :label="$t('label.number.of.vms')" required>
            <a-input-number v-model:value="form.count" :min="1" :max="10000" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="nameprefix" :label="$t('label.name.prefix')" required>
            <a-input v-model:value="form.nameprefix" placeholder="customer-vps" />
            <span class="ant-form-text">VMs: {{ form.nameprefix }}-0001, {{ form.nameprefix }}-0002, ...</span>
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item name="zoneid" :label="$t('label.zone')" required>
        <a-select v-model:value="form.zoneid">
          <a-select-option v-for="zone in zones" :key="zone.id" :value="zone.id">{{ zone.name }}</a-select-option>
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
          <a-select-option v-for="t in templates" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
        </a-select>
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item name="assignpublicip" :label="$t('label.assign.public.ip')">
            <a-switch v-model:checked="form.assignpublicip" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item name="batchsize" :label="$t('label.batch.size')">
            <a-input-number v-model:value="form.batchsize" :min="1" :max="200" style="width: 100%" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item name="ipmode" :label="$t('label.public.access.mode')">
            <a-select v-model:value="form.ipmode" :disabled="!form.assignpublicip">
              <a-select-option value="STATIC_NAT">{{ $t('label.public.access.mode.staticnat.per.vm') }}</a-select-option>
              <a-select-option value="PORT_FORWARD">{{ $t('label.public.access.mode.portforward.full') }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item v-if="form.assignpublicip && form.ipmode === 'PORT_FORWARD'" name="privateports" :label="$t('label.private.ports')">
            <a-input v-model:value="form.privateports" placeholder="22,80,443" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item name="keypairname" :label="$t('label.ssh.keypair.optional')">
        <a-input v-model:value="form.keypairname" placeholder="my-keypair" />
      </a-form-item>

      <a-divider />

      <a-alert
        v-if="form.count > 100"
        :message="'You are about to deploy ' + form.count + ' VMs' + (form.assignpublicip ? ' with ' + form.count + ' public IPs' : '')"
        type="warning"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" size="large">
          Deploy {{ form.count }} VMs
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { api } from '@/api'

export default {
  name: 'BulkDeployVMs',
  setup (props, { emit }) {
    const loading = ref(false)
    const zones = ref([])
    const serviceOfferings = ref([])
    const templates = ref([])

    const form = reactive({
      count: 10,
      nameprefix: 'vps',
      zoneid: undefined,
      serviceofferingid: undefined,
      templateid: undefined,
      assignpublicip: false,
      ipmode: 'STATIC_NAT',
      privateports: '22',
      batchsize: 50,
      keypairname: ''
    })

    const rules = {
      count: [{ required: true, message: 'Enter number of VMs' }],
      nameprefix: [{ required: true, message: 'Enter name prefix' }],
      zoneid: [{ required: true, message: 'Select a zone' }],
      serviceofferingid: [{ required: true, message: 'Select a service offering' }],
      templateid: [{ required: true, message: 'Select a template' }]
    }

    const filterOption = (input, option) => {
      return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
    }

    onMounted(() => {
      api('listZones', { available: true }).then(r => { zones.value = r.listzonesresponse?.zone || [] })
      api('listServiceOfferings', {}).then(r => { serviceOfferings.value = r.listserviceofferingsresponse?.serviceoffering || [] })
      api('listTemplates', { templatefilter: 'featured' }).then(r => { templates.value = r.listtemplatesresponse?.template || [] })
    })

    const handleSubmit = () => {
      loading.value = true
      api('bulkDeployVirtualMachines', form).then(() => {
        emit('close-action')
        emit('refresh')
      }).catch(error => {
        console.error('Bulk deploy failed', error)
      }).finally(() => { loading.value = false })
    }

    return { form, rules, loading, zones, serviceOfferings, templates, filterOption, handleSubmit }
  }
}
</script>
