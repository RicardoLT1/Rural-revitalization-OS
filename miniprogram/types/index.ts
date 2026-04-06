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

export interface InvestmentTag {
  key: string;
  label: string;
}

export interface ResourcePoint {
  id: string;
  name: string;
  category: '闲置农房' | '土地' | '文旅空间';
  lat: number;
  lng: number;
  address: string;
  area: number;
  annualEstimate: number;
  investmentStatus: '可招商' | '洽谈中' | '已签约';
  tags: string[];
  cover: string;
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
  priority: '高优先' | '中优先' | '观察';
  direction: string;
}

export interface TodoItem {
  id: string;
  title: string;
  dueDate: string;
  category: '项目申报' | '资产流转' | '活动筹备' | '村民议事';
  status: '待处理' | '进行中' | '已逾期';
  processId: string;
}

export interface ApprovalItem {
  id: string;
  title: string;
  applicant: string;
  amount: number;
  status: '待审批' | '已退回' | '已通过';
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
