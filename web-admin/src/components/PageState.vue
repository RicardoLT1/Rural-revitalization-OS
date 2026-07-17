<script setup lang="ts">
import { computed } from 'vue'
import { AlertCircle, Inbox, RefreshCw } from '@lucide/vue'

const props = defineProps<{ loading?: boolean; error?: string; empty?: boolean; emptyText?: string }>()
defineEmits<{ retry: [] }>()

const friendlyError = computed(() => {
  const error = props.error || ''
  if (/timeout/i.test(error)) return '服务响应超时，请确认 MySQL、Redis 与后端业务服务均已启动。'
  if (/network|connect|failed to fetch|无法连接/i.test(error)) return '无法连接业务服务，请确认后端服务正在运行。'
  return error || '业务服务暂时不可用，请稍后重试。'
})
</script>

<template>
  <div v-if="loading" class="page-state page-skeleton" aria-label="正在读取业务数据" aria-live="polite">
    <header class="page-loading-heading">
      <span class="page-loading-icon"><RefreshCw class="spin" :size="18" /></span>
      <div>
        <strong>正在汇总业务数据</strong>
        <span>正在连接资源、审批与运营统计</span>
      </div>
      <b>LIVE</b>
    </header>
    <section class="page-loading-rows" aria-hidden="true"><i /><i /><i /><i /></section>
  </div>
  <div v-else-if="error" class="page-state error">
    <AlertCircle :size="28" />
    <strong>业务数据暂时不可用</strong>
    <span>{{ friendlyError }}</span>
    <small>您仍可使用左侧导航，或从用户菜单安全退出登录。</small>
    <button type="button" @click="$emit('retry')"><RefreshCw :size="15" />重新加载</button>
  </div>
  <div v-else-if="empty" class="page-state">
    <Inbox :size="26" />
    <strong>{{ emptyText || '暂无数据' }}</strong>
    <span>调整筛选条件后再试</span>
  </div>
  <slot v-else />
</template>
