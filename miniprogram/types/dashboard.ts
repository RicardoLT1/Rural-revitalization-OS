import { StatusType } from './common';

export type TrendType = 'up' | 'down' | 'flat';
export type SuggestionPriority = 'P0' | 'P1' | 'P2' | 'P3';
export type RiskLevel = 'high' | 'medium' | 'low';

export interface TrendPoint {
  date: string;
  value: number;
}

export interface TrendSeries {
  days7: TrendPoint[];
  days30: TrendPoint[];
}

export interface StatItem {
  key: string;
  title: string;
  value: number | string;
  unit: string;
  delta: number;
  trend: TrendType;
  status: StatusType;
  icon: string;
}

export interface RiskAlert {
  id: string;
  title: string;
  level: RiskLevel;
  detail: string;
  assignee: string;
}

export interface AiSuggestion {
  id: string;
  title: string;
  content: string;
  priority: SuggestionPriority;
  actionLabel: string;
  actionType: 'forecast' | 'match' | 'process' | 'report' | string;
  tag: string;
}

export interface DashboardQuickEntry {
  key: string;
  title: string;
  icon: string;
  action: 'map' | 'collab' | 'report' | 'match' | 'forecast' | string;
}

export interface DashboardMetrics {
  villageName: string;
  roleName: string;
  generatedAt?: string;
  rangeDays?: number;
  cacheStatus?: 'HIT' | 'MISS' | 'STALE' | string;
  stale?: boolean;
  stats: StatItem[];
  trends: TrendSeries;
  risks: RiskAlert[];
  suggestions: AiSuggestion[];
  quickEntries?: DashboardQuickEntry[];
}
