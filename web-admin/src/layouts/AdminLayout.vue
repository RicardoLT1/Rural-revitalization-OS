<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BarChart3, Bell, ChevronLeft, ClipboardCheck, FileText, LandPlot, LogOut, Menu, Search, Users, X } from '@lucide/vue'
import { useSessionStore } from '../stores/session'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const mobileOpen = ref(false)
const collapsed = ref(false)

const items = computed(() => [
  { to: '/dashboard', label: '运营看板', icon: BarChart3 },
  { to: '/approvals', label: '审批工作台', icon: ClipboardCheck },
  { to: '/resources', label: '资源档案', icon: LandPlot },
  { to: '/weekly-report', label: '周报草稿', icon: FileText },
  ...(session.user?.role === 'ADMIN' ? [{ to: '/users', label: '用户与权限', icon: Users }] : []),
])
const pageTitle = computed(() => items.value.find((item) => item.to === route.path)?.label || '乡耘工作台')

async function signOut() {
  await session.signOut()
  await router.replace('/login')
}
</script>

<template>
  <div class="admin-shell" :class="{ 'sidebar-collapsed': collapsed }">
    <div v-if="mobileOpen" class="nav-scrim" @click="mobileOpen = false" />
    <aside class="sidebar" :class="{ open: mobileOpen }">
      <div class="brand-block">
        <span class="brand-seal">乡</span>
        <div class="brand-copy"><strong>乡耘 OS</strong><span>运营协同中枢</span></div>
        <button class="icon-button mobile-close" type="button" title="关闭导航" @click="mobileOpen = false"><X :size="18" /></button>
      </div>
      <div class="field-index"><span>当前村域</span><strong>01 / {{ session.user?.villageId || '--' }}</strong></div>
      <nav class="main-nav" aria-label="主导航">
        <RouterLink v-for="item in items" :key="item.to" :to="item.to" @click="mobileOpen = false">
          <component :is="item.icon" :size="19" /><span>{{ item.label }}</span>
        </RouterLink>
      </nav>
      <div class="sidebar-foot">
        <div class="user-compact"><span>{{ session.user?.displayName?.slice(0, 1) }}</span><div><strong>{{ session.user?.displayName }}</strong><small>{{ session.user?.role }}</small></div></div>
        <button class="icon-button" type="button" title="退出登录" @click="signOut"><LogOut :size="18" /></button>
      </div>
    </aside>
    <main class="main-area">
      <header class="topbar">
        <div class="topbar-title">
          <button class="icon-button menu-button" type="button" title="打开导航" @click="mobileOpen = true"><Menu :size="20" /></button>
          <button class="icon-button collapse-button" type="button" title="收起侧栏" @click="collapsed = !collapsed"><ChevronLeft :size="18" /></button>
          <div><span>乡村运营协同平台</span><h1>{{ pageTitle }}</h1></div>
        </div>
        <div class="topbar-actions">
          <label class="search-box"><Search :size="17" /><input type="search" placeholder="搜索资源、流程" /></label>
          <button class="icon-button" type="button" title="通知"><Bell :size="19" /><i /></button>
        </div>
      </header>
      <section class="page-content"><RouterView /></section>
    </main>
  </div>
</template>
