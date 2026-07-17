<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { BellRing, CheckCheck, ChevronRight, Clock3, ShieldAlert } from '@lucide/vue'
import PageState from '../components/PageState.vue'
import { fetchNotifications, readAllNotifications, readNotification, type AdminNotification } from '../api/notifications'

const router = useRouter()
const rows = ref<AdminNotification[]>([])
const loading = ref(true)
const error = ref('')
const unreadOnly = ref(false)
const unreadCount = ref(0)
const total = ref(0)
const acting = ref(false)

function kind(item: AdminNotification) {
  return item.type === 'WORKFLOW_OVERDUE' ? '风险提醒' : item.type === 'WORKFLOW_PENDING' ? '待审批' : '系统消息'
}
function formatTime(value: string) { return new Date(value).toLocaleString('zh-CN', { hour12: false }) }

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await fetchNotifications(unreadOnly.value, 50)
    rows.value = result.items
    unreadCount.value = result.unreadCount
    total.value = result.total
  } catch (reason) { error.value = reason instanceof Error ? reason.message : '通知读取失败' }
  finally { loading.value = false }
}

async function changeFilter(value: boolean) { unreadOnly.value = value; await load() }

async function open(item: AdminNotification) {
  if (!item.read) await readNotification(item.id)
  window.dispatchEvent(new CustomEvent('xiangyun:notifications-updated'))
  if (item.targetPath) await router.push(item.targetPath)
  else await load()
}

async function markAll() {
  if (!unreadCount.value || acting.value) return
  acting.value = true
  try {
    await readAllNotifications()
    window.dispatchEvent(new CustomEvent('xiangyun:notifications-updated'))
    await load()
  } finally { acting.value = false }
}

onMounted(load)
</script>

<template>
  <div class="business-page notification-center-page">
    <section class="page-intro"><div><p>按账号留存 · 跨设备同步</p><h2>通知中心</h2></div><div class="notification-summary"><strong>{{ unreadCount }}</strong><span>条未读</span><small>共 {{ total }} 条有效提醒</small></div></section>
    <section class="notification-toolbar"><div class="view-tabs"><button type="button" :class="{ active: !unreadOnly }" @click="changeFilter(false)"><BellRing :size="16" />全部通知</button><button type="button" :class="{ active: unreadOnly }" @click="changeFilter(true)"><Clock3 :size="16" />只看未读</button></div><button class="secondary-button" type="button" :disabled="!unreadCount || acting" @click="markAll"><CheckCheck :size="16" />全部标为已读</button></section>
    <PageState :loading="loading" :error="error" :empty="!rows.length" :empty-text="unreadOnly ? '没有未读通知' : '当前没有有效通知'" @retry="load">
      <section class="notification-ledger">
        <button v-for="item in rows" :key="item.id" type="button" :class="{ read: item.read, risk: item.type === 'WORKFLOW_OVERDUE' }" @click="open(item)">
          <i><ShieldAlert v-if="item.type === 'WORKFLOW_OVERDUE'" :size="19" /><BellRing v-else :size="19" /></i>
          <div><span>{{ kind(item) }}<b v-if="!item.read">未读</b></span><strong>{{ item.title }}</strong><p>{{ item.content }}</p><small>{{ formatTime(item.createdAt) }}</small></div>
          <ChevronRight :size="19" />
        </button>
      </section>
    </PageState>
  </div>
</template>
