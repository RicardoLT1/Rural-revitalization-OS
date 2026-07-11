import axios from 'axios'
import type { ApiResponse } from '../types/auth'

const TOKEN_KEY = 'xiangyun.admin.token'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 12_000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    if (body && typeof body.code === 'number' && body.code !== 200) {
      return Promise.reject(new Error(body.message || '请求未完成'))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('xiangyun.admin.user')
      if (location.pathname !== '/login') location.assign('/login')
    }
    const message = error.response?.data?.message || error.message || '服务暂时不可用'
    return Promise.reject(new Error(message))
  },
)

export { TOKEN_KEY }
