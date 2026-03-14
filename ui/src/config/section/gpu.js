// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.

import { shallowRef, defineAsyncComponent } from 'vue'

export default {
  name: 'gpu',
  title: 'GPU Services',
  icon: 'thunderbolt-outlined',
  children: [
    {
      name: 'gpuinstance',
      title: 'GPU Instances',
      icon: 'deployment-unit-outlined',
      columns: ['name', 'provider', 'gpuprofileid', 'gpucount', 'state', 'zoneid', 'created'],
      details: ['name', 'id', 'provider', 'gpuprofileid', 'gpucount', 'state', 'zoneid', 'serviceofferingid', 'templateid', 'networkid', 'created', 'account', 'domainid'],
      searchFilters: ['provider', 'gpuprofileid', 'state'],
      actions: [
        {
          api: 'createGpuInstance',
          icon: 'plus-outlined',
          label: 'Create GPU Instance',
          listView: true,
          component: shallowRef(defineAsyncComponent(() => import('@/views/plugins/gpu/CreateGpuInstance.vue')))
        },
        {
          api: 'startGpuInstance',
          icon: 'caret-right-outlined',
          label: 'Start GPU Instance',
          dataView: true,
          args: ['id'],
          mapping: {
            id: {
              value: (record) => { return record.id }
            }
          }
        },
        {
          api: 'stopGpuInstance',
          icon: 'pause-outlined',
          label: 'Stop GPU Instance',
          dataView: true,
          args: ['id'],
          mapping: {
            id: {
              value: (record) => { return record.id }
            }
          }
        },
        {
          api: 'deleteGpuInstance',
          icon: 'delete-outlined',
          label: 'Delete GPU Instance',
          dataView: true,
          args: ['id'],
          mapping: {
            id: {
              value: (record) => { return record.id }
            }
          }
        }
      ]
    },
    {
      name: 'gpuprofile',
      title: 'GPU Profiles',
      icon: 'profile-outlined',
      columns: ['id', 'provider', 'gpuvendor', 'gpumodel', 'memorygb', 'maxgpucount', 'description'],
      details: ['id', 'name', 'provider', 'gpuvendor', 'gpumodel', 'memorygb', 'maxgpucount', 'description'],
      searchFilters: ['provider', 'keyword']
    },
    {
      name: 'gpumetric',
      title: 'GPU Monitoring',
      icon: 'dashboard-outlined',
      columns: ['gpuinstanceid', 'name', 'provider', 'gpuprofileid', 'gpuutilization', 'memoryutilization', 'memorytotalmb', 'powerwatts', 'temperaturec', 'timestamp'],
      details: ['gpuinstanceid', 'name', 'provider', 'gpuprofileid', 'gpuutilization', 'memoryutilization', 'memorytotalmb', 'powerwatts', 'temperaturec', 'timestamp'],
      searchFilters: ['id']
    },
    {
      name: 'gpuusage',
      title: 'GPU Usage Billing',
      icon: 'dollar-outlined',
      columns: ['gpuinstanceid', 'name', 'provider', 'gpuprofileid', 'gpucount', 'runninghours', 'hourlyrateusd', 'totalcostusd', 'startdate', 'enddate'],
      details: ['gpuinstanceid', 'name', 'provider', 'gpuprofileid', 'gpucount', 'runninghours', 'hourlyrateusd', 'totalcostusd', 'startdate', 'enddate'],
      searchFilters: ['id', 'provider', 'gpuprofileid']
    }
  ]
}
