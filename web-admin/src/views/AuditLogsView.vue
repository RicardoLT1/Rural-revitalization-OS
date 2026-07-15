<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { CheckCircle2, Copy, Eye, FileClock, Search, ShieldAlert, X, XCircle } from '@lucide/vue'
import { fetchAuditLogs } from '../api/admin'
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
const loading = ref(true)
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
}

const moduleLabels: Record<string, string> = {
  RESOURCE: '资源管理',
  WORKFLOW: '审批流程',
  REPORT: '周报管理',
  TODO: '待办管理',
}

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
  applyFilters()
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
      <div><strong>关键管理操作自动记录</strong><span>当前覆盖资源、审批、待办与周报写操作；越权请求由网关直接拦截。</span></div>
      <small>ADMIN ONLY</small>
    </section>

    <section class="business-toolbar audit-toolbar">
      <div class="filter-group">
        <label><Search :size="16" /><input v-model="keyword" placeholder="搜索操作人、对象或 Trace ID" @keyup.enter="applyFilters" /></label>
        <select v-model="module" @change="applyFilters"><option value="ALL">全部模块</option><option value="RESOURCE">资源管理</option><option value="WORKFLOW">审批流程</option><option value="REPORT">周报管理</option><option value="TODO">待办管理</option></select>
        <select v-model="result" @change="applyFilters"><option value="ALL">全部结果</option><option value="SUCCESS">操作成功</option><option value="FAILURE">操作失败</option></select>
        <button class="secondary-button query-button" type="button" @click="applyFilters">查询</button>
        <button class="text-button" type="button" @click="resetFilters">重置</button>
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
        <section class="detail-section"><div class="section-title"><div><span>责任主体</span><h4>谁在何时执行</h4></div></div><dl><div><dt>操作人</dt><dd>{{ selected.actorName || '系统' }}</dd></div><div><dt>角色</dt><dd>{{ selected.actorRole || '--' }}</dd></div><div><dt>村域</dt><dd>{{ selected.villageId || '--' }}</dd></div><div><dt>发生时间</dt><dd>{{ formatDate(selected.createdAt) }}</dd></div></dl></section>
        <section class="detail-section"><div class="section-title"><div><span>请求证据</span><h4>访问来源与执行结果</h4></div></div><dl><div><dt>请求方法</dt><dd>{{ selected.requestMethod }}</dd></div><div><dt>HTTP 状态</dt><dd>{{ selected.httpStatus }}</dd></div><div class="wide"><dt>请求路径</dt><dd><code>{{ selected.requestPath }}</code></dd></div><div><dt>客户端 IP</dt><dd>{{ selected.clientIp || '--' }}</dd></div><div><dt>处理信息</dt><dd>{{ selected.detail || '--' }}</dd></div><div class="wide"><dt>设备标识</dt><dd class="user-agent">{{ selected.userAgent || '--' }}</dd></div></dl></section>
      </div>
    </aside></div>
  </div>
</template>
