// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.

import { shallowRef, defineAsyncComponent } from 'vue'

export default {
  name: 'dbaas',
  title: 'label.dbaas',
  icon: 'database-outlined',
  children: [
    {
      name: 'dbaasinstance',
      title: 'label.dbaas.instances',
      icon: 'database-outlined',
      columns: ['name', 'dbengine', 'dbversion', 'state', 'ipaddress', 'publicipaddress', 'port', 'cpucores', 'memorymb', 'storagesizegb', 'created'],
      details: ['name', 'id', 'dbengine', 'dbversion', 'state', 'ipaddress', 'publicipaddress', 'port', 'adminusername', 'connectionstring', 'cpucores', 'memorymb', 'storagesizegb', 'backupenabled', 'highavailability', 'created'],
      searchFilters: ['dbengine', 'state'],
      actions: [
        {
          api: 'createDatabaseInstance',
          icon: 'plus-outlined',
          label: 'label.dbaas.create',
          listView: true,
          component: shallowRef(defineAsyncComponent(() => import('@/views/plugins/dbaas/CreateDatabaseInstance.vue')))
        },
        {
          api: 'scaleDatabaseInstance',
          icon: 'arrows-alt-outlined',
          label: 'label.dbaas.scale',
          dataView: true,
          args: ['id', 'serviceofferingid'],
          mapping: {
            id: {
              value: (record) => { return record.id }
            }
          }
        },
        {
          api: 'restartDatabaseInstance',
          icon: 'reload-outlined',
          label: 'label.dbaas.restart',
          dataView: true,
          args: ['id'],
          mapping: {
            id: {
              value: (record) => { return record.id }
            }
          }
        },
        {
          api: 'createDatabaseBackup',
          icon: 'save-outlined',
          label: 'label.dbaas.backup',
          dataView: true,
          args: ['dbinstanceid', 'backuptype'],
          mapping: {
            dbinstanceid: {
              value: (record) => { return record.id }
            }
          }
        },
        {
          api: 'deleteDatabaseInstance',
          icon: 'delete-outlined',
          label: 'label.dbaas.delete',
          dataView: true,
          args: ['id', 'expunge'],
          mapping: {
            id: {
              value: (record) => { return record.id }
            }
          }
        }
      ]
    },
    {
      name: 'dbaasoffering',
      title: 'label.dbaas.offerings',
      icon: 'shop-outlined',
      columns: ['name', 'dbengine', 'dbversion', 'description', 'defaultport', 'mincpu', 'minmemorymb', 'minstoragegb'],
      details: ['name', 'id', 'dbengine', 'dbversion', 'description', 'defaultport', 'mincpu', 'minmemorymb', 'minstoragegb'],
      searchFilters: ['dbengine'],
      actions: []
    },
    {
      name: 'dbaasbackup',
      title: 'label.dbaas.backups',
      icon: 'cloud-download-outlined',
      columns: ['id', 'dbinstancename', 'backuptype', 'status', 'sizebytes', 'created', 'expires'],
      details: ['id', 'dbinstanceid', 'dbinstancename', 'backuptype', 'status', 'sizebytes', 'created', 'expires'],
      actions: [
        {
          api: 'restoreDatabaseBackup',
          icon: 'undo-outlined',
          label: 'label.dbaas.restore',
          dataView: true,
          args: ['backupid', 'dbinstanceid'],
          mapping: {
            backupid: {
              value: (record) => { return record.id }
            }
          }
        }
      ]
    }
  ]
}
