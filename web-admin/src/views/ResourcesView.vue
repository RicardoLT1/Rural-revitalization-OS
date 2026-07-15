<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Activity, ArrowRight, ClipboardList, Eye, FolderKanban, History, LandPlot, MapPinned, MapPin, Pencil, Plus, RefreshCw, Search, Users, X } from '@lucide/vue'
import { createResource, fetchResource, fetchResourceActivity, fetchResourceApplicationCount, fetchResources, offlineResource, publishResource, updateResource } from '../api/business'
import AsyncPanel from '../components/AsyncPanel.vue'
import ImagePreview from '../components/ImagePreview.vue'
import PageState from '../components/PageState.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { useSessionStore } from '../stores/session'
import type { ResourceActivity, ResourceItem } from '../types/business'

const session = useSessionStore()
const route = useRoute()
const router = useRouter()
const rows = ref<ResourceItem[]>([])
const detail = ref<ResourceItem | null>(null)
const detailId = ref('')
const activity = ref<ResourceActivity | null>(null)
const keyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const category = ref(typeof route.query.category === 'string' ? route.query.category : 'ALL')
const investmentStatus = ref(typeof route.query.investmentStatus === 'string' ? route.query.investmentStatus : 'ALL')
const loading = ref(true)
const detailLoading = ref(false)
const detailError = ref('')
let detailRequestId = 0
const applicationCount = ref(0)
const error = ref('')
const notice = ref('')
const editing = ref<ResourceItem | null | 'new'>(null)
const saving = ref(false)
const form = reactive({ name: '', category: '闲置农房', address: '', area: 100 as number | null, annualEstimate: 10 as number | null, investmentStatus: '可招商', intro: '', owner: '', contact: '', ownershipStatus: '村集体确认', materialStatus: '基础材料齐全', investmentNote: '' })
const canMaintain = computed(() => ['STAFF', 'ADMIN'].includes(session.user?.role || ''))
const canControlLifecycle = computed(() => session.user?.role === 'ADMIN')

async function load() {
  loading.value = true
  error.value = ''
  try {
    rows.value = await fetchResources({
      keyword: keyword.value.trim() || undefined,
      category: category.value === 'ALL' ? undefined : category.value,
      investmentStatus: investmentStatus.value === 'ALL' ? undefined : investmentStatus.value,
    })
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '无法读取资源数据'
  } finally {
    loading.value = false
  }
}

async function openDetail(id: string, syncRoute = true) {
  const requestId = ++detailRequestId
  detailId.value = id
  detail.value = null
  activity.value = null
  detailError.value = ''
  detailLoading.value = true
  if (syncRoute && route.params.id !== id) await router.push({ name: 'resource-detail', params: { id } })
  const [resourceResult, countResult, activityResult] = await Promise.allSettled([fetchResource(id), fetchResourceApplicationCount(id), fetchResourceActivity(id)])
  if (requestId !== detailRequestId) return
  if (resourceResult.status === 'fulfilled') detail.value = resourceResult.value
  else detailError.value = resourceResult.reason instanceof Error ? resourceResult.reason.message : '资源详情读取失败'
  if (countResult.status === 'fulfilled') applicationCount.value = countResult.value.applicationCount
  if (activityResult.status === 'fulfilled') activity.value = activityResult.value
  detailLoading.value = false
}

async function closeDetail() {
  detail.value = null
  detailId.value = ''
  activity.value = null
  detailError.value = ''
  detailRequestId += 1
  applicationCount.value = 0
  if (route.name === 'resource-detail') await router.push({ name: 'resources' })
}

async function changeState(item: ResourceItem, action: 'publish' | 'offline') {
  const verb = action === 'publish' ? '发布' : '下架'
  if (!window.confirm(`确认${verb}“${item.name}”？`)) return
  try {
    if (action === 'publish') await publishResource(item.id)
    else await offlineResource(item.id)
    notice.value = `资源已${verb}`
    await load()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : `${verb}操作失败`
  } finally {
    window.setTimeout(() => { notice.value = '' }, 2600)
  }
}

function openEditor(item?: ResourceItem) {
  editing.value = item || 'new'
  Object.assign(form, item ? {
    name: item.name, category: item.category, address: item.address, area: item.area,
    annualEstimate: item.annualEstimate, investmentStatus: item.investmentStatus, intro: item.intro,
    owner: item.owner, contact: item.contact, ownershipStatus: item.ownershipStatus,
    materialStatus: item.materialStatus, investmentNote: item.investmentNote,
  } : { name: '', category: '闲置农房', address: '', area: 100, annualEstimate: 10, investmentStatus: '可招商', intro: '', owner: '', contact: '', ownershipStatus: '村集体确认', materialStatus: '基础材料齐全', investmentNote: '' })
}

async function saveResource() {
  saving.value = true; error.value = ''
  try {
    const payload = { ...form }
    if (editing.value === 'new') await createResource(payload)
    else if (editing.value) await updateResource(editing.value.id, payload)
    notice.value = editing.value === 'new' ? '资源已创建' : '资源档案已更新'
    editing.value = null; detail.value = null; await load()
  } catch (reason) { error.value = reason instanceof Error ? reason.message : '资源保存失败'; editing.value = null }
  finally { saving.value = false; window.setTimeout(() => { notice.value = '' }, 2600) }
}

function money(value: number | null) { return value == null ? '--' : `¥ ${Number(value).toLocaleString('zh-CN')}` }
function formatDate(value?: string) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '--' }
function activityName(action?: string) { return ({ APPLICATION_SUBMITTED: '收到合作申请', SUBMIT_APPLICATION: '提交合作申请', APPROVE_WORKFLOW: '审批流程更新', SUPPLEMENT_MATERIAL: '补充申请材料', RESOURCE_PUBLISHED: '发布招商', RESOURCE_OFFLINE: '资源下架' } as Record<string, string>)[action || ''] || action || '资源状态更新' }
watch(() => route.params.id, async (id) => {
  if (typeof id === 'string' && id !== detailId.value) await openDetail(id, false)
  else if (!id) { detail.value = null; detailId.value = '' }
})
onMounted(async () => {
  await load()
  if (typeof route.params.id === 'string') await openDetail(route.params.id, false)
})
</script>

<template>
  <div class="business-page">
    <section class="page-intro"><div><p>统一资源底账</p><h2>资源档案</h2></div><div class="resource-heading-actions"><div class="count-summary"><strong>{{ rows.length }}</strong><span>项当前结果</span></div><button v-if="canMaintain" class="primary-button compact-primary" type="button" @click="openEditor()"><Plus :size="17" />新增资源</button></div></section>
    <div v-if="notice" class="toast-notice"><LandPlot :size="16" />{{ notice }}</div>
    <section class="business-toolbar resource-toolbar"><div class="filter-group"><label><Search :size="16" /><input v-model="keyword" placeholder="搜索资源名称" @keyup.enter="load" /></label><select v-model="category"><option value="ALL">全部类型</option><option value="闲置农房">闲置农房</option><option value="土地">土地</option><option value="文旅空间">文旅空间</option></select><select v-model="investmentStatus"><option value="ALL">全部招商状态</option><option value="可招商">可招商</option><option value="洽谈中">洽谈中</option><option value="已签约">已签约</option><option value="已下架">已下架</option></select><button class="secondary-button query-button" type="button" @click="load"><RefreshCw :size="15" />查询</button></div></section>
    <PageState :loading="loading" :error="error" :empty="!rows.length" empty-text="没有符合条件的资源" @retry="load">
      <section class="table-panel"><div class="table-scroll"><table><thead><tr><th>资源</th><th>类型</th><th>面积</th><th>年收益预估</th><th>招商状态</th><th>操作</th></tr></thead><tbody><tr v-for="item in rows" :key="item.id" class="clickable-row" tabindex="0" @click="openDetail(item.id)" @keydown.enter="openDetail(item.id)"><td><strong>{{ item.name }}</strong><small><MapPin :size="12" />{{ item.address || item.owner || '地址待完善' }}</small></td><td>{{ item.category || '--' }}</td><td>{{ item.area == null ? '--' : `${item.area} ㎡` }}</td><td>{{ money(item.annualEstimate) }}</td><td><StatusBadge :status="item.investmentStatus" /></td><td><div class="row-buttons"><button class="table-action" type="button" @click.stop="openDetail(item.id)"><Eye :size="15" />详情</button><button v-if="canMaintain" class="table-action" type="button" @click.stop="openEditor(item)"><Pencil :size="14" />编辑</button><template v-if="canControlLifecycle"><button class="table-action approve" type="button" @click.stop="changeState(item, 'publish')">发布</button><button class="table-action reject" type="button" @click.stop="changeState(item, 'offline')">下架</button></template></div></td></tr></tbody></table></div></section>
    </PageState>
    <div v-if="detailId" class="drawer-layer" @click.self="closeDetail"><aside class="detail-drawer resource-detail-drawer"><header><div><span>资源编号 {{ detail?.id || detailId }}</span><h3>{{ detail?.name || '资源详情' }}</h3></div><div class="drawer-actions"><StatusBadge v-if="detail" :status="detail.investmentStatus" /><button v-if="canMaintain && detail" class="secondary-button" type="button" @click="openEditor(detail)"><Pencil :size="15" />编辑</button><button class="icon-button" type="button" title="关闭" @click="closeDetail"><X :size="18" /></button></div></header><div class="drawer-body">
      <AsyncPanel :loading="detailLoading" :error="detailError" :empty="!detail" empty-text="资源档案不存在" :skeleton-rows="7" @retry="openDetail(detailId, false)">
      <template v-if="detail">
      <section class="resource-hero"><div><span>{{ detail.category || '乡村资源' }}</span><p>{{ detail.intro || '暂无资源介绍' }}</p></div><div class="resource-score"><strong>{{ detail.expectedROI == null ? '--' : `${detail.expectedROI}%` }}</strong><span>预期回报率</span></div></section>
      <section class="resource-facts"><article><LandPlot :size="18" /><span>面积</span><strong>{{ detail.area == null ? '--' : `${detail.area} ㎡` }}</strong></article><article><Users :size="18" /><span>合作申请</span><strong>{{ applicationCount }} 条</strong></article><article><FolderKanban :size="18" /><span>材料状态</span><strong>{{ detail.materialStatus || '--' }}</strong></article></section>
      <section class="detail-section"><div class="section-title"><div><span>基础档案</span><h4>权属与招商信息</h4></div></div><dl><div><dt>资源类型</dt><dd>{{ detail.category || '--' }}</dd></div><div><dt>确权状态</dt><dd>{{ detail.ownershipStatus || '--' }}</dd></div><div><dt>年收益预估</dt><dd>{{ money(detail.annualEstimate) }} 万元</dd></div><div><dt>权属单位</dt><dd>{{ detail.owner || '--' }}</dd></div><div><dt>联系方式</dt><dd>{{ detail.contact || '--' }}</dd></div><div><dt>招商说明</dt><dd>{{ detail.investmentNote || '--' }}</dd></div></dl></section>
      <section class="detail-section location-card"><div class="section-title"><div><span>空间位置</span><h4>{{ detail.address || '地址待完善' }}</h4></div><MapPinned :size="20" /></div><p v-if="detail.lat != null && detail.lng != null">坐标 {{ detail.lat }}, {{ detail.lng }}</p><p v-else>暂未录入经纬度，后续可接入资源地图。</p></section>
      <section class="detail-section"><div class="section-title"><div><span>现场材料</span><h4>现场照片</h4></div><small>{{ detail.fieldPhotos?.length || 0 }} 张</small></div><ImagePreview :images="detail.fieldPhotos" :title="detail.name" /></section>
      <section class="detail-section"><div class="section-title"><div><span>协同关系</span><h4>关联项目</h4></div></div><div v-if="detail.relatedProjects?.length" class="related-projects"><span v-for="project in detail.relatedProjects" :key="project">{{ project }}<ArrowRight :size="13" /></span></div><div v-else class="compact-empty">暂无关联项目</div></section>
      <section class="detail-section"><div class="section-title"><div><span>合作流程</span><h4>关联审批</h4></div><small>{{ activity?.workflows.length || 0 }} 条</small></div><div v-if="activity?.workflows.length" class="resource-workflow-list"><button v-for="workflow in activity.workflows" :key="workflow.processId" type="button" @click="router.push({ path: '/approvals', query: { workflow: String(workflow.processId) } })"><ClipboardList :size="17" /><div><strong>{{ workflow.title }}</strong><span>流程 {{ workflow.processId }} · {{ workflow.applicantName || '申请人待补充' }}</span><small>{{ formatDate(workflow.updatedAt || workflow.createdAt) }}</small></div><StatusBadge :status="workflow.status" /><ArrowRight :size="15" /></button></div><div v-else class="compact-empty">该资源暂无关联审批</div></section>
      <section class="detail-section"><div class="section-title"><div><span>状态轨迹</span><h4>资源运营时间线</h4></div><History :size="18" /></div><div v-if="activity?.timeline.length" class="resource-activity-timeline"><article v-for="item in activity.timeline" :key="item.id"><i><Activity :size="12" /></i><div><strong>{{ activityName(item.action) }}</strong><p>{{ item.remark || item.title || '状态已更新' }}</p><span>{{ item.operatorName || '系统' }} · {{ formatDate(item.createdAt) }}</span></div><StatusBadge v-if="item.status" :status="item.status" /></article></div><div v-else class="compact-empty">暂无可追溯的资源活动</div></section>
      <div class="tag-list"><span v-for="tag in detail.tags" :key="tag">{{ tag }}</span></div>
      </template>
      </AsyncPanel>
    </div></aside></div>
    <div v-if="editing" class="dialog-layer" @click.self="editing = null"><form class="entity-dialog resource-dialog" @submit.prevent="saveResource"><header><div><span>{{ editing === 'new' ? '创建档案' : '更新档案' }}</span><h3>{{ editing === 'new' ? '新增乡村资源' : form.name }}</h3></div><button class="icon-button" type="button" title="关闭" @click="editing = null"><X :size="18" /></button></header><div class="entity-form"><div class="form-grid two"><label><span>资源名称</span><input v-model="form.name" required /></label><label><span>资源类型</span><select v-model="form.category"><option>闲置农房</option><option>土地</option><option>文旅空间</option><option>研学基地</option></select></label></div><label><span>资源地址</span><input v-model="form.address" required /></label><div class="form-grid three"><label><span>面积（㎡）</span><input v-model.number="form.area" type="number" min="0" step="0.01" /></label><label><span>年收益预估（万元）</span><input v-model.number="form.annualEstimate" type="number" min="0" step="0.01" /></label><label><span>招商状态</span><select v-model="form.investmentStatus"><option>可招商</option><option>洽谈中</option><option>已签约</option><option>已下架</option></select></label></div><label><span>资源介绍</span><textarea v-model="form.intro" rows="3" /></label><div class="form-grid two"><label><span>权属单位</span><input v-model="form.owner" /></label><label><span>联系方式</span><input v-model="form.contact" /></label></div><div class="form-grid two"><label><span>确权状态</span><input v-model="form.ownershipStatus" /></label><label><span>材料状态</span><input v-model="form.materialStatus" /></label></div><label><span>招商说明</span><textarea v-model="form.investmentNote" rows="2" /></label></div><footer><button class="secondary-button" type="button" @click="editing = null">取消</button><button class="primary-button dialog-submit" type="submit" :disabled="saving">{{ saving ? '正在保存...' : '保存资源' }}</button></footer></form></div>
  </div>
</template>
