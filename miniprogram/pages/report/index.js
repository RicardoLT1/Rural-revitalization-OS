const { dashboardData } = require('../../mock/dashboard');
const { flowLine, ratioRing, reportAiTips, reportAutoSummary, reportSummary, revenueBar } = require('../../mock/reports');
const { buildBarOption, buildLineOption, buildRingOption } = require('../../utils/chart');
const { goForecast, goInvestmentMatch } = require('../../utils/navigation');

Page({
  data: {
    summary: reportSummary,
    periods: [
      { key: '7d', label: '近7天' },
      { key: '30d', label: '近30天' }
    ],
    period: '7d',
    lineOption: buildLineOption(dashboardData.trends.days7),
    barOption: buildBarOption(revenueBar['7d'].labels, revenueBar['7d'].series),
    ringOption: buildRingOption(ratioRing.labels, ratioRing.values, ratioRing.colors),
    autoSummary: reportAutoSummary,
    aiTips: reportAiTips.map((content, index) => ({
      id: `tip-${index}`,
      title: `建议 ${index + 1}`,
      content,
      priority: index === 0 ? 'P1' : 'P2',
      actionLabel: index === 2 ? '查看趋势预测' : '查看招商推荐',
      actionType: index === 2 ? 'forecast' : 'match'
    }))
  },
  onPeriodChange(event) {
    const period = event.detail.key;
    this.setData({
      period,
      lineOption: buildLineOption(flowLine[period].labels.map((label, index) => ({
        date: label,
        value: flowLine[period].series[0].values[index]
      }))),
      barOption: buildBarOption(revenueBar[period].labels, revenueBar[period].series)
    });
  },
  onQuickTap(event) {
    const action = event.currentTarget.dataset.action;
    if (action === 'match') {
      goInvestmentMatch();
      return;
    }
    goForecast();
  },
  onTipAction(event) {
    if (event.detail.actionType === 'forecast') {
      goForecast();
      return;
    }
    goInvestmentMatch();
  }
});