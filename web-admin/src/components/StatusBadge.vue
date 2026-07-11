<script setup lang="ts">
const props = defineProps<{ status?: string }>()

const labels: Record<string, string> = {
  PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', PUBLISHED: '已发布', OFFLINE: '已下架',
}
</script>

<template><span class="status-badge" :class="(props.status || '').toLowerCase()">{{ labels[props.status || ''] || props.status || '未知' }}</span></template>
