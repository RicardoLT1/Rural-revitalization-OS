import type { AdminSearchApiItem } from '../api/search'

export interface AdminSearchResult {
  id: string
  type: '资源' | '流程' | '周报' | '用户'
  title: string
  meta: string
  to: string | { path: string; query: Record<string, string> }
}

const RECENT_KEY = 'xiangyun.admin.recent-search-results'

const labels = { RESOURCE: '资源', WORKFLOW: '流程', REPORT: '周报', USER: '用户' } as const

function searchTarget(item: AdminSearchApiItem): AdminSearchResult['to'] {
  if (item.type === 'RESOURCE') return `/resources/${item.id}`
  if (item.type === 'WORKFLOW') return { path: '/approvals', query: { workflow: item.id } }
  if (item.type === 'USER') return { path: '/users', query: { keyword: item.title } }
  return '/weekly-report'
}

export function toAdminSearchResults(items: AdminSearchApiItem[]): AdminSearchResult[] {
  return items.map((item) => ({
    id: `${item.type.toLowerCase()}-${item.id}`,
    type: labels[item.type],
    title: item.title,
    meta: [item.subtitle, item.status].filter(Boolean).join(' · '),
    to: searchTarget(item),
  }))
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
