const { investmentMatches, resourceDetails } = require('../../mock/resources');
const { goForecast, goResourceDetail } = require('../../utils/navigation');

Page({
  data: {
    resource: resourceDetails['res-01'],
    matches: [],
    aiSummary: {
      id: 'sum-1',
      title: '招商策略建议',
      content: '建议优先接洽高评分对象，并在7天内完成现场踏勘，缩短转化路径。',
      priority: 'P1',
      actionLabel: '查看趋势预测',
      actionType: 'forecast'
    }
  },
  onLoad(query) {
    const id = query.id || 'res-01';
    const resource = resourceDetails[id] || resourceDetails['res-01'];
    const source = investmentMatches[id] || investmentMatches['res-01'];
    const matches = source.map((item) => ({
      ...item,
      priorityType: item.priority === '高优先' ? 'danger' : item.priority === '中优先' ? 'warning' : 'info'
    }));
    this.setData({ resource, matches });
  },
  onResourceTap() {
    goResourceDetail(this.data.resource.id);
  },
  onAiAction() {
    goForecast();
  }
});