import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '../api/auth'
import { TOKEN_KEY } from '../api/http'
import type { UserProfile } from '../types/auth'

const USER_KEY = 'xiangyun.admin.user'

function savedUser(): UserProfile | null {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

export const useSessionStore = defineStore('session', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<UserProfile | null>(savedUser())
  const authenticated = computed(() => Boolean(token.value && user.value))
  const canUseAdmin = computed(() => user.value?.role === 'STAFF' || user.value?.role === 'ADMIN')

  function persist() {
    if (token.value) localStorage.setItem(TOKEN_KEY, token.value)
    if (user.value) localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  async function signIn(username: string, password: string) {
    const result = await authApi.login(username, password)
    token.value = result.token
    user.value = result.user
    persist()
  }

  async function restore() {
    if (!token.value) return false
    try {
      user.value = await authApi.fetchProfile()
      persist()
      return true
    } catch {
      clear()
      return false
    }
  }

  function clear() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function signOut() {
    try {
      await authApi.logout()
    } finally {
      clear()
    }
  }

  return { token, user, authenticated, canUseAdmin, signIn, restore, signOut, clear }
})
