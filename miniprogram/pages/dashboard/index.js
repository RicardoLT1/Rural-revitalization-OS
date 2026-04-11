const { getDashboardData, getDashboardPeriods, getDashboardTrend } = require('../../services/dashboard');
const { buildLineOption } = require('../../utils/chart');
const { goCollab, goForecast, goInvestmentMatch, goMap, goReport } = require('../../utils/navigation');

Page({
  data: {
    roleName: '',
    villageName: '',
    stats: [],
    risks: [],
    suggestions: [],
    periods: getDashboardPeriods(),
    trendPeriod: '7d',
    trendOption: buildLineOption([])
  },
  onLoad() {
    this.loadDashboard();
  },
  async loadDashboard() {
    const dashboard = await getDashboardData();
    this.setData({
      roleName: dashboard.roleName,
      villageName: dashboard.villageName,
      stats: dashboard.stats,
      risks: dashboard.risks,
      suggestions: dashboard.suggestions,
      trendOption: buildLineOption(dashboard.trends.days7)
    });
  },
  onTrendPeriodChange(event) {
    const trendPeriod = event.detail.key;
    this.loadTrend(trendPeriod);
  },
  async loadTrend(trendPeriod) {
    const trendData = await getDashboardTrend(trendPeriod);
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
