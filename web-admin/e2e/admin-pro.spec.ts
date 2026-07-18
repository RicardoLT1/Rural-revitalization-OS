import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import {
  ACCOUNTS,
  ROTATED_ADMIN_PASSWORD,
  apiData,
  apiLogin,
  bearer,
  browserToken,
  clearBrowserSession,
  ensureDefaultAdminPassword,
  uiLogin,
} from './helpers'

interface SystemSettings {
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
  systemVersion: string
}

interface NotificationItem {
  id: string
  title: string
  read: boolean
}

interface NotificationCenter {
  items: NotificationItem[]
  unreadCount: number
  total: number
}

interface AuditItem {
  action: string
}

interface AuditPage {
  items: AuditItem[]
}

function settingsPayload(settings: SystemSettings): SystemSettings {
  return {
    villageId: settings.villageId,
    platformName: settings.platformName,
    villageName: settings.villageName,
    mapCenterLat: settings.mapCenterLat,
    mapCenterLng: settings.mapCenterLng,
    approvalTimeoutHours: settings.approvalTimeoutHours,
    weeklyReportDay: settings.weeklyReportDay,
    workflowNotificationEnabled: settings.workflowNotificationEnabled,
    riskNotificationEnabled: settings.riskNotificationEnabled,
    contactPhone: settings.contactPhone,
    systemVersion: settings.systemVersion,
  }
}

async function freshAdmin(request: APIRequestContext) {
  return apiLogin(request, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
}

async function expectAuditAction(
  page: Page,
  request: APIRequestContext,
  token: string,
  action: string,
) {
  await expect.poll(async () => {
    const data = await apiData<AuditPage>(
      request,
      'GET',
      `/api/audit-logs?page=1&pageSize=50&keyword=${encodeURIComponent(action)}`,
      token,
    )
    return data.items.some((item) => item.action === action)
  }, { message: `审计中心应出现 ${action}` }).toBe(true)

  await page.goto('/audit-logs')
  const search = page.getByPlaceholder('搜索操作人、对象或 Trace ID')
  await search.fill(action)
  await search.press('Enter')
  await expect(page.locator('.audit-table-panel tbody')).toContainText(action)
}

test.describe('V1.3 Admin Pro 发布门', () => {
  test.describe.configure({ mode: 'serial' })

  test.beforeAll(async ({ request }) => {
    await ensureDefaultAdminPassword(request)
  })

  test.afterAll(async ({ request }) => {
    // Password cleanup is deliberately repeated at suite level so an assertion failure
    // cannot strand the shared demo admin account on the temporary E2E password.
    await ensureDefaultAdminPassword(request)
  })

  test('管理员登录、设置保存、地图中心联动并回滚', async ({ page, request }) => {
    await uiLogin(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
    const token = await browserToken(page)
    const original = await apiData<SystemSettings>(request, 'GET', '/api/system-settings', token)
    const changed = settingsPayload(original)
    changed.mapCenterLat = Number((original.mapCenterLat + 0.000321).toFixed(6))
    changed.mapCenterLng = Number((original.mapCenterLng + 0.000321).toFixed(6))

    try {
      await page.goto('/settings')
      await expect(page.getByRole('heading', { name: '系统设置', level: 2 })).toBeVisible()
      await page.locator('label').filter({ hasText: '中心纬度' }).locator('input').fill(String(changed.mapCenterLat))
      await page.locator('label').filter({ hasText: '中心经度' }).locator('input').fill(String(changed.mapCenterLng))

      const saved = page.waitForResponse((response) =>
        response.url().includes('/api/system-settings') && response.request().method() === 'PUT')
      await page.getByRole('button', { name: '保存并立即生效' }).click()
      expect((await saved).status()).toBe(200)
      await expect(page.getByText('系统设置已保存，村域名称与通知策略已同步生效')).toBeVisible()

      const persisted = await apiData<SystemSettings>(request, 'GET', '/api/system-settings', token)
      expect(persisted.mapCenterLat).toBe(changed.mapCenterLat)
      expect(persisted.mapCenterLng).toBe(changed.mapCenterLng)

      await page.goto('/resource-map')
      await expect(page.getByRole('heading', { name: '资源地图', level: 2 })).toBeVisible()
      await expect(page.getByTitle('回到村域中心')).toBeVisible()
    } finally {
      const cleanup = await freshAdmin(request)
      await apiData<SystemSettings>(
        request,
        'PUT',
        '/api/system-settings',
        cleanup.token,
        settingsPayload(original),
      )
      const restored = await apiData<SystemSettings>(request, 'GET', '/api/system-settings', cleanup.token)
      expect(restored.mapCenterLat).toBe(original.mapCenterLat)
      expect(restored.mapCenterLng).toBe(original.mapCenterLng)
    }
  })

  test('个人资料通过界面保存并在 finally 中恢复', async ({ page, request }) => {
    await uiLogin(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
    const token = await browserToken(page)
    const original = await apiData<{ displayName: string }>(request, 'GET', '/api/auth/me', token)
    const temporaryName = `E2E管理员${Date.now().toString().slice(-6)}`

    try {
      await page.goto('/profile')
      await expect(page.getByRole('heading', { name: '个人中心', level: 2 })).toBeVisible()
      await page.getByLabel('显示姓名').fill(temporaryName)
      const saved = page.waitForResponse((response) =>
        response.url().includes('/api/auth/me/profile') && response.request().method() === 'PUT')
      await page.getByRole('button', { name: '保存个人信息' }).click()
      expect((await saved).status()).toBe(200)
      await expect(page.getByText('个人信息已更新')).toBeVisible()

      const persisted = await apiData<{ displayName: string }>(request, 'GET', '/api/auth/me', token)
      expect(persisted.displayName).toBe(temporaryName)
    } finally {
      const cleanup = await freshAdmin(request)
      await apiData(request, 'PUT', '/api/auth/me/profile', cleanup.token, { displayName: original.displayName })
      const restored = await apiData<{ displayName: string }>(request, 'GET', '/api/auth/me', cleanup.token)
      expect(restored.displayName).toBe(original.displayName)
    }
  })

  test('STAFF 与 USER 的路由和管理接口边界为 403', async ({ page, request }) => {
    const staff = await apiLogin(request, ACCOUNTS.staff.username, ACCOUNTS.staff.password)
    const user = await apiLogin(request, ACCOUNTS.user.username, ACCOUNTS.user.password)

    const staffUsers = await request.get('/api/users', { headers: bearer(staff.token) })
    expect(staffUsers.status()).toBe(403)
    const staffSettingsWrite = await request.put('/api/system-settings', {
      headers: bearer(staff.token),
      data: {},
    })
    expect(staffSettingsWrite.status()).toBe(403)

    const userUsers = await request.get('/api/users', { headers: bearer(user.token) })
    expect(userUsers.status()).toBe(403)
    const userSettings = await request.get('/api/system-settings', { headers: bearer(user.token) })
    expect(userSettings.status()).toBe(403)

    await uiLogin(page, ACCOUNTS.staff.username, ACCOUNTS.staff.password)
    await page.goto('/settings')
    await expect(page).toHaveURL(/\/dashboard$/)
    await expect(page.getByRole('heading', { name: '系统设置' })).toHaveCount(0)

    await clearBrowserSession(page)
    await page.goto('/login')
    await page.getByPlaceholder('请输入账号').fill(ACCOUNTS.user.username)
    await page.getByPlaceholder('请输入密码').fill(ACCOUNTS.user.password)
    await page.getByRole('button', { name: '进入工作台' }).click()
    await expect(page).toHaveURL(/\/login$/)
    await expect(page.getByRole('alert')).toContainText('当前账号没有管理端访问权限')
  })

  test('通知已读状态跨退出和重新登录保留', async ({ page, request }) => {
    await uiLogin(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
    const firstToken = await browserToken(page)
    const center = await apiData<NotificationCenter>(request, 'GET', '/api/notifications?limit=50', firstToken)
    expect(center.total, '演示环境应至少有一条真实审批或超时通知').toBeGreaterThan(0)
    const item = center.items.find((candidate) => candidate.read) || center.items[0]
    expect(item).toBeTruthy()

    await apiData(request, 'POST', `/api/notifications/${item!.id}/read`, firstToken)
    await page.goto('/notifications')
    const beforeRelogin = page.locator('.notification-ledger > button').filter({ hasText: item!.title }).first()
    await expect(beforeRelogin).toHaveClass(/read/)

    const logout = await request.post('/api/auth/logout', { headers: bearer(firstToken) })
    expect(logout.status()).toBe(200)
    await uiLogin(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
    await page.goto('/notifications')
    const afterRelogin = page.locator('.notification-ledger > button').filter({ hasText: item!.title }).first()
    await expect(afterRelogin).toHaveClass(/read/)
  })

  test('修改密码使旧 token 失效，并恢复 admin 默认密码', async ({ page, request }) => {
    await ensureDefaultAdminPassword(request)
    try {
      await uiLogin(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
      const oldToken = await browserToken(page)
      await page.goto('/profile')
      await page.getByLabel('当前密码').fill(ACCOUNTS.admin.password)
      await page.getByLabel('新密码', { exact: true }).fill(ROTATED_ADMIN_PASSWORD)
      await page.getByLabel('确认新密码').fill(ROTATED_ADMIN_PASSWORD)

      const changed = page.waitForResponse((response) =>
        response.url().includes('/api/auth/me/password') && response.request().method() === 'POST')
      await page.getByRole('button', { name: '更新密码' }).click()
      expect((await changed).status()).toBe(200)
      await expect(page.getByText('密码已更新，正在返回登录页')).toBeVisible()

      const staleSession = await request.get('/api/auth/me', { headers: bearer(oldToken) })
      expect(staleSession.status()).toBe(401)
      await expect(page).toHaveURL(/\/login$/, { timeout: 5_000 })

      await uiLogin(page, ACCOUNTS.admin.username, ROTATED_ADMIN_PASSWORD)
      const rotatedToken = await browserToken(page)
      const me = await apiData<{ id: string }>(request, 'GET', '/api/auth/me', rotatedToken)
      await apiData(
        request,
        'POST',
        `/api/users/${me.id}/password`,
        rotatedToken,
        { password: ACCOUNTS.admin.password },
      )
      await apiLogin(request, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
    } finally {
      await ensureDefaultAdminPassword(request)
    }
  })

  test('审计中心可检索设置和个人资料操作', async ({ page, request }) => {
    await uiLogin(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password)
    const token = await browserToken(page)
    await expectAuditAction(page, request, token, 'UPDATE_SYSTEM_SETTINGS')
    await expectAuditAction(page, request, token, 'UPDATE_MY_PROFILE')
  })
})
