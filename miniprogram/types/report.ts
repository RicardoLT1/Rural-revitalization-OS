import { AiSuggestion, RiskAlert, TrendPoint } from './dashboard';
import { OptionItem } from './common';
import { InvestmentMatchViewItem, ResourceDetail } from './resource';

export type ReportPeriod = '7d' | '30d';
export interface ChartSeries {
  name: string;
  values: number[];
  color?: string;
}

export interface ReportSummary {
  id: string;
  title: string;
  value: string;
  delta: string;
}

export interface ReportSeries {
  labels: string[];
  series: ChartSeries[];
}

export type RevenueBarData = ReportSeries;

export interface RevenueRatioItem {
  label: string;
  value: number;
  color: string;
}

export interface RevenueRatioData {
  labels: string[];
  values: number[];
  colors: string[];
}

export interface ForecastPoint {
  date: string;
  actual?: number;
  predict: number;
  upper: number;
  lower: number;
}

export interface ForecastBand {
  confidence: string;
  range: string;
}

export interface ForecastResult {
  forecastData: ForecastPoint[];
  band: ForecastBand;
  risks: RiskAlert[];
  strategies: string[];
}

export interface ReportDashboardView {
  summary: ReportSummary[];
  periods: OptionItem<ReportPeriod>[];
  period: ReportPeriod;
  flowPoints: TrendPoint[];
  revenueBar: RevenueBarData;
  ratioRing: RevenueRatioData;
  autoSummary: string;
  aiTips: AiSuggestion[];
}

export interface InvestmentMatchView {
  resource: ResourceDetail;
  matches: InvestmentMatchViewItem[];
  aiSummary: AiSuggestion;
}