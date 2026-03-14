<template>
  <div class="service-catalog">
    <a-card class="catalog-toolbar" :bordered="false">
      <a-row :gutter="16" align="middle">
        <a-col :xs="24" :md="14">
          <a-input-search
            v-model:value="searchKeyword"
            :placeholder="$t('label.catalog.search')"
            @search="fetchCatalog"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :md="10">
          <a-radio-group v-model:value="selectedCategory" @change="fetchCatalog" class="category-group">
            <a-radio-button value="">{{ $t('label.catalog.category.all') }}</a-radio-button>
            <a-radio-button value="DATABASE">Database</a-radio-button>
            <a-radio-button value="WEBSERVER">Web</a-radio-button>
            <a-radio-button value="APPSTACK">App</a-radio-button>
            <a-radio-button value="CACHE">Cache</a-radio-button>
            <a-radio-button value="MONITORING">Monitoring</a-radio-button>
            <a-radio-button value="MANAGEMENT">Management</a-radio-button>
          </a-radio-group>
        </a-col>
      </a-row>
    </a-card>

    <a-card class="status-tracker" :title="$t('label.catalog.batch.tracker')" :bordered="false">
      <a-row :gutter="12" align="middle">
        <a-col :xs="24" :md="14">
          <a-input
            ref="batchOperationInputRef"
            v-model:value="batchOperationId"
            :placeholder="$t('label.catalog.batch.id.placeholder')"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :md="10" class="tracker-actions">
          <a-space>
            <a-button type="primary" :loading="batchLoading" @click="fetchBatchStatus">
              {{ $t('label.catalog.batch.load') }}
            </a-button>
            <a-button @click="clearBatchStatus">{{ $t('label.clear') }}</a-button>
          </a-space>
        </a-col>
      </a-row>

      <div v-if="batchStatus" class="tracker-content">
        <a-row :gutter="12" class="tracker-metrics">
          <a-col :xs="12" :md="6">
            <div class="metric-box">
              <div class="metric-label">{{ $t('label.total') }}</div>
              <div class="metric-value">{{ batchStatus.totalcount || 0 }}</div>
            </div>
          </a-col>
          <a-col :xs="12" :md="6">
            <div class="metric-box metric-success">
              <div class="metric-label">{{ $t('label.completed') }}</div>
              <div class="metric-value">{{ batchStatus.completedcount || 0 }}</div>
            </div>
          </a-col>
          <a-col :xs="12" :md="6">
            <div class="metric-box metric-danger">
              <div class="metric-label">{{ $t('label.failed') }}</div>
              <div class="metric-value">{{ batchStatus.failedcount || 0 }}</div>
            </div>
          </a-col>
          <a-col :xs="12" :md="6">
            <div class="metric-box">
              <div class="metric-label">{{ $t('label.state') }}</div>
              <a-tag :color="getStateColor(batchStatus.state)">{{ batchStatus.state }}</a-tag>
            </div>
          </a-col>
        </a-row>

        <a-progress :percent="batchProgress" :status="batchProgressStatus" stroke-color="#0f766e" />

        <a-table
          :columns="instanceColumns"
          :data-source="batchStatus.instances || []"
          :pagination="{ pageSize: 10 }"
          :row-key="record => `${record.index}-${record.name}`"
          size="small"
          class="instance-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'status'">
              <a-tag :color="getStateColor(record.status)">{{ record.status }}</a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'publicendpoint'">
              <span>{{ formatEndpoint(record) }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'updated'">
              <span>{{ formatDate(record.updated) }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'error'">
              <span class="error-cell">{{ record.error || '-' }}</span>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>

    <a-spin :spinning="loading">
      <a-row :gutter="[16, 16]" class="catalog-grid">
        <a-col
          v-for="item in catalogItems"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <a-card hoverable class="catalog-card" :bordered="false">
            <template #title>
              <div class="card-header">
                <a-tag :color="getCategoryColor(item.category)">{{ item.category }}</a-tag>
                <a-tag v-if="item.popular" color="gold">Popular</a-tag>
              </div>
              <h3 class="card-title">{{ item.name }}</h3>
            </template>

            <p class="card-description">{{ item.description }}</p>

            <div class="card-specs">
              <div><strong>Version:</strong> {{ item.version }}</div>
              <div><strong>Port:</strong> {{ item.defaultport }}</div>
              <div><strong>Min:</strong> {{ item.mincpu }} CPU, {{ item.minmemorymb }}MB RAM, {{ item.minstoragegb }}GB</div>
            </div>

            <div class="card-features" v-if="item.features && item.features.length">
              <a-tag v-for="feature in item.features.slice(0, 3)" :key="feature">{{ feature }}</a-tag>
            </div>

            <template #actions>
              <a-button type="primary" @click="showDeployModal(item)">
                <template #icon><rocket-outlined /></template>
                {{ $t('label.catalog.deploy') }}
              </a-button>
            </template>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>

    <a-modal
      v-model:open="deployModalVisible"
      :title="'Deploy ' + (selectedItem ? selectedItem.name : '')"
      :confirm-loading="deploying"
      @ok="handleDeploy"
    >
      <a-form layout="vertical">
        <a-form-item :label="$t('label.instance.name')" required>
          <a-input v-model:value="deployForm.name" placeholder="my-service-01" />
        </a-form-item>
        <a-form-item :label="$t('label.zone')" required>
          <a-select v-model:value="deployForm.zoneid">
            <a-select-option v-for="zone in zones" :key="zone.id" :value="zone.id">
              {{ zone.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('label.serviceofferingid')" required>
          <a-select v-model:value="deployForm.serviceofferingid">
            <a-select-option v-for="offering in serviceOfferings" :key="offering.id" :value="offering.id">
              {{ offering.name }} ({{ offering.cpunumber }} CPU, {{ offering.memory }}MB)
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('label.template')" required>
          <a-select v-model:value="deployForm.templateid" show-search :filter-option="filterTemplateOption">
            <a-select-option v-for="template in templates" :key="template.id" :value="template.id">
              {{ template.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('label.assign.public.ip')">
          <a-switch v-model:checked="deployForm.assignpublicip" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="$t('label.public.access.mode')">
              <a-select v-model:value="deployForm.ipmode" :disabled="!deployForm.assignpublicip">
                <a-select-option value="STATIC_NAT">{{ $t('label.public.access.mode.staticnat') }}</a-select-option>
                <a-select-option value="PORT_FORWARD">{{ $t('label.public.access.mode.portforward') }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item v-if="deployForm.assignpublicip && deployForm.ipmode === 'PORT_FORWARD'" :label="$t('label.private.ports')">
              <a-input v-model:value="deployForm.privateports" placeholder="80,443" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item :label="$t('label.allowed.cidr')">
          <a-input v-model:value="deployForm.allowedcidr" placeholder="0.0.0.0/0" />
        </a-form-item>
        <a-form-item :label="$t('label.quantity')">
          <a-input-number v-model:value="deployForm.count" :min="1" :max="1000" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, computed, getCurrentInstance, nextTick } from 'vue'
import { api } from '@/api'
import { RocketOutlined } from '@ant-design/icons-vue'

export default {
  name: 'ServiceCatalog',
  components: { RocketOutlined },
  setup () {
    const { proxy } = getCurrentInstance()
    const catalogItems = ref([])
    const zones = ref([])
    const serviceOfferings = ref([])
    const templates = ref([])
    const loading = ref(false)
    const deploying = ref(false)
    const searchKeyword = ref('')
    const selectedCategory = ref('')
    const deployModalVisible = ref(false)
    const selectedItem = ref(null)
    const batchOperationId = ref('')
    const batchOperationInputRef = ref(null)
    const batchStatus = ref(null)
    const batchLoading = ref(false)
    const poller = ref(null)

    const instanceColumns = [
      { title: '#', dataIndex: 'index', width: 70 },
      { title: 'Name', dataIndex: 'name' },
      { title: 'Status', dataIndex: 'status', width: 120 },
      { title: 'VM ID', dataIndex: 'vmid', width: 120 },
      { title: 'Private IP', dataIndex: 'privateip', width: 140 },
      { title: 'Public Endpoint', dataIndex: 'publicendpoint', width: 200 },
      { title: 'Updated', dataIndex: 'updated', width: 180 },
      { title: 'Error', dataIndex: 'error' }
    ]

    const deployForm = reactive({
      catalogitemid: '',
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

    const batchProgress = computed(() => {
      const total = batchStatus.value?.totalcount || 0
      const completed = batchStatus.value?.completedcount || 0
      const failed = batchStatus.value?.failedcount || 0
      if (!total) {
        return 0
      }
      return Math.round(((completed + failed) * 100) / total)
    })

    const batchProgressStatus = computed(() => {
      const state = batchStatus.value?.state
      if (state === 'FAILED') {
        return 'exception'
      }
      if (state === 'COMPLETED' || state === 'PARTIAL') {
        return 'success'
      }
      return 'active'
    })

    const getCategoryColor = (category) => {
      const colors = {
        DATABASE: 'blue',
        WEBSERVER: 'green',
        APPSTACK: 'magenta',
        CACHE: 'orange',
        MONITORING: 'cyan',
        MANAGEMENT: 'geekblue'
      }
      return colors[category] || 'default'
    }

    const getStateColor = (state) => {
      const mapped = {
        RUNNING: 'processing',
        COMPLETED: 'success',
        PARTIAL: 'warning',
        FAILED: 'error',
        PENDING: 'default'
      }
      return mapped[state] || 'default'
    }

    const formatDate = (value) => {
      if (!value) {
        return '-'
      }
      return new Date(value).toLocaleString()
    }

    const formatEndpoint = (record) => {
      if (!record.publicip) {
        return '-'
      }
      return record.publicport ? `${record.publicip}:${record.publicport}` : record.publicip
    }

    const extractDeployResponse = (response) => response.deploycatalogitemresponse || response
    const extractBatchStatusResponse = (response) => response.listcatalogdeploymentstatusresponse || response

    const stopPolling = () => {
      if (poller.value) {
        clearInterval(poller.value)
        poller.value = null
      }
    }

    const startPollingIfRunning = () => {
      stopPolling()
      if (batchStatus.value?.state === 'RUNNING') {
        poller.value = setInterval(() => {
          fetchBatchStatus(false)
        }, 5000)
      }
    }

    const fetchCatalog = () => {
      loading.value = true
      const params = {}
      if (selectedCategory.value) params.category = selectedCategory.value
      if (searchKeyword.value) params.keyword = searchKeyword.value

      api('listCatalogItems', params)
        .then(response => {
          catalogItems.value = response.listcatalogitemsresponse?.catalogitem || []
        })
        .finally(() => {
          loading.value = false
        })
    }

    const fetchZones = () => {
      api('listZones', { available: true }).then(response => {
        zones.value = response.listzonesresponse?.zone || []
      })
    }

    const fetchServiceOfferings = () => {
      api('listServiceOfferings', {}).then(response => {
        serviceOfferings.value = response.listserviceofferingsresponse?.serviceoffering || []
      })
    }

    const fetchTemplates = () => {
      api('listTemplates', { templatefilter: 'featured' }).then(response => {
        templates.value = response.listtemplatesresponse?.template || []
      })
    }

    const fetchBatchStatus = (showLoading = true) => {
      if (!batchOperationId.value) {
        return
      }

      if (showLoading) {
        batchLoading.value = true
      }

      api('listCatalogDeploymentStatus', { batchoperationid: batchOperationId.value })
        .then(response => {
          batchStatus.value = extractBatchStatusResponse(response)
          startPollingIfRunning()
        })
        .finally(() => {
          batchLoading.value = false
        })
    }

    const clearBatchStatus = () => {
      batchOperationId.value = ''
      batchStatus.value = null
      stopPolling()
    }

    const filterTemplateOption = (input, option) => {
      return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
    }

    const showDeployModal = (item) => {
      selectedItem.value = item
      deployForm.catalogitemid = item.id
      deployForm.name = item.name.toLowerCase().replace(/\s+/g, '-') + '-01'
      deployForm.privateports = item.defaultport ? String(item.defaultport) : ''
      deployModalVisible.value = true
    }

    const handleDeploy = () => {
      deploying.value = true
      api('deployCatalogItem', deployForm)
        .then(response => {
          const payload = extractDeployResponse(response)
          deployModalVisible.value = false
          if (payload && payload.batchoperationid) {
            batchOperationId.value = payload.batchoperationid
            fetchBatchStatus()
            nextTick(() => {
              if (batchOperationInputRef.value && batchOperationInputRef.value.focus) {
                batchOperationInputRef.value.focus()
              }
            })
            const copied = copyBatchOperationId(payload.batchoperationid)
            if (proxy && proxy.$notification) {
              proxy.$notification.success({
                message: proxy.$t ? proxy.$t('label.catalog.batch.tracker') : 'Batch Deployment Tracker',
                description: copied
                  ? 'Batch Operation ID: ' + payload.batchoperationid + ' (copied)'
                  : 'Batch Operation ID: ' + payload.batchoperationid
              })
            }
          }
        })
        .finally(() => {
          deploying.value = false
        })
    }

    const copyBatchOperationId = (value) => {
      if (!value || typeof navigator === 'undefined' || !navigator.clipboard || !navigator.clipboard.writeText) {
        return false
      }
      navigator.clipboard.writeText(value).catch(() => {})
      return true
    }

    onMounted(() => {
      fetchCatalog()
      fetchZones()
      fetchServiceOfferings()
      fetchTemplates()
    })

    onUnmounted(() => {
      stopPolling()
    })

    return {
      catalogItems,
      zones,
      serviceOfferings,
      templates,
      loading,
      deploying,
      searchKeyword,
      selectedCategory,
      deployModalVisible,
      selectedItem,
      deployForm,
      batchOperationId,
      batchOperationInputRef,
      batchStatus,
      batchLoading,
      instanceColumns,
      batchProgress,
      batchProgressStatus,
      getCategoryColor,
      getStateColor,
      fetchCatalog,
      fetchBatchStatus,
      clearBatchStatus,
      showDeployModal,
      handleDeploy,
      filterTemplateOption,
      formatDate,
      formatEndpoint
    }
  }
}
</script>

<style scoped>
.service-catalog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.catalog-toolbar,
.status-tracker {
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f4fbfb 100%);
  box-shadow: 0 8px 24px rgba(15, 118, 110, 0.08);
}

.category-group {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
}

.tracker-actions {
  display: flex;
  justify-content: flex-end;
}

.tracker-content {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tracker-metrics {
  margin-bottom: 4px;
}

.metric-box {
  background: #effaf9;
  border: 1px solid #d8f1ee;
  border-radius: 10px;
  padding: 10px;
}

.metric-success {
  background: #eefaf2;
  border-color: #caebd4;
}

.metric-danger {
  background: #fef1f1;
  border-color: #f7cece;
}

.metric-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.metric-value {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.catalog-grid {
  margin-top: 2px;
}

.catalog-card {
  height: 100%;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.08);
}

.card-header {
  margin-bottom: 6px;
  display: flex;
  gap: 4px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  line-height: 1.3;
}

.card-description {
  color: #475569;
  font-size: 13px;
  min-height: 58px;
}

.card-specs {
  background: #f1f5f9;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 10px;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-features {
  margin-top: 8px;
  min-height: 30px;
}

.instance-table {
  margin-top: 4px;
}

.error-cell {
  color: #b91c1c;
}

@media (max-width: 768px) {
  .tracker-actions {
    justify-content: flex-start;
    margin-top: 8px;
  }

  .card-description {
    min-height: 0;
  }
}
</style>
