<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check, Clock3, KeyRound, LockKeyhole, Save, ShieldCheck, UserRound } from '@lucide/vue'
import { changeMyPassword, fetchLoginRecords, updateMyProfile, type LoginRecord } from '../api/profile'
import { fetchSystemSettings } from '../api/settings'
import { useSessionStore } from '../stores/session'

const router = useRouter()
const session = useSessionStore()
const displayName = ref(session.user?.displayName || '')
const records = ref<LoginRecord[]>([])
const recordsLoading = ref(true)
const recordsError = ref('')
const profileSaving = ref(false)
const passwordSaving = ref(false)
const profileError = ref('')
const passwordError = ref('')
const notice = ref('')
const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const villageName = ref('当前村域')

const roleLabel = computed(() => session.user?.role === 'ADMIN' ? '系统管理员' : '业务工作人员')
const permissionLabels: Record<string, string> = {
  'user:manage': '用户管理', 'role:manage': '角色管理', 'resource:write': '资源管理',
  'resource:read': '资源查看', 'workflow:approve': '审批处理', 'workflow:read': '流程查看',
  'report:read': '周报管理', 'system:read': '系统治理',
}

function formatTime(value: string) { return new Date(value).toLocaleString('zh-CN', { hour12: false }) }
function device(value?: string) {
  if (!value) return '未知设备'
  if (/Windows/i.test(value)) return 'Windows 管理终端'
  if (/Macintosh|Mac OS/i.test(value)) return 'macOS 管理终端'
  if (/Android|iPhone/i.test(value)) return '移动设备'
  return '管理终端'
}

async function loadRecords() {
  recordsLoading.value = true
  recordsError.value = ''
  try { records.value = await fetchLoginRecords(12) }
  catch (reason) { recordsError.value = reason instanceof Error ? reason.message : '登录记录读取失败' }
  finally { recordsLoading.value = false }
}

async function loadVillageContext() {
  try { villageName.value = (await fetchSystemSettings()).villageName }
  catch { /* 个人中心仍可在运营上下文暂不可用时独立工作。 */ }
}

async function saveProfile() {
  profileError.value = ''
  if (displayName.value.trim().length < 2) { profileError.value = '姓名至少填写 2 个字符'; return }
  profileSaving.value = true
  try {
    await updateMyProfile(displayName.value.trim())
    await session.restore()
    displayName.value = session.user?.displayName || displayName.value
    notice.value = '个人信息已更新'
    window.setTimeout(() => { notice.value = '' }, 2600)
  } catch (reason) { profileError.value = reason instanceof Error ? reason.message : '个人信息更新失败' }
  finally { profileSaving.value = false }
}

async function savePassword() {
  passwordError.value = ''
  if (newPassword.value.length < 8) { passwordError.value = '新密码至少 8 位'; return }
  if (newPassword.value !== confirmPassword.value) { passwordError.value = '两次输入的新密码不一致'; return }
  passwordSaving.value = true
  try {
    await changeMyPassword(currentPassword.value, newPassword.value)
    notice.value = '密码已更新，正在返回登录页'
    window.setTimeout(() => { session.clear(); void router.replace('/login') }, 900)
  } catch (reason) { passwordError.value = reason instanceof Error ? reason.message : '密码更新失败' }
  finally { passwordSaving.value = false }
}

onMounted(() => { void loadRecords(); void loadVillageContext() })
</script>

<template>
  <div class="business-page profile-page">
    <div v-if="notice" class="toast-notice"><Check :size="16" />{{ notice }}</div>
    <section class="page-intro"><div><p>账号身份 · 安全记录</p><h2>个人中心</h2></div><span class="profile-session"><ShieldCheck :size="16" />当前会话已通过网关身份校验</span></section>
    <section class="profile-identity-card">
      <div class="profile-avatar">{{ session.user?.displayName?.slice(0, 1) }}</div>
      <div><span>{{ roleLabel }}</span><h3>{{ session.user?.displayName }}</h3><p>@{{ session.user?.username }} · {{ villageName }} · 村域 {{ session.user?.villageId }}</p></div>
      <dl><div><dt>账号状态</dt><dd>正常</dd></div><div><dt>权限数量</dt><dd>{{ session.user?.permissions.length || 0 }}</dd></div><div><dt>版本节点</dt><dd>Admin Pro</dd></div></dl>
    </section>

    <div class="profile-grid">
      <main class="profile-main-stack">
        <section class="profile-panel">
          <header><UserRound :size="19" /><div><span>基本资料</span><h3>管理端显示身份</h3></div></header>
          <div class="profile-form"><label><span>账号</span><input :value="session.user?.username" disabled /><small>账号不可自行修改</small></label><label><span>显示姓名</span><input v-model.trim="displayName" maxlength="32" /><small>用于审批记录、审计日志和操作留痕</small></label><label><span>当前角色</span><input :value="roleLabel" disabled /></label><label><span>所属村域</span><input :value="`${villageName}（${session.user?.villageId}）`" disabled /></label></div>
          <footer><span v-if="profileError">{{ profileError }}</span><button class="secondary-button" type="button" :disabled="profileSaving" @click="saveProfile"><Save :size="15" />{{ profileSaving ? '正在保存...' : '保存个人信息' }}</button></footer>
        </section>

        <section class="profile-panel password-panel">
          <header><LockKeyhole :size="19" /><div><span>账号安全</span><h3>修改登录密码</h3></div></header>
          <p><KeyRound :size="15" />密码修改成功后，当前账号的全部登录会话都会失效，需要重新登录。</p>
          <div class="profile-form password-form"><label><span>当前密码</span><input v-model="currentPassword" type="password" autocomplete="current-password" /></label><label><span>新密码</span><input v-model="newPassword" type="password" autocomplete="new-password" placeholder="至少 8 位" /></label><label><span>确认新密码</span><input v-model="confirmPassword" type="password" autocomplete="new-password" /></label></div>
          <footer><span v-if="passwordError">{{ passwordError }}</span><button class="primary-button" type="button" :disabled="passwordSaving" @click="savePassword"><LockKeyhole :size="15" />{{ passwordSaving ? '正在更新...' : '更新密码' }}</button></footer>
        </section>
      </main>

      <aside class="profile-side-stack">
        <section class="profile-permission-card"><span>当前权限</span><h3>您的管理边界</h3><div><b v-for="permission in session.user?.permissions" :key="permission">{{ permissionLabels[permission] || permission }}</b></div><p>界面只负责呈现，所有管理权限仍由网关和后端接口强制校验。</p></section>
        <section class="login-record-card">
          <header><Clock3 :size="18" /><div><span>安全轨迹</span><h3>最近登录记录</h3></div></header>
          <div v-if="recordsLoading" class="compact-empty">正在读取登录记录...</div><div v-else-if="recordsError" class="compact-empty">{{ recordsError }} <button type="button" @click="loadRecords">重试</button></div><div v-else-if="records.length" class="login-record-list"><article v-for="item in records" :key="item.id"><i /><div><strong>{{ item.action === 'LOGIN_SUCCESS' ? '登录成功' : '退出登录' }}</strong><span>{{ device(item.userAgent) }} · {{ item.clientIp || '未知 IP' }}</span><small>{{ formatTime(item.createdAt) }}</small></div></article></div><div v-else class="compact-empty">当前账号还没有可展示的登录记录</div>
        </section>
      </aside>
    </div>
  </div>
</template>
