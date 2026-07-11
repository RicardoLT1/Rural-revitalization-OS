<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { AlertTriangle, CalendarDays, Lightbulb, RefreshCw, TrendingUp } from '@lucide/vue'
import { fetchDashboard } from '../api/business'
import PageState from '../components/PageState.vue'
import type { DashboardData } from '../types/business'

const data = ref<DashboardData | null>(null)
const days = ref(7)
const loading = ref(true)
const error = ref('')

const trend = computed(() => days.value === 30 ? data.value?.trends?.days30 || [] : data.value?.trends?.days7 || [])
const maxValue = computed(() => Math.max(...trend.value.map((item) => Number(item.value)), 1))
const generatedTime = computed(() => data.value?.generatedAt ? new Date(data.value.generatedAt).toLocaleString('zh-CN', { hour12: false }) : '--')

async function load() {
  loading.value = true
  error.value = ''
  try {
    data.value = await fetchDashboard(days.value)
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

onMounted(load)
</script>

<template>
  <div class="dashboard-page">
    <section class="page-intro">
      <div><p>实时经营视图</p><h2>把需要判断的事，放在最前面</h2></div>
      <div class="page-actions"><span class="freshness"><i :class="{ stale: data?.stale }" />{{ data?.stale ? '降级数据' : '数据正常' }}</span><button class="icon-button" type="button" title="刷新看板" @click="load"><RefreshCw :size="17" /></button></div>
    </section>

    <PageState :loading="loading" :error="error" @retry="load">
      <template v-if="data">
        <section class="metric-grid live-metrics">
          <article v-for="stat in data.stats" :key="stat.key" class="metric-card">
            <div class="metric-key">{{ stat.key.toUpperCase() }}</div><span>{{ stat.title }}</span><strong>{{ stat.value }}<em>{{ stat.unit }}</em></strong><small>{{ stat.trend === 'up' ? '保持关注' : '当前稳定' }}</small>
          </article>
        </section>

        <section class="dashboard-live-grid">
          <article class="business-panel trend-panel">
            <header class="panel-header"><div><span>运营趋势</span><h3>资源访问热度</h3></div><div class="segmented"><button v-for="value in [7, 30]" :key="value" type="button" :class="{ active: days === value }" @click="changeRange(value)">近 {{ value }} 天</button></div></header>
            <div class="bar-chart" :style="{ '--columns': trend.length }">
              <div v-for="point in trend" :key="`${point.date}-${point.value}`" class="bar-column"><span>{{ point.value }}</span><i :style="{ height: `${Math.max(8, Number(point.value) / maxValue * 100)}%` }" /><small>{{ point.date }}</small></div>
            </div>
            <footer><span><CalendarDays :size="14" />生成于 {{ generatedTime }}</span><span>缓存状态 {{ data.cacheStatus || '--' }}</span></footer>
          </article>

          <aside class="business-panel attention-panel">
            <header class="panel-header"><div><span>风险提醒</span><h3>需要跟进</h3></div><AlertTriangle :size="20" /></header>
            <div v-if="data.risks?.length" class="attention-list"><article v-for="risk in data.risks" :key="risk.id"><strong>{{ risk.title }}</strong><p>{{ risk.detail }}</p><span>负责人：{{ risk.assignee }}</span></article></div>
            <div v-else class="compact-empty">当前没有风险提醒</div>
          </aside>

          <article class="business-panel suggestion-panel">
            <header class="panel-header"><div><span>运营建议</span><h3>辅助判断</h3></div><Lightbulb :size="20" /></header>
            <div v-if="data.suggestions?.length" class="suggestion-list"><article v-for="item in data.suggestions" :key="item.id"><b>{{ item.priority }}</b><div><strong>{{ item.title }}</strong><p>{{ item.content }}</p></div><span>{{ item.tag }}</span></article></div>
            <div v-else class="compact-empty">当前没有新的运营建议</div>
          </article>

          <aside class="business-panel gate-summary"><TrendingUp :size="22" /><span>阶段 3 · 第二批</span><h3>真实业务数据已接入</h3><p>看板、审批与资源页面开始替换静态 Demo，旧 Web 仍保留至完整验收。</p></aside>
        </section>
      </template>
    </PageState>
  </div>
</template>
