const { getReportDashboard, getReportPeriods } = require('../../services/report');
const { buildBarOption, buildLineOption, buildRingOption } = require('../../utils/chart');
const { goForecast, goInvestmentMatch } = require('../../utils/navigation');

const PageState = { Loading: 'loading', Ready: 'ready', Empty: 'empty', Error: 'error' };
const getErrorMessage = (error) => error && error.message ? error.message : '智能报表加载失败，请稍后重试';
const safeArray = (value) => Array.isArray(value) ? value : [];

Page({
  data: {
    pageState: PageState.Loading,
    isLoading: true,
    errorMessage: '',
    loadingText: '正在加载乡耘 OS 数据...',
    errorTitle: '智能报表加载失败',
    emptyTitle: '暂无报表数据',
    emptyDescription: '当前时间维度下没有可展示的分析数据。',
    summary: [],
    periods: getReportPeriods(),
    period: '7d',
    lineOption: buildLineOption([]),
    barOption: buildBarOption([], []),
    ringOption: buildRingOption([], [], []),
    autoSummary: '',
    aiTips: []
  },

  onLoad() {
    this.loadReport(this.data.period);
  },

  onRetry() {
    this.loadReport(this.data.period);
  },

  onPeriodChange(event) {
    this.loadReport(event.detail.key);
  },

  async loadReport(period) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const view = await getReportDashboard(period);
      const summary = safeArray(view && view.summary);
      const flowPoints = safeArray(view && view.flowPoints);
      const revenueBar = view && view.revenueBar ? view.revenueBar : { labels: [], series: [] };
      const ratioRing = view && view.ratioRing ? view.ratioRing : { labels: [], values: [], colors: [] };
      const aiTips = safeArray(view && view.aiTips);
      const isEmpty = !summary.length && !flowPoints.length && !safeArray(revenueBar.series).length && !safeArray(ratioRing.values).length;
      this.setData({
        pageState: isEmpty ? PageState.Empty : PageState.Ready,
        isLoading: false,
        summary,
        periods: view && view.periods ? view.periods : getReportPeriods(),
        period,
        lineOption: buildLineOption(flowPoints),
        barOption: buildBarOption(safeArray(revenueBar.labels), safeArray(revenueBar.series)),
        ringOption: buildRingOption(safeArray(ratioRing.labels), safeArray(ratioRing.values), safeArray(ratioRing.colors)),
        autoSummary: view && view.autoSummary ? view.autoSummary : '暂无自动摘要。',
        aiTips
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },

  onQuickTap(event) {
    const action = event.currentTarget.dataset.action;
    if (action === 'match') return goInvestmentMatch();
    goForecast();
  },

  onTipAction(event) {
    if (event.detail.actionType === 'forecast') return goForecast();
    goInvestmentMatch();
  }
});