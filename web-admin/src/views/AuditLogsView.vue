<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { CheckCircle2, Copy, Download, Eye, FileClock, Search, ShieldAlert, X, XCircle } from '@lucide/vue'
import { exportAuditLogs, fetchAuditLogs } from '../api/admin'
import PagePager from '../components/PagePager.vue'
import PageState from '../components/PageState.vue'
import type { AdminAuditLog } from '../types/business'

const rows = ref<AdminAuditLog[]>([])
const page = ref(1)
const pageSize = 12
const total = ref(0)
const totalPages = ref(0)
const keyword = ref('')
const module = ref('ALL')
const result = ref('ALL')
const startDate = ref('')
const endDate = ref('')
const loading = ref(true)
const exporting = ref(false)
const exportMessage = ref('')
const error = ref('')
const selected = ref<AdminAuditLog | null>(null)
const copied = ref(false)

const actionLabels: Record<string, string> = {
  CREATE_RESOURCE: '新建资源',
  UPDATE_RESOURCE: '更新资源',
  DELETE_RESOURCE: '删除资源',
  PUBLISH_RESOURCE: '发布资源',
  OFFLINE_RESOURCE: '下架资源',
  CHANGE_INVESTMENT_STATUS: '调整招商状态',
  APPROVE_WORKFLOW: '通过审批',
  REJECT_WORKFLOW: '驳回审批',
  SUPPLEMENT_MATERIAL: '要求补充材料',
  PROCESS_WORKFLOW_ACTION: '处理流程节点',
  CREATE_WEEKLY_REPORT: '生成周报',
  CREATE_TODO: '创建待办',
  COMPLETE_TODO: '完成待办',
  CREATE_USER: '新增用户',
  UPDATE_USER: '更新用户',
  DELETE_USER: '停用并删除用户',
  ENABLE_USER: '启用用户',
  DISABLE_USER: '停用用户',
  RESET_USER_PASSWORD: '重置用户密码',
  ASSIGN_USER_ROLE: '调整用户角色',
  CREATE_ROLE: '新增角色',
  UPDATE_ROLE: '更新角色',
  ACCESS_DENIED: '拒绝越权访问',
  LOGIN_SUCCESS: '登录成功',
  LOGIN_FAILURE: '登录失败',
  LOGOUT: '退出登录',
}

const moduleLabels: Record<string, string> = {
  RESOURCE: '资源管理',
  WORKFLOW: '审批流程',
  REPORT: '周报管理',
  TODO: '待办管理',
  USER: '用户与权限',
  SECURITY: '安全边界',
}

const fieldLabels: Record<string, string> = {
  id: '编号', name: '名称', category: '资源类型', address: '地址', area: '面积',
  annualEstimate: '年收益预估', investmentStatus: '招商状态', intro: '资源简介',
  owner: '权属单位', contact: '联系方式', ownershipStatus: '确权状态',
  materialStatus: '材料状态', status: '业务状态', username: '登录账号',
  displayName: '姓名', role: '角色', villageId: '村域', enabled: '账号状态',
  permissions: '权限集合', passwordChanged: '密码已变更', sessionsInvalidated: '原会话已失效',
  deleted: '已删除', updated: '已更新', created: '已创建',
}

function parseSnapshot(value?: string): Record<string, unknown> {
  if (!value) return {}
  try {
    const parsed = JSON.parse(value)
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed)
      ? parsed as Record<string, unknown>
      : { value: parsed }
  } catch {
    return { value }
  }
}

function comparable(value: unknown) {
  return JSON.stringify(value ?? null)
}

function displayValue(value: unknown) {
  if (value === undefined || value === null || value === '') return '--'
  if (typeof value === 'boolean') return value ? '是' : '否'
  if (Array.isArray(value)) return value.length ? value.join('、') : '空集合'
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

const diffRows = computed(() => {
  if (!selected.value) return []
  const before = parseSnapshot(selected.value.beforeData)
  const after = parseSnapshot(selected.value.afterData)
  return [...new Set([...Object.keys(before), ...Object.keys(after)])]
    .map((key) => ({
      key,
      label: fieldLabels[key] || key,
      before: displayValue(before[key]),
      after: displayValue(after[key]),
      changed: comparable(before[key]) !== comparable(after[key]),
    }))
    .filter((item) => item.changed)
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchAuditLogs({
      page: page.value,
      pageSize,
      keyword: keyword.value.trim() || undefined,
      module: module.value === 'ALL' ? undefined : module.value,
      result: result.value === 'ALL' ? undefined : result.value,
      startTime: startDate.value ? `${startDate.value}T00:00:00` : undefined,
      endTime: endDate.value ? `${endDate.value}T23:59:59` : undefined,
    })
    rows.value = data.items
    total.value = data.total
    totalPages.value = data.totalPages
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '无法读取审计日志'
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  page.value = 1
  load()
}

function resetFilters() {
  keyword.value = ''
  module.value = 'ALL'
  result.value = 'ALL'
  startDate.value = ''
  endDate.value = ''
  applyFilters()
}

async function downloadCsv() {
  exporting.value = true
  exportMessage.value = ''
  try {
    const blob = await exportAuditLogs({
      keyword: keyword.value.trim() || undefined,
      module: module.value === 'ALL' ? undefined : module.value,
      result: result.value === 'ALL' ? undefined : result.value,
      startTime: startDate.value ? `${startDate.value}T00:00:00` : undefined,
      endTime: endDate.value ? `${endDate.value}T23:59:59` : undefined,
    })
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `乡耘OS_管理员审计_${new Date().toISOString().slice(0, 10)}.csv`
    anchor.click()
    URL.revokeObjectURL(url)
    exportMessage.value = '已导出当前筛选结果'
  } catch (reason) {
    exportMessage.value = reason instanceof Error ? reason.message : '导出失败，请重试'
  } finally {
    exporting.value = false
  }
}

function changePage(nextPage: number) {
  page.value = nextPage
  load()
}

function formatDate(value?: string) {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

async function copyTrace() {
  if (!selected.value?.traceId) return
  await navigator.clipboard.writeText(selected.value.traceId)
  copied.value = true
  window.setTimeout(() => { copied.value = false }, 1500)
}

onMounted(load)
</script>

<template>
  <div class="business-page audit-page">
    <section class="page-intro audit-intro">
      <div><p>管理审计 · 不可见责任变为可查证记录</p><h2>操作留痕</h2></div>
      <div class="count-summary"><strong>{{ total }}</strong><span>条审计证据</span></div>
    </section>

    <section class="audit-assurance">
      <FileClock :size="22" />
      <div><strong>关键管理操作自动记录</strong><span>资源、审批、用户与权限操作统一留痕；越权请求由网关拦截并写入安全审计。</span></div>
      <small>ADMIN ONLY</small>
    </section>

    <section class="business-toolbar audit-toolbar">
      <div class="filter-group">
        <label><Search :size="16" /><input v-model="keyword" placeholder="搜索操作人、对象或 Trace ID" @keyup.enter="applyFilters" /></label>
        <select v-model="module" @change="applyFilters"><option value="ALL">全部模块</option><option value="RESOURCE">资源管理</option><option value="WORKFLOW">审批流程</option><option value="USER">用户与权限</option><option value="SECURITY">安全边界</option><option value="REPORT">周报管理</option><option value="TODO">待办管理</option></select>
        <select v-model="result" @change="applyFilters"><option value="ALL">全部结果</option><option value="SUCCESS">操作成功</option><option value="FAILURE">操作失败</option></select>
        <label class="audit-date-field"><span>起</span><input v-model="startDate" type="date" aria-label="开始日期" @change="applyFilters" /></label>
        <label class="audit-date-field"><span>止</span><input v-model="endDate" type="date" aria-label="结束日期" @change="applyFilters" /></label>
        <button class="secondary-button query-button" type="button" @click="applyFilters">查询</button>
        <button class="text-button" type="button" @click="resetFilters">重置</button>
        <button class="secondary-button audit-export-button" type="button" :disabled="exporting" @click="downloadCsv"><Download :size="15" />{{ exporting ? '正在导出' : '导出 CSV' }}</button>
        <small v-if="exportMessage" class="audit-export-message">{{ exportMessage }}</small>
      </div>
    </section>

    <PageState :loading="loading" :error="error" :empty="!rows.length" empty-text="当前筛选下暂无审计记录" @retry="load">
      <section class="table-panel audit-table-panel">
        <div class="table-scroll"><table><thead><tr><th>时间 / 结果</th><th>操作人</th><th>行为与对象</th><th>请求</th><th>Trace ID</th><th>详情</th></tr></thead><tbody><tr v-for="item in rows" :key="item.id" :class="['audit-row', item.result.toLowerCase()]" @click="selected = item"><td><strong>{{ formatDate(item.createdAt) }}</strong><small class="audit-result" :class="item.result.toLowerCase()"><CheckCircle2 v-if="item.result === 'SUCCESS'" :size="13" /><XCircle v-else :size="13" />{{ item.result === 'SUCCESS' ? '成功' : `失败 · ${item.httpStatus}` }}</small></td><td><strong>{{ item.actorName || '系统' }}</strong><small>{{ item.actorRole || '--' }} · 村域 {{ item.villageId || '--' }}</small></td><td><strong>{{ actionLabels[item.action] || item.action }}</strong><small>{{ moduleLabels[item.module] || item.module }} · {{ item.targetType || '对象' }} {{ item.targetId || '--' }}</small></td><td><code>{{ item.requestMethod }}</code><small>{{ item.requestPath }}</small></td><td><code class="trace-code">{{ item.traceId || '--' }}</code></td><td><button class="table-action" type="button" @click.stop="selected = item"><Eye :size="14" />查看</button></td></tr></tbody></table></div>
        <PagePager :page="page" :page-size="pageSize" :total="total" :total-pages="totalPages" @change="changePage" />
      </section>
    </PageState>

    <div v-if="selected" class="drawer-layer" @click.self="selected = null"><aside class="detail-drawer audit-drawer" role="dialog" aria-modal="true" aria-label="审计详情">
      <header><div><span>审计证据 #{{ selected.id }}</span><h3>{{ actionLabels[selected.action] || selected.action }}</h3></div><div class="drawer-actions"><span class="audit-result" :class="selected.result.toLowerCase()">{{ selected.result === 'SUCCESS' ? '成功' : '失败' }}</span><button class="icon-button" type="button" title="关闭" @click="selected = null"><X :size="18" /></button></div></header>
      <div class="drawer-body audit-drawer-body">
        <section class="audit-trace-card"><ShieldAlert :size="22" /><div><span>TRACE ID</span><code>{{ selected.traceId || '--' }}</code></div><button type="button" :disabled="!selected.traceId" @click="copyTrace"><Copy :size="15" />{{ copied ? '已复制' : '复制' }}</button></section>
        <section class="detail-section audit-diff-section"><div class="section-title"><div><span>变更对照</span><h4>本次实际改变了什么</h4></div><small>{{ diffRows.length }} 个字段</small></div><div v-if="diffRows.length" class="audit-diff-ledger"><header><span>字段</span><span>操作前</span><span>操作后</span></header><div v-for="item in diffRows" :key="item.key"><strong>{{ item.label }}</strong><p>{{ item.before }}</p><p>{{ item.after }}</p></div></div><div v-else class="compact-empty">该事件没有结构化变更快照，可通过下方请求证据继续追踪</div></section>
        <section class="detail-section"><div class="section-title"><div><span>责任主体</span><h4>谁在何时执行</h4></div></div><dl><div><dt>操作人</dt><dd>{{ selected.actorName || '系统' }}</dd></div><div><dt>角色</dt><dd>{{ selected.actorRole || '--' }}</dd></div><div><dt>村域</dt><dd>{{ selected.villageId || '--' }}</dd></div><div><dt>发生时间</dt><dd>{{ formatDate(selected.createdAt) }}</dd></div></dl></section>
        <section class="detail-section"><div class="section-title"><div><span>请求证据</span><h4>访问来源与执行结果</h4></div></div><dl><div><dt>请求方法</dt><dd>{{ selected.requestMethod }}</dd></div><div><dt>HTTP 状态</dt><dd>{{ selected.httpStatus }}</dd></div><div class="wide"><dt>请求路径</dt><dd><code>{{ selected.requestPath }}</code></dd></div><div><dt>客户端 IP</dt><dd>{{ selected.clientIp || '--' }}</dd></div><div><dt>处理信息</dt><dd>{{ selected.detail || '--' }}</dd></div><div class="wide"><dt>设备标识</dt><dd class="user-agent">{{ selected.userAgent || '--' }}</dd></div></dl></section>
      </div>
    </aside></div>
  </div>
</template>
