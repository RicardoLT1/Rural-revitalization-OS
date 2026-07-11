<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { CalendarRange, Check, FileCheck2, Save } from '@lucide/vue'
import { createWeeklyReport, fetchDashboard, fetchWeeklyReports } from '../api/business'
import PageState from '../components/PageState.vue'
import type { WeeklyReport } from '../types/business'

function isoDate(date: Date) { return date.toISOString().slice(0, 10) }
const today = new Date()
const start = new Date(today)
start.setDate(today.getDate() - 6)

const form = reactive({
  weekStart: isoDate(start), weekEnd: isoDate(today), title: '', summary: '', highlights: '', risks: '', nextWeekPlan: '',
})
const reports = ref<WeeklyReport[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const notice = ref('')
const selected = ref<WeeklyReport | null>(null)
const valid = computed(() => form.title.trim().length > 0 && form.summary.trim().length > 0 && form.weekStart <= form.weekEnd)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [history, dashboard] = await Promise.all([fetchWeeklyReports(), fetchDashboard(7)])
    reports.value = history
    if (!form.title) {
      form.title = `${form.weekStart} 至 ${form.weekEnd} 运营周报`
      const stats = dashboard.stats || []
      form.summary = stats.map(item => `${item.title} ${item.value}${item.unit || ''}`).join('；')
      form.highlights = dashboard.suggestions?.map(item => item.title).join('；') || ''
      form.risks = dashboard.risks?.map(item => item.title).join('；') || '本周无新增风险事项'
    }
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '无法读取周报数据'
  } finally { loading.value = false }
}

async function save() {
  if (!valid.value) return
  saving.value = true
  error.value = ''
  try {
    await createWeeklyReport({ ...form })
    notice.value = '周报已确认保存'
    await load()
    window.setTimeout(() => { notice.value = '' }, 2600)
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '周报保存失败'
  } finally { saving.value = false }
}

function formatDate(value: string) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '--' }
onMounted(load)
</script>

<template>
  <div class="business-page report-page">
    <section class="page-intro"><div><p>人工确认写入</p><h2>运营周报</h2></div><span class="manual-badge"><FileCheck2 :size="16" />工作人员确认后保存</span></section>
    <div v-if="notice" class="toast-notice"><Check :size="16" />{{ notice }}</div>
    <PageState :loading="loading" :error="error" @retry="load">
      <div class="report-layout">
        <form class="report-editor" @submit.prevent="save">
          <header><div><span>本周报告</span><h3>编辑正式内容</h3></div><CalendarRange :size="21" /></header>
          <div class="form-grid two"><label><span>开始日期</span><input v-model="form.weekStart" type="date" required /></label><label><span>结束日期</span><input v-model="form.weekEnd" type="date" required /></label></div>
          <label><span>周报标题</span><input v-model="form.title" maxlength="160" required /></label>
          <label><span>经营总结</span><textarea v-model="form.summary" rows="4" required /></label>
          <div class="form-grid two"><label><span>本周亮点</span><textarea v-model="form.highlights" rows="4" /></label><label><span>风险事项</span><textarea v-model="form.risks" rows="4" /></label></div>
          <label><span>下周计划</span><textarea v-model="form.nextWeekPlan" rows="4" placeholder="填写下一周的重点工作与负责人安排" /></label>
          <footer><p>当前内容由经营数据预填，保存前请人工复核。</p><button class="primary-button report-save" type="submit" :disabled="saving || !valid"><Save :size="17" />{{ saving ? '正在保存...' : '确认并保存' }}</button></footer>
        </form>
        <aside class="report-history"><header><span>历史记录</span><strong>{{ reports.length }} 份</strong></header><div v-if="reports.length" class="report-list"><button v-for="item in reports" :key="item.id" type="button" @click="selected = item"><span>{{ item.weekStart }} 至 {{ item.weekEnd }}</span><strong>{{ item.title }}</strong><small>{{ item.authorName || item.authorId }} · {{ formatDate(item.createdAt) }}</small></button></div><div v-else class="compact-empty">尚未保存正式周报</div></aside>
      </div>
    </PageState>
    <div v-if="selected" class="dialog-layer" @click.self="selected = null"><article class="report-preview"><header><div><span>{{ selected.weekStart }} 至 {{ selected.weekEnd }}</span><h3>{{ selected.title }}</h3></div><button class="secondary-button" type="button" @click="selected = null">关闭</button></header><section><h4>经营总结</h4><p>{{ selected.summary }}</p><h4>本周亮点</h4><p>{{ selected.highlights || '--' }}</p><h4>风险事项</h4><p>{{ selected.risks || '--' }}</p><h4>下周计划</h4><p>{{ selected.nextWeekPlan || '--' }}</p></section></article></div>
  </div>
</template>
