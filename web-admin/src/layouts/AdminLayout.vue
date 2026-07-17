<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BarChart3, Bell, Check, ChevronDown, ChevronLeft, ChevronRight, ClipboardCheck, FileClock, FileText, FolderKanban, LandPlot, LogOut, MapPinned, Menu, Search, Settings, UserRound, Users, X } from '@lucide/vue'
import { fetchDashboard, fetchResources, fetchTodos, fetchWeeklyReports } from '../api/business'
import { fetchUsers } from '../api/admin'
import ComingSoonButton from '../components/ComingSoonButton.vue'
import { recentSearchResults, rememberSearchResult, searchAdminIndex } from '../services/adminSearch'
import type { AdminSearchResult } from '../services/adminSearch'
import { useSessionStore } from '../stores/session'
import type { ResourceItem, UserRow, WeeklyReport, WorkflowItem } from '../types/business'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const mobileOpen = ref(false)
const collapsed = ref(false)
const activePanel = ref<'search' | 'notifications' | 'user' | null>(null)
const searchQuery = ref('')
const searchTerm = ref('')
let searchTimer = 0
const searchLoading = ref(false)
const searchLoaded = ref(false)
const resourceIndex = ref<ResourceItem[]>([])
const workflowIndex = ref<WorkflowItem[]>([])
const reportIndex = ref<WeeklyReport[]>([])
const userIndex = ref<UserRow[]>([])
const recentResults = ref(recentSearchResults())
const notificationRows = ref<Array<{ id: string; title: string; detail: string; kind: string; to: string | { path: string; query?: Record<string, string> } }>>([])
function savedReadNotifications() {
  try { return new Set<string>(JSON.parse(localStorage.getItem('xiangyun.admin.read-notifications') || '[]') as string[]) }
  catch { return new Set<string>() }
}
const readNotifications = ref(savedReadNotifications())
const notificationsLoading = ref(false)
const notificationsLoaded = ref(false)

const items = computed(() => [
  { to: '/dashboard', label: '运营看板', icon: BarChart3 },
  { to: '/approvals', label: '审批工作台', icon: ClipboardCheck },
  { to: '/resources', label: '资源目录', icon: LandPlot },
  { to: '/resource-map', label: '资源地图', icon: MapPinned },
  { to: '/weekly-report', label: '周报管理', icon: FileText },
  ...(session.user?.role === 'ADMIN' ? [
    { to: '/users', label: '用户与权限', icon: Users },
    { to: '/audit-logs', label: '审计日志', icon: FileClock },
  ] : []),
])
const pageTitle = computed(() => String(route.meta.title || items.value.find((item) => item.to === route.path)?.label || '乡耘工作台'))
const unreadCount = computed(() => notificationRows.value.filter((item) => !readNotifications.value.has(item.id)).length)
const searchResults = computed(() => {
  return searchAdminIndex(searchTerm.value, { resources: resourceIndex.value, workflows: workflowIndex.value, reports: reportIndex.value, users: userIndex.value })
})

async function loadSearchIndex() {
  if (searchLoaded.value || searchLoading.value) return
  searchLoading.value = true
  const results = await Promise.allSettled([
    fetchResources({}), fetchTodos(), fetchWeeklyReports(),
    session.user?.role === 'ADMIN' ? fetchUsers() : Promise.resolve([] as UserRow[]),
  ])
  if (results[0].status === 'fulfilled') resourceIndex.value = results[0].value
  if (results[1].status === 'fulfilled') workflowIndex.value = results[1].value
  if (results[2].status === 'fulfilled') reportIndex.value = results[2].value
  if (results[3].status === 'fulfilled') userIndex.value = results[3].value
  searchLoaded.value = true
  searchLoading.value = false
}

async function openSearch() {
  activePanel.value = 'search'
  await loadSearchIndex()
}

async function loadNotifications(force = false) {
  if ((!force && notificationsLoaded.value) || notificationsLoading.value) return
  notificationsLoading.value = true
  const [todoResult, dashboardResult] = await Promise.allSettled([fetchTodos(), fetchDashboard(7)])
  const notices: typeof notificationRows.value = []
  if (todoResult.status === 'fulfilled') notices.push(...todoResult.value.filter((item) => item.status === 'PENDING').map((item) => ({ id: `todo-${item.processId || item.id}`, title: item.title || '待审批事项', detail: `流程 ${item.processId || item.id} 等待处理`, kind: '待审批', to: { path: '/approvals', query: { workflow: String(item.processId || item.id) } } })))
  if (dashboardResult.status === 'fulfilled') notices.push(...(dashboardResult.value?.risks || []).map((item) => ({ id: `risk-${item.id}`, title: item.title, detail: item.detail, kind: '风险提醒', to: { path: '/approvals', query: { status: 'PENDING' } } })))
  notificationRows.value = notices
  notificationsLoading.value = false
  notificationsLoaded.value = true
}

function refreshNotifications() {
  notificationRows.value = []
  loadNotifications(true)
}

async function openNotifications() {
  activePanel.value = activePanel.value === 'notifications' ? null : 'notifications'
  if (activePanel.value === 'notifications') await loadNotifications()
}

async function navigateResult(to: string | { path: string; query?: Record<string, string> }) {
  activePanel.value = null
  searchQuery.value = ''
  await router.push(to)
}

async function openSearchResult(item: AdminSearchResult) {
  recentResults.value = rememberSearchResult(item)
  await navigateResult(item.to)
}

async function openNotification(item: typeof notificationRows.value[number]) {
  const next = new Set(readNotifications.value)
  next.add(item.id)
  readNotifications.value = next
  localStorage.setItem('xiangyun.admin.read-notifications', JSON.stringify([...next]))
  await navigateResult(item.to)
}

function markAllRead() {
  readNotifications.value = new Set(notificationRows.value.map((item) => item.id))
  localStorage.setItem('xiangyun.admin.read-notifications', JSON.stringify([...readNotifications.value]))
}

function handleShortcut(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault()
    openSearch()
  }
  if (event.key === 'Escape') activePanel.value = null
}

watch(searchQuery, (value) => {
  window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => { searchTerm.value = value }, 180)
})

function signOut() {
  session.signOut()
  activePanel.value = null
  void router.replace('/login')
}

onMounted(() => {
  window.addEventListener('keydown', handleShortcut)
  window.addEventListener('xiangyun:workflow-updated', refreshNotifications)
  loadNotifications()
})
onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleShortcut)
  window.removeEventListener('xiangyun:workflow-updated', refreshNotifications)
  window.clearTimeout(searchTimer)
})
</script>

<template>
  <div class="admin-shell" :class="{ 'sidebar-collapsed': collapsed }">
    <div v-if="mobileOpen" class="nav-scrim" @click="mobileOpen = false" />
    <aside class="sidebar" :class="{ open: mobileOpen }">
      <div class="brand-block">
        <span class="brand-seal">乡</span>
        <div class="brand-copy"><strong>乡耘 OS</strong><span>资源协同中枢</span></div>
        <button class="icon-button mobile-close" type="button" title="关闭导航" @click="mobileOpen = false"><X :size="18" /></button>
      </div>
      <div class="field-index"><span>当前村域</span><strong>01 / {{ session.user?.villageId || '--' }}</strong></div>
      <nav class="main-nav" aria-label="主导航">
        <RouterLink v-for="item in items" :key="item.to" :to="item.to" :title="collapsed ? item.label : undefined" @click="mobileOpen = false">
          <component :is="item.icon" :size="19" /><span>{{ item.label }}</span>
        </RouterLink>
      </nav>
      <div class="nav-extension" :aria-hidden="collapsed">
        <span>管理工具</span>
        <ComingSoonButton><FolderKanban :size="18" />数据分析</ComingSoonButton>
        <ComingSoonButton><Settings :size="18" />系统设置</ComingSoonButton>
      </div>
      <div class="sidebar-foot">
        <div class="user-compact"><span>{{ session.user?.displayName?.slice(0, 1) }}</span><div><strong>{{ session.user?.displayName }}</strong><small>{{ session.user?.role }}</small></div></div>
        <button class="icon-button" type="button" title="退出登录" @click="signOut"><LogOut :size="18" /></button>
      </div>
    </aside>
    <main class="main-area">
      <header class="topbar">
        <div class="topbar-title">
          <button class="icon-button menu-button" type="button" title="打开导航" @click="mobileOpen = true"><Menu :size="20" /></button>
          <button class="icon-button collapse-button" type="button" :title="collapsed ? '展开侧栏' : '收起侧栏'" :aria-expanded="!collapsed" @click="collapsed = !collapsed"><ChevronLeft :size="18" /></button>
          <div><span>乡村运营协同平台</span><h1>{{ pageTitle }}</h1></div>
        </div>
        <div class="topbar-actions">
          <label class="search-box" @click="openSearch"><Search :size="17" /><input v-model="searchQuery" type="search" placeholder="搜索资源、流程、报表..." @focus="openSearch" /><kbd>Ctrl K</kbd></label>
          <button class="icon-button" type="button" :title="`${unreadCount} 条未读通知`" :aria-expanded="activePanel === 'notifications'" @click="openNotifications"><Bell :size="19" /><i v-if="unreadCount" /><b v-if="unreadCount" class="notification-count">{{ unreadCount > 9 ? '9+' : unreadCount }}</b></button>
          <button class="topbar-user" type="button" :aria-expanded="activePanel === 'user'" @click="activePanel = activePanel === 'user' ? null : 'user'"><span>{{ session.user?.displayName?.slice(0, 1) }}</span><div><strong>{{ session.user?.displayName }}</strong><small>青禾村</small></div><ChevronDown :size="15" /></button>
        </div>
      </header>
      <button v-if="activePanel" class="topbar-popover-scrim" type="button" aria-label="关闭浮层" @click="activePanel = null" />
      <section v-if="activePanel === 'search'" class="command-palette" role="dialog" aria-modal="true" aria-label="全局搜索">
        <header><Search :size="19" /><input v-model="searchQuery" autofocus placeholder="输入资源名称、流程编号、周报或用户" /><button type="button" @click="activePanel = null"><X :size="17" /></button></header>
        <div class="command-results"><div v-if="searchLoading" class="popover-state">正在建立搜索索引...</div><template v-else-if="!searchQuery.trim()"><div v-if="recentResults.length" class="recent-search"><span>最近访问</span><button v-for="item in recentResults" :key="item.id" type="button" @click="openSearchResult(item)"><b>{{ item.type }}</b><div><strong>{{ item.title }}</strong><small>{{ item.meta }}</small></div><ChevronRight :size="15" /></button></div><div v-else class="search-hints"><span>可搜索</span><b>资源档案</b><b>审批流程</b><b>运营周报</b><b v-if="session.user?.role === 'ADMIN'">用户</b></div></template><button v-for="item in searchResults" v-else :key="item.id" type="button" @click="openSearchResult(item)"><span>{{ item.type }}</span><div><strong>{{ item.title }}</strong><small>{{ item.meta }}</small></div><ChevronRight :size="16" /></button><div v-if="searchQuery.trim() && !searchLoading && !searchResults.length" class="popover-state">没有找到匹配结果，换个关键词试试</div></div>
        <footer><span>Enter 打开结果</span><span>Esc 关闭</span></footer>
      </section>
      <section v-if="activePanel === 'notifications'" class="topbar-popover notification-popover">
        <header><div><span>运营提醒</span><h3>通知中心</h3></div><button type="button" :disabled="!notificationRows.length" @click="markAllRead"><Check :size="14" />全部已读</button></header>
        <div v-if="notificationsLoading" class="popover-state">正在汇总待办与风险...</div><div v-else-if="notificationRows.length" class="notification-list"><button v-for="item in notificationRows" :key="item.id" type="button" :class="{ read: readNotifications.has(item.id) }" @click="openNotification(item)"><i /><div><span>{{ item.kind }}</span><strong>{{ item.title }}</strong><p>{{ item.detail }}</p></div><ChevronRight :size="16" /></button></div><div v-else class="popover-state"><Check :size="22" />当前没有新的运营提醒</div>
      </section>
      <section v-if="activePanel === 'user'" class="topbar-popover user-popover">
        <header><span>{{ session.user?.displayName?.slice(0, 1) }}</span><div><strong>{{ session.user?.displayName }}</strong><small>{{ session.user?.username }} · {{ session.user?.role }}</small></div></header>
        <dl><div><dt>当前村域</dt><dd>{{ session.user?.villageId || '--' }}</dd></div><div><dt>系统版本</dt><dd>v2.1.0</dd></div></dl>
        <button type="button" disabled><UserRound :size="16" />个人信息<small>即将上线</small></button><button class="logout-menu-item" type="button" @click="signOut"><LogOut :size="16" />退出登录</button>
      </section>
      <section class="page-content"><RouterView /></section>
    </main>
  </div>
</template>
