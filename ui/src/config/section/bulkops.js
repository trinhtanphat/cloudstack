// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.

import { shallowRef, defineAsyncComponent } from 'vue'

export default {
  name: 'bulkops',
  title: 'label.bulk.operations',
  icon: 'cluster-outlined',
  children: [
    {
      name: 'bulkjob',
      title: 'label.bulk.jobs',
      icon: 'thunderbolt-outlined',
      columns: ['jobid', 'jobtype', 'status', 'totalcount', 'completedcount', 'failedcount', 'progress', 'created', 'completed'],
      details: ['jobid', 'jobtype', 'status', 'totalcount', 'completedcount', 'failedcount', 'progress', 'created', 'completed'],
      searchFilters: ['status', 'jobtype'],
      actions: [
        {
          api: 'bulkDeployVirtualMachines',
          icon: 'plus-outlined',
          label: 'label.bulk.deploy.vms',
          listView: true,
          component: shallowRef(defineAsyncComponent(() => import('@/views/plugins/bulk/BulkDeployVMs.vue')))
        },
        {
          api: 'bulkAllocatePublicIpAddresses',
          icon: 'global-outlined',
          label: 'label.bulk.allocate.ips',
          listView: true,
          args: ['count', 'zoneid', 'networkid', 'assigntovms']
        }
      ]
    }
  ]
}
