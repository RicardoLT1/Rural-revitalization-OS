export type TrendType = 'up' | 'down' | 'flat';
export type StatusType = 'success' | 'warning' | 'danger' | 'info' | 'neutral';

export interface TrendPoint {
  date: string;
  value: number;
}

export interface StatItem {
  key: string;
  title: string;
  value: number;
  unit: string;
  delta: number;
  trend: TrendType;
  status: StatusType;
  icon: string;
}

export interface RiskAlert {
  id: string;
  title: string;
  level: 'high' | 'medium' | 'low';
  detail: string;
  assignee: string;
}

export interface AiSuggestion {
  id: string;
  title: string;
  content: string;
  priority: 'P1' | 'P2' | 'P3';
  actionLabel: string;
  actionType: 'report' | 'match' | 'forecast' | 'resource' | 'process';
  tag?: string;
}

export interface DashboardMetrics {
  villageName: string;
  roleName: string;
  stats: StatItem[];
  trends: {
    days7: TrendPoint[];
    days30: TrendPoint[];
  };
  risks: RiskAlert[];
  suggestions: AiSuggestion[];
}

export type ResourceCategory = '\u95f2\u7f6e\u519c\u623f' | '\u571f\u5730' | '\u6587\u65c5\u7a7a\u95f4';
export type InvestmentStatus = '\u53ef\u62db\u5546' | '\u6d3d\u8c08\u4e2d' | '\u5df2\u7b7e\u7ea6';

export interface InvestmentTag {
  key: string;
  label: string;
}

export interface ResourcePoint {
  id: string;
  name: string;
  category: ResourceCategory;
  lat: number;
  lng: number;
  address: string;
  area: number;
  annualEstimate: number;
  investmentStatus: InvestmentStatus;
  tags: string[];
  cover?: string;
}

export interface ResourceDetail extends ResourcePoint {
  intro: string;
  owner: string;
  contact: string;
  relatedProjects: string[];
  occupancyRate: number;
  expectedROI: number;
}

export interface InvestmentMatch {
  id: string;
  investor: string;
  score: number;
  reason: string;
  priority: '\u9ad8\u4f18\u5148' | '\u4e2d\u4f18\u5148' | '\u89c2\u5bdf';
  direction: string;
}

export interface TodoItem {
  id: string;
  title: string;
  dueDate: string;
  category: '\u9879\u76ee\u7533\u62a5' | '\u8d44\u4ea7\u6d41\u8f6c' | '\u6d3b\u52a8\u7b79\u5907' | '\u6751\u6c11\u8bae\u4e8b';
  status: '\u5f85\u5904\u7406' | '\u8fdb\u884c\u4e2d' | '\u5df2\u903e\u671f';
  processId: string;
}

export interface ApprovalItem {
  id: string;
  title: string;
  applicant: string;
  amount: number;
  status: '\u5f85\u5ba1\u6279' | '\u5df2\u9a73\u56de' | '\u5df2\u901a\u8fc7';
  processId: string;
  time: string;
}

export interface WorkflowNode {
  id: string;
  name: string;
  owner: string;
  status: 'done' | 'doing' | 'pending' | 'blocked';
  time: string;
  remark: string;
}

export interface ProcessRecord {
  id: string;
  operator: string;
  action: string;
  time: string;
  remark: string;
  nodeId: string;
}

export interface ArchiveRecord {
  id: string;
  code: string;
  archivedAt: string;
  archivedBy: string;
  note: string;
}

export interface ProcessDetail {
  id: string;
  title: string;
  status: string;
  currentNodeId: string;
  blocker?: string;
  nodes: WorkflowNode[];
  records: ProcessRecord[];
  archive: ArchiveRecord;
}

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