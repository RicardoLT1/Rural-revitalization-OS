import { http } from './http'
import type { ApiResponse } from '../types/auth'
import type { DashboardData, ResourceItem, WorkflowItem } from '../types/business'

export async function fetchDashboard(days: number) {
  const response = await http.get<ApiResponse<DashboardData>>('/dashboard', { params: { days } })
  return response.data.data
}

export async function fetchTodos() {
  const response = await http.get<ApiResponse<WorkflowItem[]>>('/workflows/todos')
  return response.data.data || []
}

export async function fetchApprovalHistory() {
  const response = await http.get<ApiResponse<WorkflowItem[]>>('/workflows/approvals')
  return response.data.data || []
}

export async function decideWorkflow(id: string | number, action: 'approve' | 'reject', remark: string) {
  const response = await http.post<ApiResponse<Record<string, unknown>>>(`/workflows/${id}/${action}`, { remark })
  return response.data.data
}

export interface ResourceFilters {
  keyword?: string
  category?: string
  investmentStatus?: string
}

export async function fetchResources(filters: ResourceFilters) {
  const response = await http.get<ApiResponse<ResourceItem[]>>('/resources', {
    params: { page: 1, size: 50, ...filters },
  })
  return response.data.data || []
}

export async function fetchResource(id: string) {
  const response = await http.get<ApiResponse<ResourceItem>>(`/resources/${id}`)
  return response.data.data
}

export async function publishResource(id: string) {
  await http.post(`/resources/${id}/publish`)
}

export async function offlineResource(id: string) {
  await http.post(`/resources/${id}/offline`)
}
