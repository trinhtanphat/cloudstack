<template>
  <div>
    <a-card title="VPC Network Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Routing Mode">{{ resource.routingmode }}</a-descriptions-item>
        <a-descriptions-item label="Auto Create Subnets">
          <a-tag :color="resource.autocreatesubnetworks ? 'green' : 'blue'">
            {{ resource.autocreatesubnetworks ? 'Auto' : 'Custom' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Status">
          <a-tag color="green">{{ resource.status }}</a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-tabs default-active-key="subnets" style="margin-top: 16px">
      <a-tab-pane key="subnets" tab="Subnets">
        <a-table :columns="subnetColumns" :data-source="subnets" :pagination="false" size="small" />
      </a-tab-pane>
      <a-tab-pane key="firewall" tab="Firewall Rules">
        <a-table :columns="firewallColumns" :data-source="firewallRules" :pagination="false" size="small" />
      </a-tab-pane>
      <a-tab-pane key="routes" tab="Routes">
        <a-table :columns="routeColumns" :data-source="routes" :pagination="false" size="small" />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
export default {
  name: 'GcpNetworkingTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      subnets: [],
      firewallRules: [],
      routes: [],
      subnetColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Region', dataIndex: 'region', key: 'region' },
        { title: 'IP Range', dataIndex: 'ipCidrRange', key: 'ipCidrRange' },
        { title: 'Private Access', dataIndex: 'privateIpGoogleAccess', key: 'privateIpGoogleAccess' }
      ],
      firewallColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Direction', dataIndex: 'direction', key: 'direction' },
        { title: 'Priority', dataIndex: 'priority', key: 'priority' },
        { title: 'Source Ranges', dataIndex: 'sourceRanges', key: 'sourceRanges' }
      ],
      routeColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Destination', dataIndex: 'destRange', key: 'destRange' },
        { title: 'Next Hop', dataIndex: 'nextHop', key: 'nextHop' },
        { title: 'Priority', dataIndex: 'priority', key: 'priority' }
      ]
    }
  }
}
</script>
