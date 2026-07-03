import { isMockMode } from '../config/env';
import { PRIORITY_STATUS_TYPE } from '../constants/status';
import { forecastBand, forecastData, forecastRisks, forecastStrategies } from '../mock/forecast';
import { flowLine, ratioRing, reportAiTips, reportAutoSummary, reportSummary, revenueBar } from '../mock/reports';
import type { AiSuggestion, TrendPoint } from '../types/dashboard';
import type { ForecastResult, InvestmentMatchView, ReportDashboardView, ReportPeriod } from '../types/report';
import { get } from '../utils/request';
import { getInvestmentMatches, getResourceDetail } from './resource';

const toFlowPoints = (period: ReportPeriod): TrendPoint[] => {
  return flowLine[period].labels.map((label, index) => ({
    date: label,
    value: flowLine[period].series[0].values[index]
  }));
};

export const getReportPeriods = () => [
  { key: '7d' as ReportPeriod, label: '\u8fd17\u5929' },
  { key: '30d' as ReportPeriod, label: '\u8fd130\u5929' }
];

const buildReportTips = (): AiSuggestion[] => reportAiTips.map((content, index) => ({
  id: 'tip-' + index,
  title: '\u5efa\u8bae ' + (index + 1),
  content,
  priority: index === 0 ? 'P1' : 'P2',
  actionLabel: index === 2 ? '\u67e5\u770b\u8d8b\u52bf\u9884\u6d4b' : '\u67e5\u770b\u62db\u5546\u63a8\u8350',
  actionType: index === 2 ? 'forecast' : 'match',
  tag: index === 0 ? '\u8fd0\u8425\u4f18\u5316' : index === 1 ? '\u62db\u5546\u5339\u914d' : '\u98ce\u9669\u89c4\u907f'
}));

export const getReportDashboard = async (period: ReportPeriod = '7d'): Promise<ReportDashboardView> => {
  if (!isMockMode('report')) {
    try {
      return await get<ReportDashboardView>('/reports/dashboard', { period });
    } catch (error) {
      return buildMockReportDashboard(period);
    }
  }

  return buildMockReportDashboard(period);
};

const buildMockReportDashboard = (period: ReportPeriod): ReportDashboardView => {
  return {
    summary: reportSummary,
    periods: getReportPeriods(),
    period,
    flowPoints: toFlowPoints(period),
    revenueBar: revenueBar[period],
    ratioRing,
    autoSummary: reportAutoSummary,
    aiTips: buildReportTips()
  };
};

export const getForecastView = async (): Promise<ForecastResult> => {
  if (!isMockMode('report')) {
    return get<ForecastResult>('/reports/forecast');
  }

  return {
    forecastData,
    band: forecastBand,
    risks: forecastRisks,
    strategies: forecastStrategies
  };
};

export const getInvestmentMatchView = async (resourceId = 'res-01'): Promise<InvestmentMatchView> => {
  const [resource, source] = await Promise.all([
    getResourceDetail(resourceId),
    getInvestmentMatches(resourceId)
  ]);

  return {
    resource,
    matches: source.map((item) => ({
      ...item,
      priorityType: PRIORITY_STATUS_TYPE[item.priority] || 'info'
    })),
    aiSummary: {
      id: 'sum-1',
      title: '\u62db\u5546\u7b56\u7565\u5efa\u8bae',
      content: '\u5efa\u8bae\u4f18\u5148\u63a5\u6d3d\u9ad8\u8bc4\u5206\u5bf9\u8c61\uff0c\u5e76\u5728 7 \u5929\u5185\u5b8c\u6210\u73b0\u573a\u8e0f\u52d8\uff0c\u7f29\u77ed\u8f6c\u5316\u8def\u5f84\u3002',
      priority: 'P1',
      actionLabel: '\u67e5\u770b\u8d8b\u52bf\u9884\u6d4b',
      actionType: 'forecast',
      tag: '\u667a\u80fd\u7b56\u7565'
    }
  };
};
