<script setup lang="ts">
import { ArrowUpRight, CalendarDays, CheckCircle2, Clock3, LandPlot } from '@lucide/vue'

const metrics = [
  { label: '待处理审批', value: '--', note: '等待后端数据', icon: Clock3 },
  { label: '在库资源', value: '--', note: '资源档案总量', icon: LandPlot },
  { label: '本周已办结', value: '--', note: '流程处理结果', icon: CheckCircle2 },
]
const tasks = [
  ['合作申请审批', '即将接入 /workflows/todos', '审批工作台'],
  ['资源档案维护', '即将接入 /resources', '资源档案'],
  ['经营周报草稿', '人工确认后方可发布', '周报草稿'],
]
</script>

<template>
  <div class="dashboard-page">
    <section class="page-intro"><div><p>今日工作概览</p><h2>把需要判断的事，放在最前面</h2></div><button class="date-chip" type="button"><CalendarDays :size="17" />近 7 天</button></section>
    <section class="metric-grid">
      <article v-for="metric in metrics" :key="metric.label" class="metric-card"><div class="metric-icon"><component :is="metric.icon" :size="20" /></div><span>{{ metric.label }}</span><strong>{{ metric.value }}</strong><small>{{ metric.note }}</small></article>
      <article class="metric-card signature-card"><span>阶段 3</span><strong>正式 Web 管理端</strong><small>当前为第一批工程基线</small><ArrowUpRight :size="20" /></article>
    </section>
    <section class="work-grid">
      <article class="work-panel"><header><div><span>工作队列</span><h3>下一批业务接入</h3></div><small>按阶段门槛推进</small></header><div class="task-list"><div v-for="(task, index) in tasks" :key="task[0]" class="task-row"><b>{{ String(index + 1).padStart(2, '0') }}</b><div><strong>{{ task[0] }}</strong><span>{{ task[1] }}</span></div><RouterLink :to="index === 0 ? '/approvals' : index === 1 ? '/resources' : '/weekly-report'">{{ task[2] }}</RouterLink></div></div></article>
      <aside class="gate-panel"><span>PHASE GATE</span><h3>静态 Web 继续保留</h3><p>正式管理端完成登录、审批、资源和看板联调前，原有演示页面仍是可靠回退入口。</p><div class="gate-progress"><i /><i /><i /><i /></div><small>工程骨架已完成 · 业务联调待进行</small></aside>
    </section>
  </div>
</template>
