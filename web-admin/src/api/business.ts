import { http } from './http'
import type { ApiResponse, PagedResult } from '../types/auth'
import type { DashboardData, ResourceActivity, ResourceItem, WeeklyReport, WorkflowDetail, WorkflowItem, WorkflowOperationLog } from '../types/business'

export async function fetchDashboard(days: number) {
  const response = await http.get<ApiResponse<DashboardData>>('/dashboard/admin-overview', { params: { days } })
  return response.data.data
}

export async function fetchTodoPage(params: { page?: number; pageSize?: number; keyword?: string; status?: string } = {}) {
  const response = await http.get<ApiResponse<PagedResult<WorkflowItem>>>('/workflows/todos', { params })
  return response.data.data
}

export async function fetchTodos() {
  return (await fetchTodoPage({ page: 1, pageSize: 100 })).items || []
}

export async function fetchApprovalPage(params: { page?: number; pageSize?: number; keyword?: string; status?: string } = {}) {
  const response = await http.get<ApiResponse<PagedResult<WorkflowItem>>>('/workflows/approvals', { params })
  return response.data.data
}

export async function fetchApprovalHistory() {
  return (await fetchApprovalPage({ page: 1, pageSize: 100 })).items || []
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

export interface ResourcePageFilters extends ResourceFilters {
  page?: number
  pageSize?: number
}

export async function fetchResourcePage(filters: ResourcePageFilters = {}) {
  const response = await http.get<ApiResponse<PagedResult<ResourceItem>>>('/resources', {
    params: filters,
  })
  return response.data.data
}

export async function fetchResources(filters: ResourceFilters) {
  return (await fetchResourcePage({ ...filters, page: 1, pageSize: 100 })).items || []
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
  const response = await http.get<ApiResponse<PagedResult<WeeklyReport>>>('/operation/reports/weekly', {
    params: { page: 1, pageSize: 100 },
  })
  return response.data.data?.items || []
}

export type WeeklyReportPayload = Pick<WeeklyReport, 'weekStart' | 'weekEnd' | 'title' | 'summary' | 'highlights' | 'risks' | 'nextWeekPlan'>

export async function createWeeklyReport(payload: WeeklyReportPayload) {
  const response = await http.post<ApiResponse<{ id: string; status: string }>>('/operation/reports/weekly', payload)
  return response.data.data
}
