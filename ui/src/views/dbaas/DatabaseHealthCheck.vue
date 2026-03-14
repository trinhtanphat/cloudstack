<template>
  <div>
    <a-card :title="$t('label.dbaas.health')" :loading="loading">
      <template #extra>
        <a-button type="primary" size="small" @click="fetchHealth" :loading="loading">
          <template #icon><ReloadOutlined /></template>
          {{ $t('label.refresh') }}
        </a-button>
      </template>

      <a-alert
        v-if="health"
        :type="health.healthy ? 'success' : 'error'"
        :message="health.healthy ? $t('label.dbaas.health.healthy') : $t('label.dbaas.health.unhealthy')"
        :description="health.message"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-descriptions v-if="health" bordered :column="2" size="small">
        <a-descriptions-item :label="$t('label.name')">
          {{ health.name }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('label.dbaas.engine')">
          {{ health.dbengine }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('label.state')">
          <a-tag :color="health.state === 'RUNNING' ? 'green' : 'red'">
            {{ health.state }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('label.dbaas.health.vm.state')">
          <a-tag :color="health.vmstate === 'Running' ? 'green' : 'orange'">
            {{ health.vmstate || 'N/A' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('label.dbaas.health.port.reachable')">
          <a-tag :color="health.portreachable ? 'green' : 'red'">
            {{ health.portreachable ? 'Yes' : 'No' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('label.ip.address')">
          {{ health.ipaddress }}:{{ health.port }}
        </a-descriptions-item>
        <a-descriptions-item :label="$t('label.dbaas.health.checked.at')">
          {{ health.checkedat }}
        </a-descriptions-item>
      </a-descriptions>

      <a-empty v-if="!health && !loading" />
    </a-card>
  </div>
</template>

<script>
import { api } from '@/api'
import { ReloadOutlined } from '@ant-design/icons-vue'

export default {
  name: 'DatabaseHealthCheck',
  components: { ReloadOutlined },
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      loading: false,
      health: null
    }
  },
  mounted () {
    this.fetchHealth()
  },
  methods: {
    fetchHealth () {
      if (!this.resource || !this.resource.id) return
      this.loading = true
      api('checkDatabaseHealth', {
        id: this.resource.id
      }).then(response => {
        this.health = response.checkdatabasehealthresponse || response
      }).catch(error => {
        this.$notification.error({
          message: this.$t('label.error'),
          description: error.response?.data?.errorresponse?.errortext || error.message
        })
      }).finally(() => {
        this.loading = false
      })
    }
  }
}
</script>
