// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.

import { shallowRef, defineAsyncComponent } from 'vue'

export default {
  name: 'catalog',
  title: 'label.catalog',
  icon: 'appstore-outlined',
  component: shallowRef(defineAsyncComponent(() => import('@/views/plugins/catalog/ServiceCatalog.vue'))),
  children: [
    {
      name: 'catalogitems',
      title: 'label.catalog.items',
      icon: 'appstore-outlined',
      columns: ['name', 'category', 'version', 'description', 'defaultport', 'mincpu', 'minmemorymb', 'minstoragegb'],
      details: ['name', 'id', 'category', 'version', 'description', 'defaultport', 'mincpu', 'minmemorymb', 'minstoragegb', 'features'],
      searchFilters: ['category'],
      actions: [
        {
          api: 'deployCatalogItem',
          icon: 'rocket-outlined',
          label: 'label.catalog.deploy',
          listView: true,
          dataView: true,
          component: shallowRef(defineAsyncComponent(() => import('@/views/plugins/catalog/DeployCatalogItem.vue')))
        }
      ]
    }
  ]
}
