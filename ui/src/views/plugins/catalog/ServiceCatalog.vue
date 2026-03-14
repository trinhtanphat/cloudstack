<template>
  <div class="service-catalog">
    <!-- Category Filter -->
    <a-row :gutter="16" style="margin-bottom: 20px">
      <a-col :span="12">
        <a-input-search
          v-model:value="searchKeyword"
          :placeholder="$t('label.catalog.search')"
          @search="fetchCatalog"
          style="width: 100%"
        />
      </a-col>
      <a-col :span="12">
        <a-radio-group v-model:value="selectedCategory" @change="fetchCatalog">
          <a-radio-button value="">All</a-radio-button>
          <a-radio-button value="DATABASE">Databases</a-radio-button>
          <a-radio-button value="WEBSERVER">Web Servers</a-radio-button>
          <a-radio-button value="APPSTACK">App Stacks</a-radio-button>
          <a-radio-button value="CACHE">Cache</a-radio-button>
          <a-radio-button value="MONITORING">Monitoring</a-radio-button>
          <a-radio-button value="MANAGEMENT">Management</a-radio-button>
        </a-radio-group>
      </a-col>
    </a-row>

    <!-- Catalog Grid -->
    <a-row :gutter="[16, 16]">
      <a-col
:xs="24"
:sm="12"
:md="8"
:lg="6"
v-for="item in catalogItems"
:key="item.id">
        <a-card hoverable class="catalog-card">
          <template #title>
            <div class="card-header">
              <a-tag :color="getCategoryColor(item.category)">{{ item.category }}</a-tag>
              <a-tag v-if="item.popular" color="gold">Popular</a-tag>
            </div>
            <h3>{{ item.name }}</h3>
          </template>

          <p class="card-description">{{ item.description }}</p>

          <div class="card-specs">
            <a-space direction="vertical" size="small" style="width: 100%">
              <div><strong>Version:</strong> {{ item.version }}</div>
              <div><strong>Port:</strong> {{ item.defaultport }}</div>
              <div><strong>Min specs:</strong> {{ item.mincpu }} CPU, {{ item.minmemorymb }}MB RAM, {{ item.minstoragegb }}GB</div>
            </a-space>
          </div>

          <div class="card-features" v-if="item.features && item.features.length">
            <a-tag v-for="feature in item.features.slice(0, 3)" :key="feature" size="small">{{ feature }}</a-tag>
          </div>

          <template #actions>
            <a-button type="primary" @click="showDeployModal(item)">
              <template #icon><rocket-outlined /></template>
              Deploy
            </a-button>
          </template>
        </a-card>
      </a-col>
    </a-row>

    <!-- Deploy Modal -->
    <a-modal
      v-model:open="deployModalVisible"
      :title="'Deploy ' + (selectedItem ? selectedItem.name : '')"
      @ok="handleDeploy"
      :confirmLoading="deploying">
      <a-form layout="vertical">
        <a-form-item label="Instance Name" required>
          <a-input v-model:value="deployForm.name" placeholder="my-database-01" />
        </a-form-item>
        <a-form-item label="Zone" required>
          <a-select v-model:value="deployForm.zoneid">
            <a-select-option v-for="zone in zones" :key="zone.id" :value="zone.id">
              {{ zone.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Service Offering" required>
          <a-select v-model:value="deployForm.serviceofferingid">
            <a-select-option v-for="offering in serviceOfferings" :key="offering.id" :value="offering.id">
              {{ offering.name }} ({{ offering.cpunumber }} CPU, {{ offering.memory }}MB)
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Template" required>
          <a-select v-model:value="deployForm.templateid" show-search :filter-option="filterTemplateOption">
            <a-select-option v-for="template in templates" :key="template.id" :value="template.id">
              {{ template.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Assign Public IP">
          <a-switch v-model:checked="deployForm.assignpublicip" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Public Access Mode">
              <a-select v-model:value="deployForm.ipmode" :disabled="!deployForm.assignpublicip">
                <a-select-option value="STATIC_NAT">Dedicated IP</a-select-option>
                <a-select-option value="PORT_FORWARD">Shared IP + random port</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item v-if="deployForm.assignpublicip && deployForm.ipmode === 'PORT_FORWARD'" label="Private Ports">
              <a-input v-model:value="deployForm.privateports" placeholder="80,443" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="Allowed CIDR">
          <a-input v-model:value="deployForm.allowedcidr" placeholder="0.0.0.0/0" />
        </a-form-item>
        <a-form-item label="Quantity">
          <a-input-number v-model:value="deployForm.count" :min="1" :max="1000" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { api } from '@/api'
import { RocketOutlined } from '@ant-design/icons-vue'

export default {
  name: 'ServiceCatalog',
  components: { RocketOutlined },
  setup () {
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

    const getCategoryColor = (category) => {
      const colors = {
        DATABASE: 'blue',
        WEBSERVER: 'green',
        APPSTACK: 'purple',
        CACHE: 'orange',
        MONITORING: 'cyan',
        MANAGEMENT: 'geekblue'
      }
      return colors[category] || 'default'
    }

    const fetchCatalog = () => {
      loading.value = true
      const params = {}
      if (selectedCategory.value) params.category = selectedCategory.value
      if (searchKeyword.value) params.keyword = searchKeyword.value

      api('listCatalogItems', params).then(response => {
        catalogItems.value = response.listcatalogitemsresponse?.catalogitem || []
      }).finally(() => {
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
      api('deployCatalogItem', deployForm).then(response => {
        deployModalVisible.value = false
      }).catch(error => {
        console.error('Deployment failed', error)
      }).finally(() => {
        deploying.value = false
      })
    }

    onMounted(() => {
      fetchCatalog()
      fetchZones()
      fetchServiceOfferings()
      fetchTemplates()
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
      getCategoryColor,
      fetchCatalog,
      showDeployModal,
      handleDeploy,
      filterTemplateOption
    }
  }
}
</script>

<style scoped>
.catalog-card {
  height: 100%;
}
.card-header {
  margin-bottom: 4px;
}
.card-description {
  color: #666;
  font-size: 13px;
  margin-bottom: 12px;
}
.card-specs {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  margin-bottom: 12px;
  font-size: 12px;
}
.card-features {
  margin-top: 8px;
}
</style>
