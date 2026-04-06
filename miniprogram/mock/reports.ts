import { ChartSeries, ReportSummary } from '../types';

export const reportSummary: ReportSummary[] = [
  { id: 's1', title: '本月累计客流', value: '4.8 万', delta: '+11.2%' },
  { id: 's2', title: '本月累计营收', value: '268.7 万', delta: '+8.6%' },
  { id: 's3', title: '招商转化率', value: '34%', delta: '+4.1%' }
];

export const flowLine: Record<'7d' | '30d', { labels: string[]; series: ChartSeries[] }> = {
  '7d': {
    labels: ['03-30', '03-31', '04-01', '04-02', '04-03', '04-04', '04-05'],
    series: [{ name: '客流', values: [1320, 1412, 1498, 1610, 1580, 1726, 1864], color: '#2F7D32' }]
  },
  '30d': {
    labels: ['W1', 'W2', 'W3', 'W4'],
    series: [{ name: '客流', values: [8520, 9060, 9780, 10360], color: '#2F7D32' }]
  }
};

export const revenueBar: Record<'7d' | '30d', { labels: string[]; series: ChartSeries[] }> = {
  '7d': {
    labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    series: [{ name: '营收(万)', values: [21, 23, 24, 26, 30, 36, 39], color: '#6FAF5E' }]
  },
  '30d': {
    labels: ['第1周', '第2周', '第3周', '第4周'],
    series: [{ name: '营收(万)', values: [52, 61, 71, 84], color: '#6FAF5E' }]
  }
};

export const ratioRing = {
  labels: ['文旅收入', '农产品', '活动服务'],
  values: [52, 28, 20],
  colors: ['#2F7D32', '#6FAF5E', '#D58A2A']
};

export const reportAutoSummary = '系统自动结论：近7天客流持续上行，周末峰值明显。本月营收增长主要来自文旅空间夜间消费，建议继续优化夜游动线并提前部署停车调度。';

export const reportAiTips = [
  '建议将“春耕活动”与文创工坊联动，提升二次消费率。',
  '招商线索中，轻餐饮与民宿类投资方匹配度最高。',
  '下周有中雨过程，建议提前调整户外活动排期。'
];
