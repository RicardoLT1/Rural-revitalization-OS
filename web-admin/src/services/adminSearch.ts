import type { ResourceItem, UserRow, WeeklyReport, WorkflowItem } from '../types/business'

export interface AdminSearchResult {
  id: string
  type: '资源' | '流程' | '周报' | '用户'
  title: string
  meta: string
  to: string | { path: string; query: Record<string, string> }
}

export interface AdminSearchIndex {
  resources: ResourceItem[]
  workflows: WorkflowItem[]
  reports: WeeklyReport[]
  users: UserRow[]
}

const RECENT_KEY = 'xiangyun.admin.recent-search-results'

export function searchAdminIndex(query: string, index: AdminSearchIndex): AdminSearchResult[] {
  const term = query.trim().toLowerCase()
  if (!term) return []
  const matches = (value: string) => value.toLowerCase().includes(term)
  return [
    ...index.resources.filter((item) => matches(`${item.name} ${item.category} ${item.address || ''}`)).slice(0, 4).map((item): AdminSearchResult => ({ id: `resource-${item.id}`, type: '资源', title: item.name, meta: `${item.category} · ${item.address || '地址待完善'}`, to: `/resources/${item.id}` })),
    ...index.workflows.filter((item) => matches(`${item.title} ${item.processId || item.id} ${item.applicant || ''}`)).slice(0, 4).map((item): AdminSearchResult => ({ id: `workflow-${item.processId || item.id}`, type: '流程', title: item.title || '合作申请', meta: `流程 ${item.processId || item.id} · ${item.status}`, to: { path: '/approvals', query: { workflow: String(item.processId || item.id) } } })),
    ...index.reports.filter((item) => matches(`${item.title} ${item.summary}`)).slice(0, 3).map((item): AdminSearchResult => ({ id: `report-${item.id}`, type: '周报', title: item.title, meta: `${item.weekStart} 至 ${item.weekEnd}`, to: '/weekly-report' })),
    ...index.users.filter((item) => matches(`${item.username} ${item.displayName}`)).slice(0, 3).map((item): AdminSearchResult => ({ id: `user-${item.id}`, type: '用户', title: item.displayName, meta: `${item.username} · ${item.role}`, to: '/users' })),
  ].slice(0, 10)
}

export function recentSearchResults(): AdminSearchResult[] {
  try { return JSON.parse(localStorage.getItem(RECENT_KEY) || '[]') as AdminSearchResult[] }
  catch { return [] }
}

export function rememberSearchResult(result: AdminSearchResult) {
  const next = [result, ...recentSearchResults().filter((item) => item.id !== result.id)].slice(0, 5)
  localStorage.setItem(RECENT_KEY, JSON.stringify(next))
  return next
}
