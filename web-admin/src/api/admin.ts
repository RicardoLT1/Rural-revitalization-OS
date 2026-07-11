import { http } from './http'
import type { ApiResponse } from '../types/auth'
import type { UserRow } from '../types/business'

export async function fetchUsers() {
  const response = await http.get<ApiResponse<UserRow[]>>('/users')
  return response.data.data || []
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
