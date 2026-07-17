import { http } from './http'
import type { ApiResponse, LoginResult, UserProfile } from '../types/auth'

export async function login(username: string, password: string) {
  const response = await http.post<ApiResponse<LoginResult>>('/auth/login', { username, password })
  return response.data.data
}

export async function fetchProfile() {
  const response = await http.get<ApiResponse<UserProfile>>('/auth/me')
  return response.data.data
}

export async function logout(token?: string) {
  await http.post('/auth/logout', undefined, {
    timeout: 2500,
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  })
}
