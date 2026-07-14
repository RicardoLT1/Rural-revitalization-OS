<script setup lang="ts">
const props = defineProps<{ status?: string }>()

const labels: Record<string, string> = {
  PENDING: '待审批', MATERIAL_REQUIRED: '待补材料', APPROVED: '已通过', REJECTED: '已驳回', PUBLISHED: '已发布', OFFLINE: '已下架',
  DONE: '已完成', DOING: '处理中', COMPLETED: '已完成',
}
</script>

<template><span class="status-badge" :class="(props.status || '').toLowerCase()">{{ labels[props.status || ''] || props.status || '未知' }}</span></template>
