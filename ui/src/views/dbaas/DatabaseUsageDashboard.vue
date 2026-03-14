<template>
  <div>
    <a-card :title="$t('label.dbaas.usage')">
      <template #extra>
        <a-space>
          <a-range-picker
            v-model:value="dateRange"
            :format="'YYYY-MM-DD'"
            @change="fetchUsage"
            size="small"
          />
          <a-button type="primary" size="small" @click="fetchUsage" :loading="loading">
            <template #icon><ReloadOutlined /></template>
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="usageData"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'dbengine'">
            <a-tag color="blue">{{ record.dbengine }}</a-tag>
          </template>
          <template v-if="column.key === 'state'">
            <a-tag :color="record.state === 'RUNNING' ? 'green' : 'orange'">
              {{ record.state }}
            </a-tag>
          </template>
          <template v-if="column.key === 'resources'">
            {{ record.cpucores }} vCPU / {{ record.memorymb }} MB / {{ record.storagesizegb }} GB
          </template>
        </template>
      </a-table>

      <a-divider />

      <a-row :gutter="16" v-if="summary">
        <a-col :span="6">
          <a-statistic
            :title="$t('label.dbaas.usage.running.hours')"
            :value="summary.totalRunningHours"
            suffix="h"
            :precision="1"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            :title="$t('label.dbaas.usage.cpu.hours')"
            :value="summary.totalCpuHours"
            suffix="vCPU·h"
            :precision="1"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            :title="$t('label.dbaas.usage.memory.hours')"
            :value="summary.totalMemoryMbHours"
            suffix="MB·h"
            :precision="0"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            :title="$t('label.dbaas.usage.backup.count')"
            :value="summary.totalBackups"
          />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script>
import { api } from '@/api'
import { ReloadOutlined } from '@ant-design/icons-vue'

export default {
  name: 'DatabaseUsageDashboard',
  components: { ReloadOutlined },
  data () {
    return {
      loading: false,
      usageData: [],
      dateRange: null,
      columns: [
        { title: this.$t('label.name'), dataIndex: 'name', key: 'name' },
        { title: this.$t('label.dbaas.engine'), dataIndex: 'dbengine', key: 'dbengine' },
        { title: this.$t('label.state'), dataIndex: 'state', key: 'state' },
        { title: 'Resources', key: 'resources' },
        { title: this.$t('label.dbaas.usage.running.hours'), dataIndex: 'runninghours', key: 'runninghours' },
        { title: this.$t('label.dbaas.usage.cpu.hours'), dataIndex: 'cpuhoursused', key: 'cpuhoursused' },
        { title: this.$t('label.dbaas.usage.backup.count'), dataIndex: 'backupcount', key: 'backupcount' }
      ]
    }
  },
  computed: {
    summary () {
      if (!this.usageData.length) return null
      return {
        totalRunningHours: this.usageData.reduce((sum, r) => sum + (r.runninghours || 0), 0),
        totalCpuHours: this.usageData.reduce((sum, r) => sum + (r.cpuhoursused || 0), 0),
        totalMemoryMbHours: this.usageData.reduce((sum, r) => sum + (r.memorymbhours || 0), 0),
        totalBackups: this.usageData.reduce((sum, r) => sum + (r.backupcount || 0), 0)
      }
    }
  },
  mounted () {
    this.fetchUsage()
  },
  methods: {
    fetchUsage () {
      this.loading = true
      const params = {}
      if (this.dateRange && this.dateRange.length === 2) {
        params.startdate = this.dateRange[0].format('YYYY-MM-DD')
        params.enddate = this.dateRange[1].format('YYYY-MM-DD')
      }
      api('listDatabaseUsage', params).then(response => {
        this.usageData = response.listdatabaseusageresponse?.databaseusage || []
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
