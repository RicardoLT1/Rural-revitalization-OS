import { expect, type APIRequestContext, type Page } from '@playwright/test'

export const ACCOUNTS = {
  admin: { username: 'admin', password: '123456' },
  staff: { username: 'staff_demo', password: '123456' },
  user: { username: 'user_demo', password: '123456' },
} as const

export const ROTATED_ADMIN_PASSWORD = 'AdminE2E!2026'

export interface UserProfile {
  id: string
  username: string
  displayName: string
  role: 'USER' | 'STAFF' | 'ADMIN'
  villageId: string
  permissions: string[]
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  user: UserProfile
}

interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

export function bearer(token: string) {
  return { Authorization: `Bearer ${token}` }
}

async function envelope<T>(response: Awaited<ReturnType<APIRequestContext['fetch']>>, label: string) {
  const text = await response.text()
  let body: ApiEnvelope<T> | undefined
  try {
    body = JSON.parse(text) as ApiEnvelope<T>
  } catch {
    // The assertion below includes the raw body when a proxy or service returns HTML.
  }
  expect(response.ok(), `${label}: HTTP ${response.status()} ${text}`).toBe(true)
  expect(body?.code, `${label}: ${body?.message || text}`).toBe(200)
  return body!.data
}

export async function apiLogin(
  request: APIRequestContext,
  username: string,
  password: string,
): Promise<LoginResult> {
  const response = await request.post('/api/auth/login', { data: { username, password } })
  return envelope<LoginResult>(response, `登录 ${username}`)
}

async function tryApiLogin(
  request: APIRequestContext,
  username: string,
  password: string,
): Promise<LoginResult | null> {
  const response = await request.post('/api/auth/login', { data: { username, password } })
  if (!response.ok()) return null
  const body = await response.json() as ApiEnvelope<LoginResult>
  return body.code === 200 ? body.data : null
}

export async function apiData<T>(
  request: APIRequestContext,
  method: string,
  path: string,
  token: string,
  data?: unknown,
): Promise<T> {
  const response = await request.fetch(path, {
    method,
    headers: bearer(token),
    data,
  })
  return envelope<T>(response, `${method} ${path}`)
}

export async function clearBrowserSession(page: Page) {
  if (!page.url().startsWith('http')) await page.goto('/login')
  await page.evaluate(() => {
    localStorage.clear()
    sessionStorage.clear()
  })
  // Reload after clearing storage so the Pinia session store is reconstructed.
  // Without this, a page that was already authenticated keeps redirecting
  // /login back to /dashboard even though localStorage is empty.
  await page.reload()
}

export async function uiLogin(page: Page, username: string, password: string) {
  await clearBrowserSession(page)
  await page.goto('/login')
  await page.getByPlaceholder('请输入账号').fill(username)
  await page.getByPlaceholder('请输入密码').fill(password)
  await Promise.all([
    page.waitForURL((url) => url.pathname === '/dashboard'),
    page.getByRole('button', { name: '进入工作台' }).click(),
  ])
  await expect(page.getByRole('heading', { name: '运营看板', level: 1 })).toBeVisible()
}

export async function browserToken(page: Page) {
  const token = await page.evaluate(() => localStorage.getItem('xiangyun.admin.token'))
  expect(token, '浏览器登录后应保存访问令牌').toBeTruthy()
  return token!
}

export async function ensureDefaultAdminPassword(request: APIRequestContext) {
  const defaultLogin = await tryApiLogin(request, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
  if (defaultLogin) return defaultLogin

  const rotatedLogin = await tryApiLogin(request, ACCOUNTS.admin.username, ROTATED_ADMIN_PASSWORD)
  expect(rotatedLogin, 'admin 既不能使用默认密码，也不能使用 E2E 临时密码；需人工恢复测试账号').toBeTruthy()
  await apiData<Record<string, unknown>>(
    request,
    'POST',
    `/api/users/${rotatedLogin!.user.id}/password`,
    rotatedLogin!.token,
    { password: ACCOUNTS.admin.password },
  )
  return apiLogin(request, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
}
