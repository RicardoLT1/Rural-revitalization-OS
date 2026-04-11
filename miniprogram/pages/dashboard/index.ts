import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getDashboardData, getDashboardPeriods, getDashboardTrend, TrendPeriod } from '../../services/dashboard';
import type { PageLoadState } from '../../types/common';
import { buildLineOption } from '../../utils/chart';
import { goCollab, goForecast, goInvestmentMatch, goMap, goReport } from '../../utils/navigation';

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '\u9996\u9875\u6570\u636e\u52a0\u8f7d\u5931\u8d25',
    emptyTitle: '\u6682\u65e0\u9a7e\u9a76\u8231\u6570\u636e',
    emptyDescription: '\u8bf7\u7a0d\u540e\u518d\u67e5\u770b\u5ba2\u6d41\u3001\u8425\u6536\u4e0e\u98ce\u9669\u6982\u89c8\u3002',
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
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const dashboard = await getDashboardData();
      const isEmpty = !dashboard.stats.length && !dashboard.risks.length && !dashboard.suggestions.length && !dashboard.trends.days7.length;
      this.setData({
        pageState: isEmpty ? PageState.Empty : PageState.Ready,
        isLoading: false,
        roleName: dashboard.roleName,
        villageName: dashboard.villageName,
        stats: dashboard.stats,
        risks: dashboard.risks,
        suggestions: dashboard.suggestions,
        trendOption: buildLineOption(dashboard.trends.days7)
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onRetry() {
    this.loadDashboard();
  },
  onTrendPeriodChange(event: WechatMiniprogram.CustomEvent<{ key: '7d' | '30d' }>) {
    this.loadTrend(event.detail.key);
  },
  async loadTrend(trendPeriod: TrendPeriod) {
    try {
      const trendData = await getDashboardTrend(trendPeriod);
      this.setData({
        trendPeriod,
        trendOption: buildLineOption(trendData)
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onTipAction(event: WechatMiniprogram.CustomEvent<{ actionType: string }>) {
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
  onEntryTap(event: WechatMiniprogram.TouchEvent) {
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
