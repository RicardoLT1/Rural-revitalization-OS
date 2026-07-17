<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { KeyRound, Pencil, Plus, Search, ShieldCheck, UserCheck, UserX, X } from '@lucide/vue'
import { createUser, fetchUserPage, resetUserPassword, setUserEnabled, updateUser } from '../api/admin'
import PagePager from '../components/PagePager.vue'
import PageState from '../components/PageState.vue'
import type { UserRow } from '../types/business'

const rows = ref<UserRow[]>([])
const route = useRoute()
const page = ref(1)
const pageSize = 10
const total = ref(0)
const totalPages = ref(0)
const keyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const role = ref('ALL')
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const notice = ref('')
const editing = ref<UserRow | null | 'new'>(null)
const form = reactive({ username: '', displayName: '', password: '123456', role: 'USER', villageId: '1' })

async function load() {
  loading.value = true; error.value = ''
  try {
    const result = await fetchUserPage({
      page: page.value,
      pageSize,
      keyword: keyword.value.trim() || undefined,
      role: role.value === 'ALL' ? undefined : role.value,
    })
    rows.value = result.items
    total.value = result.total
    totalPages.value = result.totalPages
  }
  catch (reason) { error.value = reason instanceof Error ? reason.message : '无法读取用户数据' }
  finally { loading.value = false }
}

function applyFilters() {
  page.value = 1
  load()
}

function changePage(nextPage: number) {
  page.value = nextPage
  load()
}

watch(() => route.query.keyword, (value) => {
  const next = typeof value === 'string' ? value : ''
  if (next === keyword.value) return
  keyword.value = next
  applyFilters()
})

function open(item?: UserRow) {
  editing.value = item || 'new'
  Object.assign(form, item ? { username: item.username, displayName: item.displayName, password: '', role: item.role, villageId: item.villageId } : { username: '', displayName: '', password: '123456', role: 'USER', villageId: '1' })
}

async function save() {
  saving.value = true; error.value = ''
  try {
    if (editing.value === 'new') await createUser({ ...form })
    else if (editing.value) await updateUser(editing.value.id, { displayName: form.displayName, role: form.role, villageId: form.villageId })
    notice.value = editing.value === 'new' ? '用户已创建' : '用户信息已更新'
    editing.value = null; await load()
  } catch (reason) { error.value = reason instanceof Error ? reason.message : '用户保存失败'; editing.value = null }
  finally { saving.value = false; window.setTimeout(() => { notice.value = '' }, 2600) }
}

async function toggle(item: UserRow) {
  if (!window.confirm(`确认${item.enabled ? '停用' : '启用'}账号“${item.username}”？`)) return
  try { await setUserEnabled(item.id, !item.enabled); notice.value = item.enabled ? '账号已停用' : '账号已启用'; await load() }
  catch (reason) { error.value = reason instanceof Error ? reason.message : '状态更新失败' }
}

async function resetPassword(item: UserRow) {
  const password = window.prompt(`为 ${item.username} 设置新密码（至少 6 位）`, '123456')
  if (!password) return
  try { await resetUserPassword(item.id, password); notice.value = '密码已重置，原会话已失效' }
  catch (reason) { error.value = reason instanceof Error ? reason.message : '密码重置失败' }
}
onMounted(load)
</script>

<template>
  <div class="business-page">
    <section class="page-intro"><div><p>账号与访问边界</p><h2>用户与权限</h2></div><div class="resource-heading-actions"><div class="count-summary"><strong>{{ total }}</strong><span>个管理账号</span></div><button class="primary-button compact-primary" type="button" @click="open()"><Plus :size="17" />新增用户</button></div></section>
    <div v-if="notice" class="toast-notice"><ShieldCheck :size="16" />{{ notice }}</div>
    <section class="business-toolbar"><div class="filter-group"><label><Search :size="16" /><input v-model="keyword" placeholder="搜索账号或姓名" @keyup.enter="applyFilters" /></label><select v-model="role" @change="applyFilters"><option value="ALL">全部角色</option><option value="USER">普通用户</option><option value="STAFF">工作人员</option><option value="ADMIN">管理员</option></select><button class="secondary-button query-button" type="button" @click="applyFilters">查询</button></div></section>
    <PageState :loading="loading" :error="error" :empty="!rows.length" empty-text="没有符合条件的用户" @retry="load"><section class="table-panel"><div class="table-scroll"><table><thead><tr><th>账号</th><th>姓名</th><th>角色</th><th>状态</th><th>村域</th><th>操作</th></tr></thead><tbody><tr v-for="item in rows" :key="item.id"><td><strong>{{ item.username }}</strong><small>ID {{ item.id }}</small></td><td>{{ item.displayName }}</td><td><span class="role-badge">{{ item.role }}</span></td><td><span class="account-state" :class="{ disabled: !item.enabled }"><i />{{ item.enabled ? '启用' : '停用' }}</span></td><td>{{ item.villageId }}</td><td><div class="row-buttons"><button class="table-action" type="button" @click="open(item)"><Pencil :size="14" />编辑</button><button class="table-action" type="button" @click="resetPassword(item)"><KeyRound :size="14" />重置密码</button><button class="table-action" :class="item.enabled ? 'reject' : 'approve'" type="button" @click="toggle(item)"><UserX v-if="item.enabled" :size="14" /><UserCheck v-else :size="14" />{{ item.enabled ? '停用' : '启用' }}</button></div></td></tr></tbody></table></div><PagePager :page="page" :page-size="pageSize" :total="total" :total-pages="totalPages" @change="changePage" /></section></PageState>
    <div v-if="editing" class="dialog-layer" @click.self="editing = null"><form class="entity-dialog" @submit.prevent="save"><header><div><span>{{ editing === 'new' ? '创建账号' : '维护账号' }}</span><h3>{{ editing === 'new' ? '新增用户' : form.username }}</h3></div><button class="icon-button" type="button" title="关闭" @click="editing = null"><X :size="18" /></button></header><div class="entity-form"><label><span>登录账号</span><input v-model="form.username" :disabled="editing !== 'new'" minlength="3" required /></label><label><span>姓名</span><input v-model="form.displayName" required /></label><label v-if="editing === 'new'"><span>初始密码</span><input v-model="form.password" type="password" minlength="6" required /></label><div class="form-grid two"><label><span>角色</span><select v-model="form.role"><option value="USER">普通用户</option><option value="STAFF">工作人员</option><option value="ADMIN">管理员</option></select></label><label><span>村域编号</span><input v-model="form.villageId" required /></label></div></div><footer><button class="secondary-button" type="button" @click="editing = null">取消</button><button class="primary-button dialog-submit" type="submit" :disabled="saving">{{ saving ? '正在保存...' : '保存用户' }}</button></footer></form></div>
  </div>
</template>
