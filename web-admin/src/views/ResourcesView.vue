<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Eye, LandPlot, MapPin, RefreshCw, Search, X } from '@lucide/vue'
import { fetchResource, fetchResources, offlineResource, publishResource } from '../api/business'
import PageState from '../components/PageState.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { useSessionStore } from '../stores/session'
import type { ResourceItem } from '../types/business'

const session = useSessionStore()
const rows = ref<ResourceItem[]>([])
const detail = ref<ResourceItem | null>(null)
const keyword = ref('')
const category = ref('ALL')
const investmentStatus = ref('ALL')
const loading = ref(true)
const detailLoading = ref(false)
const error = ref('')
const notice = ref('')
const canManage = computed(() => session.user?.role === 'ADMIN')

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

async function openDetail(id: string) {
  detailLoading.value = true
  try { detail.value = await fetchResource(id) }
  catch (reason) { error.value = reason instanceof Error ? reason.message : '资源详情读取失败' }
  finally { detailLoading.value = false }
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

function money(value: number | null) { return value == null ? '--' : `¥ ${Number(value).toLocaleString('zh-CN')}` }
onMounted(load)
</script>

<template>
  <div class="business-page">
    <section class="page-intro"><div><p>统一资源底账</p><h2>资源档案</h2></div><div class="count-summary"><strong>{{ rows.length }}</strong><span>项当前结果</span></div></section>
    <div v-if="notice" class="toast-notice"><LandPlot :size="16" />{{ notice }}</div>
    <section class="business-toolbar resource-toolbar"><div class="filter-group"><label><Search :size="16" /><input v-model="keyword" placeholder="搜索资源名称" @keyup.enter="load" /></label><select v-model="category"><option value="ALL">全部类型</option><option value="闲置农房">闲置农房</option><option value="土地">土地</option><option value="文旅空间">文旅空间</option></select><select v-model="investmentStatus"><option value="ALL">全部招商状态</option><option value="可招商">可招商</option><option value="洽谈中">洽谈中</option><option value="已签约">已签约</option><option value="已下架">已下架</option></select><button class="secondary-button query-button" type="button" @click="load"><RefreshCw :size="15" />查询</button></div></section>
    <PageState :loading="loading" :error="error" :empty="!rows.length" empty-text="没有符合条件的资源" @retry="load">
      <section class="table-panel"><div class="table-scroll"><table><thead><tr><th>资源</th><th>类型</th><th>面积</th><th>年收益预估</th><th>招商状态</th><th>操作</th></tr></thead><tbody><tr v-for="item in rows" :key="item.id"><td><strong>{{ item.name }}</strong><small><MapPin :size="12" />{{ item.address || item.owner || '地址待完善' }}</small></td><td>{{ item.category || '--' }}</td><td>{{ item.area == null ? '--' : `${item.area} ㎡` }}</td><td>{{ money(item.annualEstimate) }}</td><td><StatusBadge :status="item.investmentStatus" /></td><td><div class="row-buttons"><button class="table-action" type="button" @click="openDetail(item.id)"><Eye :size="15" />详情</button><template v-if="canManage"><button class="table-action approve" type="button" @click="changeState(item, 'publish')">发布</button><button class="table-action reject" type="button" @click="changeState(item, 'offline')">下架</button></template></div></td></tr></tbody></table></div></section>
    </PageState>
    <div v-if="detailLoading" class="drawer-loading">正在读取资源详情...</div>
    <div v-if="detail" class="drawer-layer" @click.self="detail = null"><aside class="detail-drawer"><header><div><span>资源编号 {{ detail.id }}</span><h3>{{ detail.name }}</h3></div><button class="icon-button" type="button" title="关闭" @click="detail = null"><X :size="18" /></button></header><div class="drawer-body"><StatusBadge :status="detail.investmentStatus" /><p class="resource-intro">{{ detail.intro || '暂无资源介绍' }}</p><dl><div><dt>资源类型</dt><dd>{{ detail.category || '--' }}</dd></div><div><dt>确权状态</dt><dd>{{ detail.ownershipStatus || '--' }}</dd></div><div><dt>面积</dt><dd>{{ detail.area == null ? '--' : `${detail.area} ㎡` }}</dd></div><div><dt>预期回报率</dt><dd>{{ detail.expectedROI == null ? '--' : `${detail.expectedROI}%` }}</dd></div><div><dt>权属单位</dt><dd>{{ detail.owner || '--' }}</dd></div><div><dt>联系方式</dt><dd>{{ detail.contact || '--' }}</dd></div><div><dt>材料状态</dt><dd>{{ detail.materialStatus || '--' }}</dd></div><div><dt>招商说明</dt><dd>{{ detail.investmentNote || '--' }}</dd></div></dl><div class="tag-list"><span v-for="tag in detail.tags" :key="tag">{{ tag }}</span></div></div></aside></div>
  </div>
</template>
