<template>
  <div>
    <a-card title="BigQuery Reservation Details" :bordered="false">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="Name">{{ resource.name }}</a-descriptions-item>
        <a-descriptions-item label="Slot Capacity">{{ resource.slotcapacity }}</a-descriptions-item>
        <a-descriptions-item label="Location">{{ resource.location }}</a-descriptions-item>
        <a-descriptions-item label="Created">{{ resource.created }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="Capacity Commitments" :bordered="false" style="margin-top: 16px" v-if="resource.commitments">
      <a-table :columns="commitmentColumns" :data-source="resource.commitments" :pagination="false" size="small" />
    </a-card>

    <a-card title="Assignments" :bordered="false" style="margin-top: 16px" v-if="resource.assignments">
      <a-table :columns="assignmentColumns" :data-source="resource.assignments" :pagination="false" size="small" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'GcpBigQueryReservationTab',
  props: {
    resource: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      commitmentColumns: [
        { title: 'Name', dataIndex: 'name', key: 'name' },
        { title: 'Slot Count', dataIndex: 'slotCount', key: 'slotCount' },
        { title: 'Plan', dataIndex: 'plan', key: 'plan' },
        { title: 'State', dataIndex: 'state', key: 'state' }
      ],
      assignmentColumns: [
        { title: 'Assignee', dataIndex: 'assignee', key: 'assignee' },
        { title: 'Job Type', dataIndex: 'jobType', key: 'jobType' },
        { title: 'State', dataIndex: 'state', key: 'state' }
      ]
    }
  }
}
</script>
