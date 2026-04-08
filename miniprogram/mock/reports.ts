import { ChartSeries, ReportSummary } from '../types';

export const reportSummary: ReportSummary[] = [
  { id: 's1', title: '\u672c\u6708\u7d2f\u8ba1\u5ba2\u6d41', value: '4.8 \u4e07\u4eba\u6b21', delta: '+11.2%' },
  { id: 's2', title: '\u672c\u6708\u7d2f\u8ba1\u8425\u6536', value: '268.7 \u4e07\u5143', delta: '+8.6%' },
  { id: 's3', title: '\u62db\u5546\u8f6c\u5316\u7387', value: '34%', delta: '+4.1%' }
];

export const flowLine: Record<'7d' | '30d', { labels: string[]; series: ChartSeries[] }> = {
  '7d': {
    labels: ['03-30', '03-31', '04-01', '04-02', '04-03', '04-04', '04-05'],
    series: [{ name: '\u5ba2\u6d41', values: [13200, 14120, 14980, 16100, 15800, 17260, 18640], color: '#2F7D32' }]
  },
  '30d': {
    labels: ['\u7b2c1\u5468', '\u7b2c2\u5468', '\u7b2c3\u5468', '\u7b2c4\u5468'],
    series: [{ name: '\u5ba2\u6d41', values: [85200, 90600, 97800, 103600], color: '#2F7D32' }]
  }
};

export const revenueBar: Record<'7d' | '30d', { labels: string[]; series: ChartSeries[] }> = {
  '7d': {
    labels: ['\u5468\u4e00', '\u5468\u4e8c', '\u5468\u4e09', '\u5468\u56db', '\u5468\u4e94', '\u5468\u516d', '\u5468\u65e5'],
    series: [{ name: '\u8425\u6536(\u4e07\u5143)', values: [21, 23, 24, 26, 30, 36, 39], color: '#6FAF5E' }]
  },
  '30d': {
    labels: ['\u7b2c1\u5468', '\u7b2c2\u5468', '\u7b2c3\u5468', '\u7b2c4\u5468'],
    series: [{ name: '\u8425\u6536(\u4e07\u5143)', values: [52, 61, 71, 84], color: '#6FAF5E' }]
  }
};

export const ratioRing = {
  labels: ['\u6587\u65c5\u6536\u5165', '\u519c\u4ea7\u54c1\u9500\u552e', '\u6d3b\u52a8\u670d\u52a1'],
  values: [52, 28, 20],
  colors: ['#2F7D32', '#6FAF5E', '#D58A2A']
};

export const reportAutoSummary = '\u81ea\u52a8\u7ed3\u8bba\uff1a\u8fd1 7 \u5929\u5ba2\u6d41\u6301\u7eed\u4e0a\u884c\uff0c\u5468\u672b\u5cf0\u503c\u660e\u663e\uff1b\u672c\u6708\u8425\u6536\u589e\u957f\u4e3b\u8981\u6765\u81ea\u591c\u95f4\u6587\u65c5\u6d88\u8d39\u3002\u5efa\u8bae\u7ee7\u7eed\u4f18\u5316\u591c\u6e38\u52a8\u7ebf\u5e76\u63d0\u524d\u90e8\u7f72\u505c\u8f66\u8c03\u5ea6\u3002';

export const reportAiTips = [
  '\u5efa\u8bae\u5c06\u6625\u8015\u6d3b\u52a8\u4e0e\u6587\u521b\u5de5\u574a\u8054\u52a8\uff0c\u63d0\u5347\u4e8c\u6b21\u6d88\u8d39\u7387\u3002',
  '\u62db\u5546\u7ebf\u7d22\u4e2d\uff0c\u8f7b\u9910\u996e\u4e0e\u6c11\u5bbf\u7c7b\u6295\u8d44\u65b9\u5339\u914d\u5ea6\u6700\u9ad8\u3002',
  '\u4e0b\u5468\u6709\u4e2d\u96e8\u8fc7\u7a0b\uff0c\u5efa\u8bae\u63d0\u524d\u8c03\u6574\u6237\u5916\u6d3b\u52a8\u6392\u671f\u3002'
];