import { http } from './http'
import type { ApiResponse, PagedResult } from '../types/auth'
import type { AdminAuditLog, UserRow } from '../types/business'

export async function fetchUsers() {
  return (await fetchUserPage({ page: 1, pageSize: 100 })).items || []
}

export async function fetchUserPage(params: { page?: number; pageSize?: number; keyword?: string; role?: string; enabled?: boolean } = {}) {
  const response = await http.get<ApiResponse<PagedResult<UserRow>>>('/users', { params })
  return response.data.data
}

export async function fetchAuditLogs(params: { page?: number; pageSize?: number; keyword?: string; module?: string; result?: string } = {}) {
  const response = await http.get<ApiResponse<PagedResult<AdminAuditLog>>>('/audit-logs', { params })
  return response.data.data
}

export async function createUser(payload: { username: string; displayName: string; password: string; role: string; villageId: string }) {
  const response = await http.post<ApiResponse<UserRow>>('/users', payload)
  return response.data.data
}

export async function updateUser(id: string, payload: { displayName: string; role: string; villageId: string }) {
  const response = await http.put<ApiResponse<UserRow>>(`/users/${id}`, payload)
  return response.data.data
}

export async function setUserEnabled(id: string, enabled: boolean) {
  await http.post(`/users/${id}/${enabled ? 'enable' : 'disable'}`)
}

export async function resetUserPassword(id: string, password: string) {
  await http.post(`/users/${id}/password`, { password })
}
