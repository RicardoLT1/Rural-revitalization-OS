import { ForecastBand, ForecastPoint, RiskAlert } from '../types';

export const forecastData: ForecastPoint[] = [
  { date: '04-01', actual: 14980, predict: 14980, upper: 15600, lower: 14200 },
  { date: '04-02', actual: 16100, predict: 16100, upper: 16880, lower: 15320 },
  { date: '04-03', actual: 15800, predict: 15800, upper: 16640, lower: 14960 },
  { date: '04-04', actual: 17260, predict: 17260, upper: 18200, lower: 16300 },
  { date: '04-05', actual: 18640, predict: 18640, upper: 19700, lower: 17600 },
  { date: '04-06', predict: 19100, upper: 20350, lower: 17800 },
  { date: '04-07', predict: 19800, upper: 21200, lower: 18400 },
  { date: '04-08', predict: 20500, upper: 22050, lower: 18950 }
];

export const forecastBand: ForecastBand = {
  confidence: '87%',
  range: '\u672a\u6765 3 \u5929\u5ba2\u6d41\u9884\u8ba1\u5728 1.78 \u4e07 - 2.20 \u4e07\u4eba\u6b21\u533a\u95f4\u6ce2\u52a8'
};

export const forecastRisks: RiskAlert[] = [
  { id: 'fr1', title: '\u5468\u672b\u505c\u8f66\u627f\u8f7d\u8d85\u9608\u503c', level: 'medium', detail: '\u9884\u6d4b\u5468\u516d\u8f66\u6d41\u9ad8\u4e8e\u505c\u8f66\u80fd\u529b\u4e0a\u9650 12%\u3002', assignee: '\u73b0\u573a\u8c03\u5ea6\u7ec4' },
  { id: 'fr2', title: '\u964d\u96e8\u5f71\u54cd\u6237\u5916\u6d3b\u52a8', level: 'low', detail: '\u9884\u8ba1\u5468\u4e8c\u6709\u4e2d\u96e8\uff0c\u6237\u5916\u6d3b\u52a8\u9700\u542f\u52a8\u8f6c\u79fb\u9884\u6848\u3002', assignee: '\u6d3b\u52a8\u8fd0\u8425\u7ec4' }
];

export const forecastStrategies = [
  '\u5c06\u591c\u6e38\u5f00\u573a\u63d0\u524d 30 \u5206\u949f\uff0c\u5f15\u5bfc\u5ba2\u6d41\u5206\u6bb5\u5165\u573a\u3002',
  '\u5728\u4e3b\u5165\u53e3\u589e\u8bbe\u4e34\u65f6\u5bfc\u89c8\u70b9\uff0c\u7f29\u77ed\u6e38\u5ba2\u51b3\u7b56\u8def\u5f84\u3002',
  '\u5bf9\u9732\u8425\u8349\u576a\u6d3b\u52a8\u8bbe\u7f6e\u9884\u7ea6\u4e0a\u9650\uff0c\u5e73\u8861\u4f53\u9a8c\u4e0e\u5b89\u5168\u3002'
];