import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getReportDashboard, getReportPeriods } from '../../services/report';
import type { PageLoadState } from '../../types/common';
import type { ReportPeriod } from '../../types/report';
import { buildBarOption, buildLineOption, buildRingOption } from '../../utils/chart';
import { goForecast, goInvestmentMatch } from '../../utils/navigation';

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '\u667a\u80fd\u62a5\u8868\u52a0\u8f7d\u5931\u8d25',
    emptyTitle: '\u6682\u65e0\u62a5\u8868\u6570\u636e',
    emptyDescription: '\u5f53\u524d\u65f6\u95f4\u7ef4\u5ea6\u4e0b\u6ca1\u6709\u53ef\u5c55\u793a\u7684\u5206\u6790\u6570\u636e\u3002',
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
    this.loadReport(this.data.period as ReportPeriod);
  },
  onRetry() {
    this.loadReport(this.data.period as ReportPeriod);
  },
  onPeriodChange(event: WechatMiniprogram.CustomEvent<{ key: '7d' | '30d' }>) {
    this.loadReport(event.detail.key);
  },
  onPeriodTap(event: WechatMiniprogram.TouchEvent) {
    const key = event.currentTarget.dataset.key as ReportPeriod;
    if (key && key !== this.data.period) {
      this.loadReport(key);
    }
  },
  async loadReport(period: ReportPeriod) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const view = await getReportDashboard(period);
      const isEmpty = !view.summary.length && !view.flowPoints.length && !view.revenueBar.series.length && !view.ratioRing.values.length;
      this.setData({
        pageState: isEmpty ? PageState.Empty : PageState.Ready,
        isLoading: false,
        summary: view.summary,
        periods: view.periods,
        period: view.period,
        lineOption: buildLineOption(view.flowPoints),
        barOption: buildBarOption(view.revenueBar.labels, view.revenueBar.series),
        ringOption: buildRingOption(view.ratioRing.labels, view.ratioRing.values, view.ratioRing.colors),
        autoSummary: view.autoSummary,
        aiTips: view.aiTips
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onQuickTap(event: WechatMiniprogram.TouchEvent) {
    const action = event.currentTarget.dataset.action;
    if (action === 'match') {
      goInvestmentMatch();
      return;
    }
    goForecast();
  },
  onTipAction(event: WechatMiniprogram.CustomEvent<{ actionType: string }>) {
    if (event.detail.actionType === 'forecast') {
      goForecast();
      return;
    }
    goInvestmentMatch();
  }
});
