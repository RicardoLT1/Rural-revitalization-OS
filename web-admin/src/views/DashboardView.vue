<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { AlertTriangle, ArrowRight, CalendarDays, ClipboardCheck, Download, FileBarChart, Layers3, Lightbulb, MapPinned, RefreshCw, ShieldAlert, Sparkles, TrendingUp, Users } from '@lucide/vue'
import { fetchDashboard } from '../api/business'
import ComingSoonButton from '../components/ComingSoonButton.vue'
import PageState from '../components/PageState.vue'
import type { DashboardData } from '../types/business'

const data = ref<DashboardData | null>(null)
const router = useRouter()
const days = ref(7)
const loading = ref(true)
const error = ref('')
const notice = ref('')
const displayedValues = ref<Array<number | string>>([])
const chartAnimationKey = ref(0)

const trend = computed(() => days.value === 30 ? data.value?.trends?.days30 || [] : data.value?.trends?.days7 || [])
const maxValue = computed(() => Math.max(...trend.value.map((item) => Number(item.value)), 1))
const generatedTime = computed(() => data.value?.generatedAt ? new Date(data.value.generatedAt).toLocaleString('zh-CN', { hour12: false }) : '--')
const metricIcons = [Layers3, Users, ClipboardCheck, ShieldAlert]
const resourceDistribution = [
  { label: '土地', value: 41.2, color: '#15935f', offset: 0 },
  { label: '闲置农房', value: 23.5, color: '#4b95e6', offset: 41.2 },
  { label: '文旅空间', value: 17.6, color: '#f0ac35', offset: 64.7 },
  { label: '研学基地', value: 11.8, color: '#9075cb', offset: 82.3 },
  { label: '其他资源', value: 5.9, color: '#dce5e0', offset: 94.1 },
]
const chartPoints = computed(() => trend.value.map((point, index) => ({
  ...point,
  x: trend.value.length <= 1 ? 50 : 5 + index * 90 / (trend.value.length - 1),
  y: 88 - Number(point.value) / maxValue.value * 68,
})))
const chartLine = computed(() => chartPoints.value.map((point) => `${point.x},${point.y}`).join(' '))

function animateValues() {
  const targets = data.value?.stats.map((stat) => Number(stat.value)) || []
  displayedValues.value = targets.map(() => 0)
  const startedAt = performance.now()
  const duration = 900
  const tick = (now: number) => {
    const progress = Math.min(1, (now - startedAt) / duration)
    const eased = 1 - Math.pow(1 - progress, 3)
    displayedValues.value = targets.map((target, index) => Number.isFinite(target) ? Math.round(target * eased) : data.value?.stats[index]?.value || '--')
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await fetchDashboard(days.value)
    chartAnimationKey.value += 1
    animateValues()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '无法读取看板数据'
  } finally {
    loading.value = false
  }
}

async function changeRange(value: number) {
  days.value = value
  await load()
}

function drillMetric(key: string) {
  const destinations: Record<string, { path: string; query?: Record<string, string> }> = {
    resource: { path: '/resources' },
    ready: { path: '/resources', query: { investmentStatus: '可招商' } },
    todo: { path: '/approvals', query: { status: 'PENDING' } },
    risk: { path: '/approvals', query: { status: 'PENDING' } },
  }
  const target = destinations[key]
  if (target) router.push(target)
}

function drillCategory(category: string) {
  router.push(category === '其他资源' ? '/resources' : { path: '/resources', query: { category } })
}

function handleSuggestion(actionType?: string) {
  const action = actionType?.toLowerCase()
  if (action === 'match' || action?.includes('resource')) router.push({ path: '/resources', query: { investmentStatus: '可招商' } })
  else if (action?.includes('approval') || action?.includes('risk')) router.push({ path: '/approvals', query: { status: 'PENDING' } })
  else if (action?.includes('report')) router.push('/weekly-report')
  else {
    notice.value = '该建议暂未开放自动执行，已保留为运营参考'
    window.setTimeout(() => { notice.value = '' }, 2800)
  }
}

function downloadReport() {
  if (!data.value) return
  const rows = [['日期', '访问量'], ...trend.value.map((item) => [item.date, String(item.value)])]
  const csv = `\uFEFF${rows.map((row) => row.map((cell) => `"${cell.replaceAll('"', '""')}"`).join(',')).join('\n')}`
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a')
  const now = new Date()
  const dateStamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
  link.href = url
  link.download = `乡耘OS_资源访问趋势_${dateStamp}.csv`
  link.click()
  URL.revokeObjectURL(url)
  notice.value = '趋势报表已生成'
  window.setTimeout(() => { notice.value = '' }, 2200)
}

const todoCount = computed(() => Number(data.value?.stats.find((item) => item.key === 'todo')?.value || 0))

onMounted(load)
</script>

<template>
  <div class="dashboard-page">
    <div v-if="notice" class="toast-notice"><Sparkles :size="16" />{{ notice }}</div>
    <section class="page-intro dashboard-heading">
      <div><p>乡村资源运营协同平台</p><h2>您好，欢迎回来！</h2><span>实时掌握资源动态，驱动乡村资源高效协同与运营。</span></div>
      <div class="page-actions"><span class="freshness"><i :class="{ stale: data?.stale }" />数据状态：<b>{{ data?.stale ? '降级' : '正常' }}</b></span><button class="icon-button" type="button" title="刷新看板" @click="load"><RefreshCw :size="17" /></button><ComingSoonButton class="secondary-button custom-dashboard" label="自定义看板" /></div>
    </section>

    <PageState :loading="loading" :error="error" @retry="load">
      <template v-if="data">
        <section class="metric-grid live-metrics">
          <button v-for="(stat, index) in data.stats" :key="stat.key" class="metric-card" type="button" :aria-label="`查看${stat.title}`" @click="drillMetric(stat.key)">
            <div class="metric-card-head"><span class="metric-icon"><component :is="metricIcons[index]" :size="22" /></span><span>{{ stat.title }}</span></div><strong>{{ displayedValues[index] ?? 0 }}<em>{{ stat.unit }}</em></strong><div class="metric-delta"><small>较昨日 <b>{{ index === 2 ? '-1' : index === 3 ? '0' : `+${index + 1}` }}</b></small><svg viewBox="0 0 90 28" aria-hidden="true"><polyline :class="`spark-${index}`" points="1,22 12,18 23,21 34,10 45,14 56,6 67,11 78,8 89,2" /></svg></div>
          </button>
        </section>

        <section class="dashboard-live-grid">
          <article class="business-panel trend-panel">
            <header class="panel-header"><div><span>近 {{ days }} 天日均访问量 <b>{{ Math.round(trend.reduce((sum, item) => sum + Number(item.value), 0) / Math.max(trend.length, 1)).toLocaleString() }}</b> 次</span><h3><TrendingUp :size="18" />资源访问趋势</h3></div><div class="chart-tools"><div class="segmented"><button v-for="value in [7, 30]" :key="value" type="button" :class="{ active: days === value }" @click="changeRange(value)">近 {{ value }} 天</button></div><button class="icon-button" type="button" title="下载 CSV 报表" @click="downloadReport"><Download :size="17" /></button></div></header>
            <div :key="chartAnimationKey" class="area-chart"><svg viewBox="0 0 100 100" preserveAspectRatio="none" aria-label="资源访问趋势"><defs><linearGradient id="trendFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#15935f" stop-opacity=".32"/><stop offset="1" stop-color="#15935f" stop-opacity="0"/></linearGradient><clipPath id="chartReveal"><rect class="chart-reveal" x="0" y="0" width="100" height="100" /></clipPath></defs><g class="chart-grid"><line v-for="y in [20,40,60,80]" :key="y" x1="0" :y1="y" x2="100" :y2="y" /></g><g class="chart-data" clip-path="url(#chartReveal)"><polygon :points="`5,92 ${chartLine} 95,92`" fill="url(#trendFill)"/><polyline :points="chartLine"/></g></svg><div class="chart-points" aria-hidden="true"><i v-for="point in chartPoints" :key="point.date" :title="`${point.date} · ${point.value} 次`" :style="{ left: `${point.x}%`, top: `${point.y}%` }" /></div><div class="chart-labels"><span v-for="(point, index) in trend" :key="point.date" :style="{ animationDelay: `${650 + index * 100}ms` }">{{ point.date }}</span></div></div>
            <footer><span><CalendarDays :size="14" />生成于 {{ generatedTime }}</span></footer>
          </article>

          <aside class="business-panel distribution-panel">
            <header class="panel-header"><div><span>资源总量 <b>{{ displayedValues[0] ?? 0 }} 项</b></span><h3><Layers3 :size="18" />资源类型分布</h3></div></header>
            <div :key="`donut-${chartAnimationKey}`" class="distribution-body">
              <div class="donut-chart" role="img" aria-label="资源类型分布环形图">
                <svg viewBox="0 0 120 120" aria-hidden="true">
                  <circle class="donut-track" cx="60" cy="60" r="46" pathLength="100" />
                  <circle
                    v-for="(segment, index) in resourceDistribution"
                    :key="segment.label"
                    class="donut-segment"
                    cx="60"
                    cy="60"
                    r="46"
                    pathLength="100"
                    :stroke="segment.color"
                    :style="{
                      '--segment-length': `${segment.value}`,
                      '--segment-offset': `${-segment.offset}`,
                      '--segment-delay': `${420 + index * 150}ms`,
                    }"
                  />
                </svg>
                <div><strong>{{ displayedValues[0] ?? 0 }}</strong><span>总数</span></div>
              </div>
              <ul><li v-for="item in resourceDistribution" :key="item.label"><button type="button" @click="drillCategory(item.label)"><i :style="{ background: item.color }" />{{ item.label }} <b>{{ item.value }}%</b><ArrowRight :size="13" /></button></li></ul>
            </div>
          </aside>

          <aside class="business-panel attention-panel risk-panel">
            <header class="panel-header"><div><span>风险提醒</span><h3>需要跟进</h3></div><AlertTriangle :size="20" /></header>
            <div v-if="data.risks?.length" class="attention-list"><article v-for="risk in data.risks" :key="risk.id"><div><strong>{{ risk.title }}</strong><b>高风险</b></div><p>{{ risk.detail }}</p><span>负责人：{{ risk.assignee }}</span><button type="button" @click="router.push({ path: '/approvals', query: { status: 'PENDING' } })">立即处理</button></article></div>
            <div v-else class="compact-empty">当前没有风险提醒</div>
          </aside>

          <article class="business-panel quick-panel"><header class="panel-header"><div><span>高频功能</span><h3>快捷入口</h3></div></header><div class="quick-grid"><RouterLink to="/approvals"><ClipboardCheck :size="21"/><span>审批工作台</span></RouterLink><RouterLink :to="{ path: '/approvals', query: { status: 'PENDING' } }"><FileBarChart :size="21"/><i v-if="todoCount">{{ todoCount }}</i><span>我的待办</span></RouterLink><RouterLink to="/resource-map"><MapPinned :size="21"/><span>资源地图</span></RouterLink><RouterLink to="/weekly-report"><TrendingUp :size="21"/><span>周报管理</span></RouterLink></div></article>

          <article class="business-panel suggestion-panel">
            <header class="panel-header"><div><span>智能辅助</span><h3><Sparkles :size="18" />智能运营建议</h3></div><Lightbulb :size="20" /></header>
            <div v-if="data.suggestions?.length" class="suggestion-list"><article v-for="item in data.suggestions" :key="item.id"><b>{{ item.priority }}</b><div><strong>{{ item.title }}</strong><p>{{ item.content }}</p></div><button type="button" @click="handleSuggestion(item.actionType)">{{ item.actionLabel || item.tag || '查看建议' }}<ArrowRight :size="14" /></button></article></div>
            <div v-else class="compact-empty">当前没有新的运营建议</div>
          </article>

        </section>
        <footer class="dashboard-footer"><span>数据生成时间：{{ generatedTime }}</span><span><i />系统正常运行</span><span>版本：v2.1.0</span></footer>
      </template>
    </PageState>
  </div>
</template>
