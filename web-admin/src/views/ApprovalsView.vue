<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Check, ClipboardCheck, History, Search, X } from '@lucide/vue'
import { decideWorkflow, fetchApprovalHistory, fetchTodos } from '../api/business'
import PageState from '../components/PageState.vue'
import StatusBadge from '../components/StatusBadge.vue'
import type { WorkflowItem } from '../types/business'

const tab = ref<'todos' | 'history'>('todos')
const todos = ref<WorkflowItem[]>([])
const history = ref<WorkflowItem[]>([])
const keyword = ref('')
const status = ref('ALL')
const loading = ref(true)
const error = ref('')
const selected = ref<WorkflowItem | null>(null)
const decision = ref<'approve' | 'reject'>('approve')
const remark = ref('')
const saving = ref(false)
const notice = ref('')

const rows = computed(() => {
  const source = tab.value === 'todos' ? todos.value : history.value
  const term = keyword.value.trim().toLowerCase()
  return source.filter((item) => {
    const matchesKeyword = !term || `${item.title || ''} ${item.processId || ''} ${item.applicant || ''}`.toLowerCase().includes(term)
    return matchesKeyword && (status.value === 'ALL' || item.status === status.value)
  })
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [todoRows, historyRows] = await Promise.all([fetchTodos(), fetchApprovalHistory()])
    todos.value = todoRows
    history.value = historyRows
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '无法读取审批数据'
  } finally {
    loading.value = false
  }
}

function openDecision(item: WorkflowItem, action: 'approve' | 'reject') {
  selected.value = item
  decision.value = action
  remark.value = ''
}

async function submitDecision() {
  if (!selected.value) return
  saving.value = true
  try {
    await decideWorkflow(selected.value.processId || selected.value.id, decision.value, remark.value.trim())
    notice.value = decision.value === 'approve' ? '申请已通过' : '申请已驳回'
    selected.value = null
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '审批操作未完成'
    selected.value = null
  } finally {
    saving.value = false
    window.setTimeout(() => { notice.value = '' }, 2600)
  }
}

function formatDate(value?: string) {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>

<template>
  <div class="business-page">
    <section class="page-intro"><div><p>流程与责任</p><h2>审批工作台</h2></div><div class="count-summary"><strong>{{ todos.filter(item => item.status === 'PENDING').length }}</strong><span>项待处理</span></div></section>
    <div v-if="notice" class="toast-notice"><Check :size="16" />{{ notice }}</div>
    <section class="business-toolbar">
      <div class="view-tabs"><button type="button" :class="{ active: tab === 'todos' }" @click="tab = 'todos'"><ClipboardCheck :size="16" />待办</button><button type="button" :class="{ active: tab === 'history' }" @click="tab = 'history'"><History :size="16" />审批记录</button></div>
      <div class="filter-group"><label><Search :size="16" /><input v-model="keyword" placeholder="搜索标题或流程编号" /></label><select v-model="status"><option value="ALL">全部状态</option><option value="PENDING">待审批</option><option value="APPROVED">已通过</option><option value="REJECTED">已驳回</option></select></div>
    </section>
    <PageState :loading="loading" :error="error" :empty="!rows.length" empty-text="没有符合条件的审批事项" @retry="load">
      <section class="table-panel">
        <div class="table-scroll"><table><thead><tr><th>事项</th><th>类型 / 申请人</th><th>状态</th><th>时间</th><th>操作</th></tr></thead><tbody><tr v-for="item in rows" :key="`${tab}-${item.id}`"><td><strong>{{ item.title || '合作申请' }}</strong><small>流程 {{ item.processId || item.id }}</small></td><td>{{ item.category || item.applicant || '--' }}</td><td><StatusBadge :status="item.status" /></td><td>{{ formatDate(item.dueDate || item.time) }}</td><td><div v-if="tab === 'todos' && item.status === 'PENDING'" class="row-buttons"><button class="table-action approve" type="button" @click="openDecision(item, 'approve')"><Check :size="15" />通过</button><button class="table-action reject" type="button" @click="openDecision(item, 'reject')"><X :size="15" />驳回</button></div><span v-else class="muted-text">已处理</span></td></tr></tbody></table></div>
      </section>
    </PageState>

    <div v-if="selected" class="dialog-layer" @click.self="selected = null"><section class="decision-dialog" role="dialog" aria-modal="true"><header><div><span>{{ decision === 'approve' ? '通过申请' : '驳回申请' }}</span><h3>{{ selected.title }}</h3></div><button class="icon-button" type="button" title="关闭" @click="selected = null"><X :size="18" /></button></header><p>流程编号：{{ selected.processId || selected.id }}</p><label><span>处理意见</span><textarea v-model="remark" rows="4" :placeholder="decision === 'approve' ? '填写通过意见（可选）' : '填写驳回原因'" /></label><footer><button class="secondary-button" type="button" @click="selected = null">取消</button><button class="primary-button dialog-submit" type="button" :disabled="saving || (decision === 'reject' && !remark.trim())" @click="submitDecision">{{ saving ? '正在处理...' : '确认处理' }}</button></footer></section></div>
  </div>
</template>
