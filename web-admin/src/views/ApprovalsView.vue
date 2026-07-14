<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Check, ClipboardCheck, Clock3, FileText, History, MapPin, MessageSquareText, PackageOpen, Search, UserRound, X } from '@lucide/vue'
import { decideWorkflow, fetchApprovalHistory, fetchResource, fetchTodos, fetchWorkflow, fetchWorkflowOperationLogs, requestWorkflowMaterials } from '../api/business'
import AsyncPanel from '../components/AsyncPanel.vue'
import PageState from '../components/PageState.vue'
import StatusBadge from '../components/StatusBadge.vue'
import type { ResourceItem, WorkflowDetail, WorkflowItem, WorkflowOperationLog } from '../types/business'

const route = useRoute()
const router = useRouter()
const tab = ref<'todos' | 'history'>('todos')
const todos = ref<WorkflowItem[]>([])
const history = ref<WorkflowItem[]>([])
const keyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const status = ref(typeof route.query.status === 'string' ? route.query.status : 'ALL')
const loading = ref(true)
const error = ref('')
const selected = ref<WorkflowItem | null>(null)
const workflow = ref<WorkflowDetail | null>(null)
const logs = ref<WorkflowOperationLog[]>([])
const relatedResource = ref<ResourceItem | null>(null)
const detailLoading = ref(false)
const detailError = ref('')
let detailRequestId = 0
const decision = ref<'approve' | 'reject' | 'materials' | null>(null)
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
const activeStatus = computed(() => workflow.value?.status || selected.value?.status || '')
const canDecide = computed(() => ['PENDING', '待审批'].includes(activeStatus.value))

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

async function openDetail(item: WorkflowItem, syncRoute = true) {
  const requestId = ++detailRequestId
  const id = item.processId || item.id
  selected.value = item
  workflow.value = null
  logs.value = []
  relatedResource.value = null
  detailError.value = ''
  decision.value = null
  remark.value = ''
  detailLoading.value = true
  if (syncRoute && String(route.query.workflow || '') !== String(id)) {
    await router.replace({ query: { ...route.query, workflow: String(id) } })
  }
  const [workflowResult, logsResult] = await Promise.allSettled([fetchWorkflow(id), fetchWorkflowOperationLogs(id)])
  if (requestId !== detailRequestId) return
  if (workflowResult.status === 'fulfilled') workflow.value = workflowResult.value
  else detailError.value = workflowResult.reason instanceof Error ? workflowResult.reason.message : '审批详情读取失败'
  if (logsResult.status === 'fulfilled') logs.value = logsResult.value
  const resourceId = logs.value.find((entry) => entry.resourceId)?.resourceId
  if (resourceId != null) {
    const resourceResult = await Promise.allSettled([fetchResource(String(resourceId))])
    if (requestId !== detailRequestId) return
    if (resourceResult[0].status === 'fulfilled') relatedResource.value = resourceResult[0].value
  }
  detailLoading.value = false
}

async function closeDetail() {
  selected.value = null
  detailRequestId += 1
  workflow.value = null
  decision.value = null
  const query = { ...route.query }
  delete query.workflow
  await router.replace({ query })
}

async function openAction(item: WorkflowItem, action: 'approve' | 'reject' | 'materials') {
  await openDetail(item)
  decision.value = action
}

async function submitDecision() {
  const id = workflow.value?.id || selected.value?.processId || selected.value?.id
  if (!id || !decision.value) return
  saving.value = true
  try {
    if (decision.value === 'materials') await requestWorkflowMaterials(id, remark.value.trim())
    else await decideWorkflow(id, decision.value, remark.value.trim())
    notice.value = decision.value === 'approve' ? '申请已通过' : decision.value === 'reject' ? '申请已驳回' : '已要求申请人补充材料'
    const current = selected.value
    decision.value = null
    remark.value = ''
    await load()
    if (current) await openDetail({ ...current, processId: id }, false)
    window.dispatchEvent(new CustomEvent('xiangyun:workflow-updated'))
  } catch (reason) {
    detailError.value = reason instanceof Error ? reason.message : '审批操作未完成'
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

function actionName(value: string) {
  return ({ APPROVE: '通过', APPROVED: '通过', REJECT: '驳回', REJECTED: '驳回', SUBMIT_APPLICATION: '提交申请', APPROVE_WORKFLOW: '处理审批', MATERIAL_REQUIRED: '要求补材料', SUPPLEMENT_MATERIAL: '补充材料' } as Record<string, string>)[value?.toUpperCase()] || value || '更新流程'
}

function retryDetail() {
  if (selected.value) openDetail(selected.value, false)
}

watch(() => route.query.workflow, async (id) => {
  if (!id || selected.value || loading.value) return
  const item = [...todos.value, ...history.value].find((row) => String(row.processId || row.id) === String(id))
  await openDetail(item || { id: String(id), processId: String(id), title: '审批详情', status: '' }, false)
})

onMounted(async () => {
  await load()
  const id = route.query.workflow
  if (id && !selected.value) {
    const item = [...todos.value, ...history.value].find((row) => String(row.processId || row.id) === String(id))
    await openDetail(item || { id: String(id), processId: String(id), title: '审批详情', status: '' }, false)
  }
})
</script>

<template>
  <div class="business-page">
    <section class="page-intro"><div><p>流程与责任</p><h2>审批工作台</h2></div><div class="count-summary"><strong>{{ todos.filter(item => item.status === 'PENDING').length }}</strong><span>项待处理</span></div></section>
    <div v-if="notice" class="toast-notice"><Check :size="16" />{{ notice }}</div>
    <section class="business-toolbar">
      <div class="view-tabs"><button type="button" :class="{ active: tab === 'todos' }" @click="tab = 'todos'"><ClipboardCheck :size="16" />待办</button><button type="button" :class="{ active: tab === 'history' }" @click="tab = 'history'"><History :size="16" />审批记录</button></div>
      <div class="filter-group"><label><Search :size="16" /><input v-model="keyword" placeholder="搜索标题或流程编号" /></label><select v-model="status"><option value="ALL">全部状态</option><option value="PENDING">待审批</option><option value="MATERIAL_REQUIRED">待补材料</option><option value="APPROVED">已通过</option><option value="REJECTED">已驳回</option></select></div>
    </section>
    <PageState :loading="loading" :error="error" :empty="!rows.length" empty-text="没有符合条件的审批事项" @retry="load">
      <section class="table-panel">
        <div class="table-scroll"><table><thead><tr><th>事项</th><th>类型 / 申请人</th><th>状态</th><th>时间</th><th>操作</th></tr></thead><tbody><tr v-for="item in rows" :key="`${tab}-${item.id}`" class="clickable-row" tabindex="0" @click="openDetail(item)" @keydown.enter="openDetail(item)"><td><strong>{{ item.title || '合作申请' }}</strong><small>流程 {{ item.processId || item.id }}</small></td><td>{{ item.category || item.applicant || '--' }}</td><td><StatusBadge :status="item.status" /></td><td>{{ formatDate(item.dueDate || item.time) }}</td><td><div v-if="tab === 'todos' && item.status === 'PENDING'" class="row-buttons"><button class="table-action approve" type="button" @click.stop="openAction(item, 'approve')"><Check :size="15" />通过</button><button class="table-action reject" type="button" @click.stop="openAction(item, 'reject')"><X :size="15" />驳回</button></div><button v-else class="table-action" type="button" @click.stop="openDetail(item)">查看详情</button></td></tr></tbody></table></div>
      </section>
    </PageState>

    <div v-if="selected" class="drawer-layer" @click.self="closeDetail"><aside class="detail-drawer workflow-drawer" role="dialog" aria-modal="true" aria-label="审批详情">
      <header><div><span>流程 {{ workflow?.id || selected.processId || selected.id }}</span><h3>{{ workflow?.title || selected.title }}</h3></div><div class="drawer-actions"><StatusBadge :status="activeStatus" /><button class="icon-button" type="button" title="关闭" @click="closeDetail"><X :size="18" /></button></div></header>
      <div class="drawer-body workflow-drawer-body">
        <AsyncPanel :loading="detailLoading" :error="detailError" :empty="!workflow" empty-text="审批详情不存在" :skeleton-rows="7" @retry="retryDetail">
        <template v-if="workflow">
          <section class="detail-section context-strip"><div><UserRound :size="17" /><span>申请人</span><strong>{{ workflow.applicantName || selected.applicant || '--' }}</strong></div><div><Clock3 :size="17" /><span>当前节点</span><strong>{{ workflow.currentNodeId || '--' }}</strong></div><div><FileText :size="17" /><span>材料状态</span><strong>{{ relatedResource?.materialStatus || '待流程核验' }}</strong></div></section>

          <section v-if="relatedResource" class="detail-section linked-resource"><div class="section-title"><div><span>关联资源</span><h4>{{ relatedResource.name }}</h4></div><RouterLink :to="`/resources/${relatedResource.id}`">查看资源档案</RouterLink></div><p><MapPin :size="14" />{{ relatedResource.address || '地址待完善' }}</p><dl><div><dt>类型</dt><dd>{{ relatedResource.category }}</dd></div><div><dt>面积</dt><dd>{{ relatedResource.area == null ? '--' : `${relatedResource.area} ㎡` }}</dd></div><div><dt>招商状态</dt><dd>{{ relatedResource.investmentStatus }}</dd></div><div><dt>权属单位</dt><dd>{{ relatedResource.owner || '--' }}</dd></div></dl></section>

          <section class="detail-section"><div class="section-title"><div><span>流程节点</span><h4>办理进度</h4></div><small>{{ workflow.nodes.length }} 个节点</small></div><ol class="workflow-trail"><li v-for="node in workflow.nodes" :key="node.id" :class="node.status?.toLowerCase()"><i><Check v-if="['APPROVED','COMPLETED','已完成'].includes(node.status)" :size="12" /><span v-else /></i><div><strong>{{ node.name }}</strong><span>{{ node.owner || '待分配' }}</span><p v-if="node.remark">{{ node.remark }}</p></div><StatusBadge :status="node.status" /></li></ol></section>

          <section class="detail-section"><div class="section-title"><div><span>审批记录</span><h4>历史处理意见</h4></div><small>{{ workflow.records.length }} 条</small></div><div v-if="workflow.records.length" class="record-list"><article v-for="record in workflow.records" :key="record.id"><i><MessageSquareText :size="14" /></i><div><strong>{{ actionName(record.action) }} · {{ record.operator || '系统' }}</strong><p>{{ record.remark || '未填写处理意见' }}</p><span>{{ formatDate(record.time) }}</span></div></article></div><div v-else class="compact-empty">尚无审批记录</div></section>

          <section class="detail-section"><div class="section-title"><div><span>操作日志</span><h4>全流程留痕</h4></div><small>{{ logs.length }} 条</small></div><div v-if="logs.length" class="operation-timeline"><article v-for="log in logs" :key="log.id"><i /><div><strong>{{ actionName(log.action) }}</strong><p>{{ log.remark || '流程状态已更新' }}</p><span>{{ log.operatorName || log.operatorId || '系统' }} · {{ formatDate(log.createdAt) }}</span></div></article></div><div v-else class="compact-empty">暂无操作日志</div></section>
        </template>
        </AsyncPanel>
      </div>
      <footer v-if="workflow" class="workflow-actions">
        <template v-if="canDecide">
          <div v-if="decision" class="decision-compose"><div><strong>{{ decision === 'approve' ? '确认通过申请' : decision === 'reject' ? '填写驳回原因' : '说明需要补充的材料' }}</strong><button type="button" @click="decision = null"><X :size="14" /></button></div><textarea v-model="remark" rows="3" :placeholder="decision === 'approve' ? '填写通过意见（可选）' : '请填写明确的处理要求'" /><button class="primary-button" type="button" :disabled="saving || (decision !== 'approve' && !remark.trim())" @click="submitDecision">{{ saving ? '正在处理...' : '确认提交' }}</button></div>
          <div v-else class="workflow-action-buttons"><button class="secondary-button material-action" type="button" @click="decision = 'materials'"><PackageOpen :size="16" />要求补材料</button><button class="secondary-button reject-action" type="button" @click="decision = 'reject'"><X :size="16" />驳回</button><button class="primary-button compact-primary" type="button" @click="decision = 'approve'"><Check :size="16" />通过申请</button></div>
        </template>
        <div v-else class="processed-note"><Check :size="16" />该流程已处理，详情与操作记录仍可追溯</div>
      </footer>
    </aside></div>
  </div>
</template>
