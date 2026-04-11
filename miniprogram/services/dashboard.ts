import { isMockMode } from '../config/env';
import { dashboardData } from '../mock/dashboard';
import type { OptionItem } from '../types/common';
import type { DashboardMetrics, TrendPoint } from '../types/dashboard';
import { get } from '../utils/request';

export type TrendPeriod = '7d' | '30d';

export const getDashboardData = async (): Promise<DashboardMetrics> => {
  if (isMockMode()) {
    return dashboardData;
  }
  return get<DashboardMetrics>('/dashboard');
};

export const getDashboardTrend = async (period: TrendPeriod): Promise<TrendPoint[]> => {
  const data = await getDashboardData();
  return period === '30d' ? data.trends.days30 : data.trends.days7;
};

export const getDashboardPeriods = (): OptionItem<TrendPeriod>[] => [
  { key: '7d', label: '\u8fd17\u5929' },
  { key: '30d', label: '\u8fd130\u5929' }
];
