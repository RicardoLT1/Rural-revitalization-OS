import { http } from './http'
import type { ApiResponse } from '../types/auth'

export interface SystemSettings {
  villageId: string
  platformName: string
  villageName: string
  mapCenterLat: number
  mapCenterLng: number
  approvalTimeoutHours: number
  weeklyReportDay: number
  workflowNotificationEnabled: boolean
  riskNotificationEnabled: boolean
  contactPhone: string
  updatedBy?: string
  updatedAt?: string
  systemVersion: string
}

export async function fetchSystemSettings() {
  const response = await http.get<ApiResponse<SystemSettings>>('/system-settings')
  return response.data.data
}

export async function saveSystemSettings(settings: SystemSettings) {
  const response = await http.put<ApiResponse<SystemSettings>>('/system-settings', settings)
  return response.data.data
}
