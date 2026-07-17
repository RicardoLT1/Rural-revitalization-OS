import { http } from './http'
import type { ApiResponse } from '../types/auth'

export interface AdminNotification {
  id: string
  type: 'WORKFLOW_PENDING' | 'WORKFLOW_OVERDUE' | string
  title: string
  content: string
  targetPath?: string
  read: boolean
  createdAt: string
}

export interface NotificationCenter {
  items: AdminNotification[]
  unreadCount: number
  total: number
}

export async function fetchNotifications(unreadOnly = false, limit = 20) {
  const response = await http.get<ApiResponse<NotificationCenter>>('/notifications', {
    params: { unreadOnly, limit },
  })
  return response.data.data
}

export async function readNotification(id: string) {
  await http.post(`/notifications/${id}/read`)
}

export async function readAllNotifications() {
  await http.post('/notifications/read-all')
}
