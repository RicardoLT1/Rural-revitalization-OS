import { http } from './http'
import type { ApiResponse } from '../types/auth'
import type { DashboardData, ResourceActivity, ResourceItem, WeeklyReport, WorkflowDetail, WorkflowItem, WorkflowOperationLog } from '../types/business'

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

export async function fetchWorkflow(id: string | number) {
  const response = await http.get<ApiResponse<WorkflowDetail>>(`/workflows/${id}`)
  return response.data.data
}

export async function fetchWorkflowOperationLogs(id: string | number) {
  const response = await http.get<ApiResponse<WorkflowOperationLog[]>>(`/workflows/${id}/operation-logs`)
  return response.data.data || []
}

export async function decideWorkflow(id: string | number, action: 'approve' | 'reject', remark: string) {
  const response = await http.post<ApiResponse<Record<string, unknown>>>(`/workflows/${id}/${action}`, { remark })
  return response.data.data
}

export async function requestWorkflowMaterials(id: string | number, remark: string) {
  const response = await http.post<ApiResponse<Record<string, unknown>>>(`/workflows/processes/${id}/actions`, {
    action: 'material_required',
    remark,
  })
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

export async function fetchResourceApplicationCount(id: string) {
  const response = await http.get<ApiResponse<{ resourceId: string; applicationCount: number }>>(`/resources/${id}/applications/count`)
  return response.data.data || { resourceId: id, applicationCount: 0 }
}

export async function fetchResourceActivity(id: string) {
  const response = await http.get<ApiResponse<ResourceActivity>>(`/resources/${id}/activity`)
  return response.data.data
}

export async function fetchResourceMapPoints(category?: string) {
  const response = await http.get<ApiResponse<ResourceItem[]>>('/resources/map-points', { params: { category } })
  return response.data.data || []
}

export async function publishResource(id: string) {
  await http.post(`/resources/${id}/publish`)
}

export async function offlineResource(id: string) {
  await http.post(`/resources/${id}/offline`)
}

export type ResourcePayload = Pick<ResourceItem, 'name' | 'category' | 'address' | 'area' | 'annualEstimate' | 'investmentStatus' | 'intro' | 'owner' | 'contact' | 'ownershipStatus' | 'materialStatus' | 'investmentNote'>

export async function createResource(payload: ResourcePayload) {
  const response = await http.post<ApiResponse<{ id: string }>>('/resources', payload)
  return response.data.data
}

export async function updateResource(id: string, payload: ResourcePayload) {
  await http.put(`/resources/${id}`, payload)
}

export async function fetchWeeklyReports() {
  const response = await http.get<ApiResponse<WeeklyReport[]>>('/operation/reports/weekly')
  return response.data.data || []
}

export type WeeklyReportPayload = Pick<WeeklyReport, 'weekStart' | 'weekEnd' | 'title' | 'summary' | 'highlights' | 'risks' | 'nextWeekPlan'>

export async function createWeeklyReport(payload: WeeklyReportPayload) {
  const response = await http.post<ApiResponse<{ id: string; status: string }>>('/operation/reports/weekly', payload)
  return response.data.data
}
