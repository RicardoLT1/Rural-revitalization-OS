import { dashboardData } from '../../mock/dashboard';
import { buildLineOption } from '../../utils/chart';
import { goCollab, goForecast, goInvestmentMatch, goMap, goReport } from '../../utils/navigation';

Page({
  data: {
    roleName: dashboardData.roleName,
    villageName: dashboardData.villageName,
    stats: dashboardData.stats,
    risks: dashboardData.risks,
    suggestions: dashboardData.suggestions,
    periods: [
      { key: '7d', label: '近7天' },
      { key: '30d', label: '近30天' }
    ],
    trendPeriod: '7d',
    trendOption: buildLineOption(dashboardData.trends.days7)
  },
  onTrendPeriodChange(event: WechatMiniprogram.CustomEvent<{ key: '7d' | '30d' }>) {
    const trendPeriod = event.detail.key;
    const trendData = trendPeriod === '30d' ? dashboardData.trends.days30 : dashboardData.trends.days7;
    this.setData({
      trendPeriod,
      trendOption: buildLineOption(trendData)
    });
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
