import { createRouter, createWebHistory } from 'vue-router'
import { useSessionStore } from '../stores/session'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
    {
      path: '/',
      component: () => import('../layouts/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue') },
        { path: 'approvals', name: 'approvals', component: () => import('../views/ApprovalsView.vue'), meta: { title: '审批工作台' } },
        { path: 'resources', name: 'resources', component: () => import('../views/ResourcesView.vue'), meta: { title: '资源档案' } },
        { path: 'resources/:id', name: 'resource-detail', component: () => import('../views/ResourcesView.vue'), meta: { title: '资源详情' } },
        { path: 'resource-map', name: 'resource-map', component: () => import('../views/ResourceMapView.vue'), meta: { title: '资源地图' } },
        { path: 'weekly-report', name: 'weekly-report', component: () => import('../views/WeeklyReportView.vue'), meta: { title: '周报管理' } },
        { path: 'notifications', name: 'notifications', component: () => import('../views/NotificationsView.vue'), meta: { title: '通知中心', roles: ['STAFF', 'ADMIN'] } },
        { path: 'profile', name: 'profile', component: () => import('../views/ProfileView.vue'), meta: { title: '个人中心', roles: ['STAFF', 'ADMIN'] } },
        { path: 'users', name: 'users', component: () => import('../views/UsersView.vue'), meta: { title: '用户与权限', roles: ['ADMIN'] } },
        { path: 'audit-logs', name: 'audit-logs', component: () => import('../views/AuditLogsView.vue'), meta: { title: '审计日志', roles: ['ADMIN'] } },
        { path: 'settings', name: 'settings', component: () => import('../views/SettingsView.vue'), meta: { title: '系统设置', roles: ['ADMIN'] } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  const session = useSessionStore()
  if (to.meta.public) return session.authenticated ? '/dashboard' : true
  if (!session.authenticated) return { path: '/login', query: { redirect: to.fullPath } }
  if (!session.canUseAdmin) return '/login'
  const roles = to.meta.roles as string[] | undefined
  if (roles && session.user && !roles.includes(session.user.role)) return '/dashboard'
  return true
})

export default router
