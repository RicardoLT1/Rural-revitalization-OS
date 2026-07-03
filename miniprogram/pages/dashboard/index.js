const { getDashboardData, getDashboardPeriods, getDashboardTrend } = require('../../services/dashboard');
const { buildLineOption } = require('../../utils/chart');
const { goCollab, goForecast, goInvestmentMatch, goMap, goReport } = require('../../utils/navigation');

const PageState = { Loading: 'loading', Ready: 'ready', Empty: 'empty', Error: 'error' };
const DEFAULT_LOADING_TEXT = '正在加载乡耘 OS 数据...';
const getErrorMessage = (error) => error && error.message ? error.message : '首页数据加载失败，请稍后重试';
const safeArray = (value) => Array.isArray(value) ? value : [];

Page({
  data: {
    pageState: PageState.Loading,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '首页数据加载失败',
    emptyTitle: '暂无驾驶舱数据',
    emptyDescription: '请稍后再查看资源、申请与审批概览。',
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

  onRetry() {
    this.loadDashboard();
  },

  async loadDashboard() {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const dashboard = await getDashboardData();
      const stats = safeArray(dashboard && dashboard.stats);
      const risks = safeArray(dashboard && dashboard.risks);
      const suggestions = safeArray(dashboard && dashboard.suggestions);
      const trends = dashboard && dashboard.trends ? dashboard.trends : {};
      const days7 = safeArray(trends.days7);
      const isEmpty = !stats.length && !risks.length && !suggestions.length && !days7.length;
      this.setData({
        pageState: isEmpty ? PageState.Empty : PageState.Ready,
        isLoading: false,
        roleName: dashboard && dashboard.roleName ? dashboard.roleName : 'USER',
        villageName: dashboard && dashboard.villageName ? dashboard.villageName : '青耘村',
        stats,
        risks,
        suggestions,
        trendOption: buildLineOption(days7)
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },

  async onTrendPeriodChange(event) {
    const trendPeriod = event.detail.key;
    try {
      const trendData = await getDashboardTrend(trendPeriod);
      this.setData({ trendPeriod, trendOption: buildLineOption(safeArray(trendData)) });
    } catch (error) {
      wx.showToast({ title: getErrorMessage(error), icon: 'none' });
    }
  },

  onTipAction(event) {
    const actionType = event.detail.actionType;
    if (actionType === 'forecast') return goForecast();
    if (actionType === 'match') return goInvestmentMatch();
    if (actionType === 'process') return goCollab();
    goReport();
  },

  onEntryTap(event) {
    const action = event.currentTarget.dataset.action;
    if (action === 'map') return goMap();
    if (action === 'collab') return goCollab();
    if (action === 'report') return goReport();
    if (action === 'match') return goInvestmentMatch();
    goForecast();
  }
});