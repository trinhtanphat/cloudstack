<template>
  <div>
    <a-card :title="$t('label.bulk.jobs')" :loading="initialLoading">
      <template #extra>
        <a-space>
          <a-switch
            v-model:checked="autoRefresh"
            checked-children="Auto"
            un-checked-children="Manual"
            @change="toggleAutoRefresh"
          />
          <a-button type="primary" size="small" @click="fetchJobs">
            <template #icon><ReloadOutlined /></template>
          </a-button>
        </a-space>
      </template>

      <a-list :data-source="jobs" :locale="{ emptyText: 'No bulk jobs found' }">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>
                <a-space>
                  <span>{{ item.jobtype }} - {{ item.nameprefix || 'Unnamed' }}</span>
                  <a-tag :color="statusColor(item.status)">{{ item.status }}</a-tag>
                </a-space>
              </template>
              <template #description>
                <div style="margin-top: 8px">
                  <a-progress
                    :percent="progressPercent(item)"
                    :status="progressStatus(item)"
                    :stroke-color="{ '0%': '#108ee9', '100%': '#87d068' }"
                    size="small"
                  />
                  <div style="margin-top: 4px; color: rgba(0,0,0,0.45)">
                    <a-space :size="16">
                      <span>
                        <CheckCircleOutlined style="color: #52c41a" />
                        {{ item.completedcount || 0 }} / {{ item.totalcount || 0 }} completed
                      </span>
                      <span v-if="item.failedcount > 0">
                        <CloseCircleOutlined style="color: #f5222d" />
                        {{ item.failedcount }} failed
                      </span>
                      <span>
                        <ClockCircleOutlined />
                        {{ formatDate(item.created) }}
                      </span>
                    </a-space>
                  </div>
                </div>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </div>
</template>

<script>
import { api } from '@/api'
import {
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'

export default {
  name: 'BulkJobProgress',
  components: {
    ReloadOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    ClockCircleOutlined
  },
  data () {
    return {
      jobs: [],
      initialLoading: true,
      autoRefresh: true,
      refreshTimer: null
    }
  },
  mounted () {
    this.fetchJobs()
    this.startAutoRefresh()
  },
  beforeUnmount () {
    this.stopAutoRefresh()
  },
  methods: {
    fetchJobs () {
      api('listBulkJobs', {}).then(response => {
        this.jobs = response.listbulkjobsresponse?.bulkjob || []
      }).catch(error => {
        this.$notification.error({
          message: this.$t('label.error'),
          description: error.response?.data?.errorresponse?.errortext || error.message
        })
      }).finally(() => {
        this.initialLoading = false
      })
    },
    progressPercent (item) {
      if (!item.totalcount || item.totalcount === 0) return 0
      return Math.round(((item.completedcount || 0) + (item.failedcount || 0)) / item.totalcount * 100)
    },
    progressStatus (item) {
      if (item.status === 'COMPLETED') return 'success'
      if (item.status === 'FAILED') return 'exception'
      if (item.status === 'RUNNING') return 'active'
      return 'normal'
    },
    statusColor (status) {
      const colors = {
        PENDING: 'default',
        RUNNING: 'processing',
        COMPLETED: 'success',
        FAILED: 'error',
        PARTIAL: 'warning'
      }
      return colors[status] || 'default'
    },
    formatDate (dateStr) {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleString()
    },
    toggleAutoRefresh (checked) {
      if (checked) {
        this.startAutoRefresh()
      } else {
        this.stopAutoRefresh()
      }
    },
    startAutoRefresh () {
      this.stopAutoRefresh()
      this.refreshTimer = setInterval(() => {
        const hasActiveJobs = this.jobs.some(j => j.status === 'RUNNING' || j.status === 'PENDING')
        if (hasActiveJobs) {
          this.fetchJobs()
        }
      }, 3000)
    },
    stopAutoRefresh () {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    }
  }
}
</script>
