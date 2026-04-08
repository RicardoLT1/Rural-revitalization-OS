import { flowLine, ratioRing, reportAiTips, reportAutoSummary, reportSummary, revenueBar } from '../../mock/reports';
import { buildBarOption, buildLineOption, buildRingOption } from '../../utils/chart';
import { goForecast, goInvestmentMatch } from '../../utils/navigation';

const toFlowPoints = (period: '7d' | '30d') => {
  return flowLine[period].labels.map((label, index) => ({
    date: label,
    value: flowLine[period].series[0].values[index]
  }));
};

Page({
  data: {
    summary: reportSummary,
    periods: [
      { key: '7d', label: '\u8fd17\u5929' },
      { key: '30d', label: '\u8fd130\u5929' }
    ],
    period: '7d',
    lineOption: buildLineOption(toFlowPoints('7d')),
    barOption: buildBarOption(revenueBar['7d'].labels, revenueBar['7d'].series),
    ringOption: buildRingOption(ratioRing.labels, ratioRing.values, ratioRing.colors),
    autoSummary: reportAutoSummary,
    aiTips: reportAiTips.map((content, index) => ({
      id: 'tip-' + index,
      title: '\u5efa\u8bae ' + (index + 1),
      content,
      priority: index === 0 ? 'P1' : 'P2',
      actionLabel: index === 2 ? '\u67e5\u770b\u8d8b\u52bf\u9884\u6d4b' : '\u67e5\u770b\u62db\u5546\u63a8\u8350',
      actionType: index === 2 ? 'forecast' : 'match',
      tag: index === 0 ? '\u8fd0\u8425\u4f18\u5316' : index === 1 ? '\u62db\u5546\u5339\u914d' : '\u98ce\u9669\u89c4\u907f'
    }))
  },

  onPeriodChange(event: WechatMiniprogram.CustomEvent<{ key: '7d' | '30d' }>) {
    const period = event.detail.key;
    this.setData({
      period,
      lineOption: buildLineOption(toFlowPoints(period)),
      barOption: buildBarOption(revenueBar[period].labels, revenueBar[period].series)
    });
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