export interface DashboardStat {
  key: string
  title: string
  value: number | string
  unit?: string
  trend?: string
}

export interface TrendPoint {
  date: string
  value: number
}

export interface DashboardData {
  stats: DashboardStat[]
  trends: { days7: TrendPoint[]; days30: TrendPoint[] }
  risks: Array<{ id: string; title: string; detail: string; level: string; assignee: string }>
  suggestions: Array<{ id: string; title: string; content: string; priority: string; tag: string }>
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
  investmentNote: string
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
