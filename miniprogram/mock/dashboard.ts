import { DashboardMetrics } from '../types';

export const dashboardData: DashboardMetrics = {
  villageName: '青禾示范村',
  roleName: '乡村CEO',
  stats: [
    { key: 'flow', title: '今日客流', value: 1864, unit: '人次', delta: 12.4, trend: 'up', status: 'success', icon: '🚶' },
    { key: 'revenue', title: '本月营收', value: 268.7, unit: '万元', delta: 8.6, trend: 'up', status: 'info', icon: '💰' },
    { key: 'progress', title: '项目进度', value: 74, unit: '%', delta: 4.2, trend: 'up', status: 'success', icon: '📈' },
    { key: 'risk', title: '风险预警', value: 3, unit: '项', delta: -1, trend: 'down', status: 'warning', icon: '⚠️' }
  ],
  trends: {
    days7: [
      { date: '03-30', value: 1320 },
      { date: '03-31', value: 1412 },
      { date: '04-01', value: 1498 },
      { date: '04-02', value: 1610 },
      { date: '04-03', value: 1580 },
      { date: '04-04', value: 1726 },
      { date: '04-05', value: 1864 }
    ],
    days30: [
      { date: 'W1', value: 8520 },
      { date: 'W2', value: 9060 },
      { date: 'W3', value: 9780 },
      { date: 'W4', value: 10360 }
    ]
  },
  risks: [
    { id: 'r1', title: '古桥片区排水隐患', level: 'high', detail: '连续降雨后存在路面积水风险', assignee: '工程协同组' },
    { id: 'r2', title: '民宿审批节点延迟', level: 'medium', detail: '审批已滞后2天，需加急处理', assignee: '运营审批组' },
    { id: 'r3', title: '周末停车承载预警', level: 'low', detail: '预测周末车流上浮15%', assignee: '现场调度组' }
  ],
  suggestions: [
    { id: 'a1', title: '调整活动时段分流客流', content: '建议将夜游活动提前至18:30，缓解主街拥堵并提升停留时长。', priority: 'P1', actionLabel: '查看趋势预测', actionType: 'forecast' },
    { id: 'a2', title: '优先推进闲置农房招商', content: 'A03、A07 两处资源与文旅轻餐投资方匹配度超过85%。', priority: 'P2', actionLabel: '查看招商推荐', actionType: 'match' },
    { id: 'a3', title: '复盘审批瓶颈节点', content: '资产流转流程在“乡镇复核”节点平均停留 1.8 天，建议优化审批并行策略。', priority: 'P2', actionLabel: '查看协同工作台', actionType: 'process' }
  ]
};
