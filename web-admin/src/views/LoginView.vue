<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowRight, Eye, EyeOff, LockKeyhole, UserRound } from '@lucide/vue'
import { useSessionStore } from '../stores/session'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const username = ref('staff_demo')
const password = ref('123456')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

async function submit() {
  if (!username.value.trim() || !password.value) {
    error.value = '请输入账号和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await session.signIn(username.value.trim(), password.value)
    if (!session.canUseAdmin) {
      session.clear()
      error.value = '当前账号没有管理端访问权限'
      return
    }
    await router.replace(String(route.query.redirect || '/dashboard'))
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '登录失败，请检查账号信息'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-context">
      <div class="login-brand"><span class="brand-seal large">乡</span><div><strong>乡耘 OS</strong><span>运营协同中枢</span></div></div>
      <div class="context-copy"><p>XIANGYUN OPERATIONS</p><h1>让每一项乡村资源<br />沿着清晰流程生长</h1><span>连接资源归档、合作审批与经营数据，为一线运营人员提供安静、可靠的工作界面。</span></div>
      <div class="field-map" aria-hidden="true"><i v-for="n in 24" :key="n" /></div>
      <footer><span>资源可追溯</span><span>流程有依据</span><span>数据可复核</span></footer>
    </section>
    <section class="login-form-side">
      <form class="login-form" @submit.prevent="submit">
        <div class="form-heading"><span>工作人员入口</span><h2>登录综合工作台</h2><p>使用 STAFF 或 ADMIN 账号继续</p></div>
        <p v-if="error" class="form-error" role="alert">{{ error }}</p>
        <label><span>账号</span><div class="input-control"><UserRound :size="18" /><input v-model="username" autocomplete="username" placeholder="请输入账号" /></div></label>
        <label><span>密码</span><div class="input-control"><LockKeyhole :size="18" /><input v-model="password" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" placeholder="请输入密码" /><button type="button" :title="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword"><EyeOff v-if="showPassword" :size="18" /><Eye v-else :size="18" /></button></div></label>
        <button class="primary-button" type="submit" :disabled="loading"><span>{{ loading ? '正在验证...' : '进入工作台' }}</span><ArrowRight :size="18" /></button>
        <p class="security-note">会话通过网关验证，业务服务不接受直连访问</p>
      </form>
    </section>
  </main>
</template>
