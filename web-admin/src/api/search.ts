import { http } from './http'
import type { ApiResponse } from '../types/auth'

export type AdminSearchEntityType = 'RESOURCE' | 'WORKFLOW' | 'REPORT' | 'USER'

export interface AdminSearchApiItem {
  id: string
  type: AdminSearchEntityType
  title: string
  subtitle: string
  status?: string
  updatedAt?: string
}

export interface AdminSearchApiResponse {
  query: string
  items: AdminSearchApiItem[]
  counts: Partial<Record<AdminSearchEntityType, number>>
  partial: boolean
}

export async function fetchGlobalSearch(query: string, type: AdminSearchEntityType | 'ALL' = 'ALL', limit = 10) {
  const response = await http.get<ApiResponse<AdminSearchApiResponse>>('/search', {
    params: { q: query, type, limit },
  })
  return response.data.data
}
