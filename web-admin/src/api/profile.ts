import { http } from './http'
import type { ApiResponse, UserProfile } from '../types/auth'

export interface LoginRecord {
  id: number
  action: 'LOGIN_SUCCESS' | 'LOGOUT'
  result: string
  clientIp?: string
  userAgent?: string
  detail?: string
  createdAt: string
}

export async function updateMyProfile(displayName: string) {
  const response = await http.put<ApiResponse<UserProfile>>('/auth/me/profile', { displayName })
  return response.data.data
}

export async function changeMyPassword(currentPassword: string, newPassword: string) {
  await http.post('/auth/me/password', { currentPassword, newPassword })
}

export async function fetchLoginRecords(limit = 10) {
  const response = await http.get<ApiResponse<LoginRecord[]>>('/profile/login-records', { params: { limit } })
  return response.data.data
}
