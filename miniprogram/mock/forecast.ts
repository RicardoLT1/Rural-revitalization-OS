import { ForecastBand, ForecastPoint, RiskAlert } from '../types';

export const forecastData: ForecastPoint[] = [
  { date: '04-01', actual: 1498, predict: 1498, upper: 1560, lower: 1420 },
  { date: '04-02', actual: 1610, predict: 1610, upper: 1688, lower: 1532 },
  { date: '04-03', actual: 1580, predict: 1580, upper: 1664, lower: 1496 },
  { date: '04-04', actual: 1726, predict: 1726, upper: 1820, lower: 1630 },
  { date: '04-05', actual: 1864, predict: 1864, upper: 1970, lower: 1760 },
  { date: '04-06', predict: 1910, upper: 2035, lower: 1780 },
  { date: '04-07', predict: 1980, upper: 2120, lower: 1840 },
  { date: '04-08', predict: 2050, upper: 2205, lower: 1895 }
];

export const forecastBand: ForecastBand = {
  confidence: '87%',
  range: '未来3天客流预计在 1780 - 2205 人次区间波动'
};

export const forecastRisks: RiskAlert[] = [
  { id: 'fr1', title: '周末停车承载超阈值', level: 'medium', detail: '预测周六车流高于停车能力上限 12%', assignee: '现场调度组' },
  { id: 'fr2', title: '降雨影响户外活动', level: 'low', detail: '预计周二有中雨，户外活动转移需预案', assignee: '活动运营组' }
];

export const forecastStrategies = [
  '将夜游开场提前 30 分钟，引导客流分段入场。',
  '在主入口增设临时导览点，缩短游客决策路径。',
  '对露营草坪活动设置预约上限，平衡体验与安全。'
];
