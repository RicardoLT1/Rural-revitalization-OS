<script setup lang="ts">
import { AlertCircle, Inbox, LoaderCircle, RefreshCw } from '@lucide/vue'

defineProps<{ loading?: boolean; error?: string; empty?: boolean; emptyText?: string }>()
defineEmits<{ retry: [] }>()
</script>

<template>
  <div v-if="loading" class="page-state"><LoaderCircle class="spin" :size="24" /><strong>正在读取业务数据</strong><span>请稍候</span></div>
  <div v-else-if="error" class="page-state error"><AlertCircle :size="24" /><strong>数据读取失败</strong><span>{{ error }}</span><button type="button" @click="$emit('retry')"><RefreshCw :size="15" />重新加载</button></div>
  <div v-else-if="empty" class="page-state"><Inbox :size="24" /><strong>{{ emptyText || '暂无数据' }}</strong><span>调整筛选条件后再试</span></div>
  <slot v-else />
</template>
