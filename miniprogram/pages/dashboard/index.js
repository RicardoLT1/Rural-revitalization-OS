const { dashboardData } = require('../../mock/dashboard');
const { buildLineOption } = require('../../utils/chart');
const { goCollab, goForecast, goInvestmentMatch, goMap, goReport } = require('../../utils/navigation');

Page({
  data: {
    roleName: dashboardData.roleName,
    villageName: dashboardData.villageName,
    stats: dashboardData.stats,
    risks: dashboardData.risks,
    suggestions: dashboardData.suggestions,
    periods: [
      { key: '7d', label: '\u8fd17\u5929' },
      { key: '30d', label: '\u8fd130\u5929' }
    ],
    trendPeriod: '7d',
    trendOption: buildLineOption(dashboardData.trends.days7)
  },
  onTrendPeriodChange(event) {
    const trendPeriod = event.detail.key;
    const trendData = trendPeriod === '30d' ? dashboardData.trends.days30 : dashboardData.trends.days7;
    this.setData({
      trendPeriod,
      trendOption: buildLineOption(trendData)
    });
  },
  onTipAction(event) {
    const actionType = event.detail.actionType;
    if (actionType === 'forecast') {
      goForecast();
      return;
    }
    if (actionType === 'match') {
      goInvestmentMatch();
      return;
    }
    if (actionType === 'process') {
      goCollab();
      return;
    }
    goReport();
  },
  onEntryTap(event) {
    const action = event.currentTarget.dataset.action;
    if (action === 'map') {
      goMap();
      return;
    }
    if (action === 'collab') {
      goCollab();
      return;
    }
    if (action === 'report') {
      goReport();
      return;
    }
    if (action === 'match') {
      goInvestmentMatch();
      return;
    }
    goForecast();
  }
});