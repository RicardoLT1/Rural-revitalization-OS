const { getReportDashboard, getReportPeriods } = require('../../services/report');
const { buildBarOption, buildLineOption, buildRingOption } = require('../../utils/chart');
const { goForecast, goInvestmentMatch } = require('../../utils/navigation');

Page({
  data: {
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
  onPeriodChange(event) {
    this.loadReport(event.detail.key);
  },
  async loadReport(period) {
    const view = await getReportDashboard(period);
    this.setData({
      summary: view.summary,
      periods: view.periods,
      period: view.period,
      lineOption: buildLineOption(view.flowPoints),
      barOption: buildBarOption(view.revenueBar.labels, view.revenueBar.series),
      ringOption: buildRingOption(view.ratioRing.labels, view.ratioRing.values, view.ratioRing.colors),
      autoSummary: view.autoSummary,
      aiTips: view.aiTips
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
