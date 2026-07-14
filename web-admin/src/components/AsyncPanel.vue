<script setup lang="ts">
import { AlertCircle, Inbox, RefreshCw } from '@lucide/vue'

withDefaults(defineProps<{
  loading?: boolean
  error?: string
  empty?: boolean
  emptyText?: string
  compact?: boolean
  skeletonRows?: number
}>(), { skeletonRows: 3 })
defineEmits<{ retry: [] }>()
</script>

<template>
  <div v-if="loading" class="async-panel skeleton-panel" :class="{ compact }" aria-label="正在加载">
    <i class="skeleton-title" /><i v-for="index in skeletonRows" :key="index" class="skeleton-row" :style="{ '--row-width': `${96 - index * 8}%` }" />
  </div>
  <div v-else-if="error" class="async-panel error" :class="{ compact }"><AlertCircle :size="22" /><strong>读取失败</strong><span>{{ error }}</span><button type="button" @click="$emit('retry')"><RefreshCw :size="14" />重新加载</button></div>
  <div v-else-if="empty" class="async-panel" :class="{ compact }"><Inbox :size="22" /><strong>{{ emptyText || '暂无数据' }}</strong><span>有新内容时会显示在这里</span></div>
  <slot v-else />
</template>
