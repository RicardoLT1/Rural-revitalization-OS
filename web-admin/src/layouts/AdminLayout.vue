<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BarChart3, Bell, Check, ChevronDown, ChevronLeft, ChevronRight, ClipboardCheck, FileClock, FileText, FolderKanban, LandPlot, LogOut, MapPinned, Menu, Search, Settings, UserRound, Users, X } from '@lucide/vue'
import { fetchGlobalSearch } from '../api/search'
import { fetchNotifications, readAllNotifications, readNotification, type AdminNotification } from '../api/notifications'
import { fetchSystemSettings, type SystemSettings } from '../api/settings'
import ComingSoonButton from '../components/ComingSoonButton.vue'
import { recentSearchResults, rememberSearchResult, toAdminSearchResults } from '../services/adminSearch'
import type { AdminSearchResult } from '../services/adminSearch'
import { useSessionStore } from '../stores/session'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const mobileOpen = ref(false)
const collapsed = ref(false)
const activePanel = ref<'search' | 'notifications' | 'user' | null>(null)
const searchQuery = ref('')
let searchTimer = 0
let searchRequestId = 0
const searchLoading = ref(false)
const searchError = ref('')
const searchPartial = ref(false)
const searchResults = ref<AdminSearchResult[]>([])
const recentResults = ref(recentSearchResults())
const notificationRows = ref<AdminNotification[]>([])
const notificationUnreadCount = ref(0)
const notificationsLoading = ref(false)
const notificationsLoaded = ref(false)
const notificationsError = ref('')
const platformContext = ref<Pick<SystemSettings, 'platformName' | 'villageName' | 'systemVersion'>>({ platformName: '乡耘 OS', villageName: '青耘村', systemVersion: 'v1.3-admin-pro' })

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
const unreadCount = computed(() => notificationUnreadCount.value)
function notificationKind(item: AdminNotification) {
  return item.type === 'WORKFLOW_OVERDUE' ? '风险提醒' : item.type === 'WORKFLOW_PENDING' ? '待审批' : '系统消息'
}
async function runSearch(value: string) {
  const query = value.trim()
  const requestId = ++searchRequestId
  if (!query) {
    searchResults.value = []
    searchError.value = ''
    searchPartial.value = false
    searchLoading.value = false
    return
  }
  searchLoading.value = true
  searchError.value = ''
  try {
    const result = await fetchGlobalSearch(query)
    if (requestId !== searchRequestId) return
    searchResults.value = toAdminSearchResults(result.items || [])
    searchPartial.value = Boolean(result.partial)
  } catch (reason) {
    if (requestId !== searchRequestId) return
    searchResults.value = []
    searchPartial.value = false
    searchError.value = reason instanceof Error ? reason.message : '搜索服务暂时不可用'
  } finally {
    if (requestId === searchRequestId) searchLoading.value = false
  }
}

function openSearch() {
  activePanel.value = 'search'
  if (searchQuery.value.trim()) void runSearch(searchQuery.value)
}

async function loadNotifications(force = false) {
  if ((!force && notificationsLoaded.value) || notificationsLoading.value) return
  notificationsLoading.value = true
  notificationsError.value = ''
  try {
    const result = await fetchNotifications(false, 6)
    notificationRows.value = result.items
    notificationUnreadCount.value = result.unreadCount
    notificationsLoaded.value = true
  } catch (reason) {
    notificationsError.value = reason instanceof Error ? reason.message : '通知读取失败'
  } finally { notificationsLoading.value = false }
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

function openFirstSearchResult() {
  if (searchResults.value[0]) void openSearchResult(searchResults.value[0])
}

async function openNotification(item: typeof notificationRows.value[number]) {
  if (!item.read) await readNotification(item.id)
  await loadNotifications(true)
  if (item.targetPath) await navigateResult(item.targetPath)
}

async function markAllRead() {
  await readAllNotifications()
  await loadNotifications(true)
}

async function loadPlatformContext() {
  try {
    const value = await fetchSystemSettings()
    platformContext.value = value
  } catch { /* 品牌上下文失败时保留本地安全文案，不阻断主页面。 */ }
}

function updatePlatformContext(event: Event) {
  const detail = (event as CustomEvent<SystemSettings>).detail
  if (detail) platformContext.value = detail
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
  searchTimer = window.setTimeout(() => { void runSearch(value) }, 220)
})

function signOut() {
  session.signOut()
  activePanel.value = null
  void router.replace('/login')
}

onMounted(() => {
  window.addEventListener('keydown', handleShortcut)
  window.addEventListener('xiangyun:workflow-updated', refreshNotifications)
  window.addEventListener('xiangyun:notifications-updated', refreshNotifications)
  window.addEventListener('xiangyun:settings-updated', updatePlatformContext)
  loadNotifications()
  loadPlatformContext()
})
onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleShortcut)
  window.removeEventListener('xiangyun:workflow-updated', refreshNotifications)
  window.removeEventListener('xiangyun:notifications-updated', refreshNotifications)
  window.removeEventListener('xiangyun:settings-updated', updatePlatformContext)
  window.clearTimeout(searchTimer)
})
</script>

<template>
  <div class="admin-shell" :class="{ 'sidebar-collapsed': collapsed }">
    <div v-if="mobileOpen" class="nav-scrim" @click="mobileOpen = false" />
    <aside class="sidebar" :class="{ open: mobileOpen }">
      <div class="brand-block">
        <span class="brand-seal">乡</span>
        <div class="brand-copy"><strong>{{ platformContext.platformName }}</strong><span>资源协同中枢</span></div>
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
        <RouterLink class="nav-tool-link" to="/notifications" @click="mobileOpen = false"><Bell :size="18" />通知中心<b v-if="unreadCount">{{ unreadCount > 9 ? '9+' : unreadCount }}</b></RouterLink>
        <RouterLink v-if="session.user?.role === 'ADMIN'" class="nav-tool-link" to="/settings" @click="mobileOpen = false"><Settings :size="18" />系统设置</RouterLink>
        <ComingSoonButton><FolderKanban :size="18" />数据分析</ComingSoonButton>
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
          <div><span>{{ platformContext.villageName }}运营协同平台</span><h1>{{ pageTitle }}</h1></div>
        </div>
        <div class="topbar-actions">
          <label class="search-box" @click="openSearch"><Search :size="17" /><input v-model="searchQuery" type="search" placeholder="搜索资源、流程、报表..." @focus="openSearch" @keydown.enter.prevent="openFirstSearchResult" /><kbd>Ctrl K</kbd></label>
          <button class="icon-button" type="button" :title="`${unreadCount} 条未读通知`" :aria-expanded="activePanel === 'notifications'" @click="openNotifications"><Bell :size="19" /><b v-if="unreadCount" class="notification-count">{{ unreadCount > 9 ? '9+' : unreadCount }}</b></button>
          <button class="topbar-user" type="button" :aria-expanded="activePanel === 'user'" @click="activePanel = activePanel === 'user' ? null : 'user'"><span>{{ session.user?.displayName?.slice(0, 1) }}</span><div><strong>{{ session.user?.displayName }}</strong><small>{{ platformContext.villageName }}</small></div><ChevronDown :size="15" /></button>
        </div>
      </header>
      <button v-if="activePanel" class="topbar-popover-scrim" type="button" aria-label="关闭浮层" @click="activePanel = null" />
      <section v-if="activePanel === 'search'" class="command-palette" role="dialog" aria-modal="true" aria-label="全局搜索">
        <header><Search :size="19" /><input v-model="searchQuery" autofocus placeholder="输入资源名称、流程编号、周报或用户" @keydown.enter.prevent="openFirstSearchResult" /><button type="button" @click="activePanel = null"><X :size="17" /></button></header>
        <div class="command-results"><div v-if="searchLoading" class="popover-state">正在检索当前村域的业务数据...</div><div v-else-if="searchError" class="popover-state search-error-state"><span>{{ searchError }}</span><button type="button" @click="runSearch(searchQuery)">重新搜索</button></div><template v-else-if="!searchQuery.trim()"><div v-if="recentResults.length" class="recent-search"><span>最近访问</span><button v-for="item in recentResults" :key="item.id" type="button" @click="openSearchResult(item)"><b>{{ item.type }}</b><div><strong>{{ item.title }}</strong><small>{{ item.meta }}</small></div><ChevronRight :size="15" /></button></div><div v-else class="search-hints"><span>可搜索</span><b>资源档案</b><b>审批流程</b><b>运营周报</b><b v-if="session.user?.role === 'ADMIN'">用户</b></div></template><button v-for="item in searchResults" v-else :key="item.id" type="button" @click="openSearchResult(item)"><span>{{ item.type }}</span><div><strong>{{ item.title }}</strong><small>{{ item.meta }}</small></div><ChevronRight :size="16" /></button><div v-if="searchQuery.trim() && !searchLoading && !searchError && !searchResults.length" class="popover-state">没有找到匹配结果，换个关键词试试</div></div>
        <footer><span v-if="searchPartial">已返回核心结果，用户目录暂时不可用</span><span v-else>Enter 打开首条结果</span><span>Esc 关闭</span></footer>
      </section>
      <section v-if="activePanel === 'notifications'" class="topbar-popover notification-popover">
        <header><div><span>运营提醒</span><h3>通知中心</h3></div><button type="button" :disabled="!unreadCount || notificationsLoading" @click="markAllRead"><Check :size="14" />全部已读</button></header>
        <div v-if="notificationsLoading" class="popover-state">正在读取账号通知...</div><div v-else-if="notificationsError" class="popover-state search-error-state"><span>{{ notificationsError }}</span><button type="button" @click="loadNotifications(true)">重新加载</button></div><div v-else-if="notificationRows.length" class="notification-list"><button v-for="item in notificationRows" :key="item.id" type="button" :class="{ read: item.read }" @click="openNotification(item)"><i /><div><span>{{ notificationKind(item) }}</span><strong>{{ item.title }}</strong><p>{{ item.content }}</p></div><ChevronRight :size="16" /></button></div><div v-else class="popover-state"><Check :size="22" />当前没有新的运营提醒</div>
        <footer class="notification-popover-footer"><RouterLink to="/notifications" @click="activePanel = null">查看全部通知<ChevronRight :size="15" /></RouterLink></footer>
      </section>
      <section v-if="activePanel === 'user'" class="topbar-popover user-popover">
        <header><span>{{ session.user?.displayName?.slice(0, 1) }}</span><div><strong>{{ session.user?.displayName }}</strong><small>{{ session.user?.username }} · {{ session.user?.role }}</small></div></header>
        <dl><div><dt>当前村域</dt><dd>{{ platformContext.villageName }}</dd></div><div><dt>系统版本</dt><dd>{{ platformContext.systemVersion }}</dd></div></dl>
        <button type="button" @click="navigateResult('/profile')"><UserRound :size="16" />个人中心<small>账号与安全</small></button><button class="logout-menu-item" type="button" @click="signOut"><LogOut :size="16" />退出登录</button>
      </section>
      <section class="page-content"><RouterView /></section>
    </main>
  </div>
</template>
