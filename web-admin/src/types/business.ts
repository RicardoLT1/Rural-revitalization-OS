export interface DashboardStat {
  key: string
  title: string
  value: number | string
  unit?: string
  trend?: string
  changeLabel?: string
  changeValue?: number | string
  changeHint?: string
}

export interface TrendPoint {
  date: string
  value: number
}

export interface DashboardData {
  stats: DashboardStat[]
  resourceDistribution: Array<{ label: string; count: number; percentage: number }>
  trends: { days7: TrendPoint[]; days30: TrendPoint[] }
  risks: Array<{ id: string; title: string; detail: string; level: string; assignee: string }>
  suggestions: Array<{ id: string; title: string; content: string; priority: string; tag: string; actionLabel?: string; actionType?: string }>
  cacheStatus: string
  stale: boolean
  generatedAt: string
  rangeDays: number
}

export interface WorkflowItem {
  id: string | number
  processId: string | number
  title: string
  category?: string
  status: string
  dueDate?: string
  applicant?: string
  action?: string
  remark?: string
  time?: string
}

export interface WorkflowNode {
  id: string
  name: string
  owner: string
  status: string
  remark?: string
}

export interface WorkflowRecord {
  id: string
  nodeId: string
  operator: string
  action: string
  time: string
  remark?: string
}

export interface WorkflowDetail {
  id: string
  title: string
  status: string
  currentNodeId: string
  applicantName: string
  nodes: WorkflowNode[]
  records: WorkflowRecord[]
}

export interface WorkflowOperationLog {
  id: string | number
  workflowId: string | number
  resourceId?: string | number
  action: string
  operatorId?: string
  operatorName?: string
  remark?: string
  createdAt: string
}

export interface ResourceItem {
  id: string
  name: string
  category: string
  address: string
  area: number | null
  annualEstimate: number | null
  investmentStatus: string
  tags: string[]
  intro: string
  owner: string
  contact: string
  occupancyRate: number | null
  expectedROI: number | null
  ownershipStatus: string
  materialStatus: string
  lat?: number | null
  lng?: number | null
  relatedProjects?: string[]
  fieldPhotos?: string[]
  investmentNote: string
}

export interface ResourceActivityWorkflow {
  processId: string | number
  title: string
  status: string
  applicantName?: string
  approverName?: string
  createdAt?: string
  updatedAt?: string
}

export interface ResourceActivityItem {
  id: string
  action: string
  title?: string
  status?: string
  operatorName?: string
  remark?: string
  createdAt?: string
}

export interface ResourceActivity {
  resourceId: string
  currentStatus: string
  workflows: ResourceActivityWorkflow[]
  operationLogs: WorkflowOperationLog[]
  timeline: ResourceActivityItem[]
}

export interface WeeklyReport {
  id: string | number
  weekStart: string
  weekEnd: string
  title: string
  summary: string
  highlights: string
  risks: string
  nextWeekPlan: string
  authorId: string
  authorName: string
  status: string
  createdAt: string
}

export interface UserRow {
  id: string
  username: string
  displayName: string
  role: 'USER' | 'STAFF' | 'ADMIN'
  villageId: string
  enabled: boolean
}

export interface AdminAuditLog {
  id: string | number
  traceId?: string
  actorId?: string
  actorName?: string
  actorRole?: string
  villageId?: string
  module: string
  action: string
  targetType?: string
  targetId?: string
  requestMethod: string
  requestPath: string
  clientIp?: string
  userAgent?: string
  result: 'SUCCESS' | 'FAILURE'
  httpStatus: number
  detail?: string
  beforeData?: string
  afterData?: string
  createdAt: string
}
