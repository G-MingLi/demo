// 2026/10/29 gongml [需求 待补充] 登录状态管理
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { http } from '@/utils/request'
import { clearSession, refreshSession, setAccessToken } from '@/utils/session'
import type { LoginResponse, LoginUser } from '@/types/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginUser | null>(null)
  const accessToken = ref('')
  const isLoggedIn = computed(() => Boolean(accessToken.value))

  function applySession(session: LoginResponse): void {
    accessToken.value = session.accessToken
    user.value = session.user
    setAccessToken(session.accessToken)
  }

  async function login(username: string, password: string): Promise<void> {
    const session = await http<LoginResponse>({
      url: '/v1/auth/login',
      method: 'post',
      data: { username, password },
    })
    applySession(session)
  }

  async function ensureSession(): Promise<boolean> {
    if (accessToken.value && user.value) {
      return true
    }
    try {
      const session = await refreshSession()
      applySession(session)
      return true
    } catch {
      clear()
      return false
    }
  }

  async function logout(): Promise<void> {
    try {
      await http<void>({ url: '/v1/auth/logout', method: 'post' })
    } catch {
      // 登出失败也要清理本地状态
    } finally {
      clear()
    }
  }

  function clear(): void {
    accessToken.value = ''
    user.value = null
    clearSession()
  }

  function hasPerm(perm: string): boolean {
    return Boolean(user.value?.permissions.includes(perm))
  }

  return {
    user,
    accessToken,
    isLoggedIn,
    login,
    ensureSession,
    logout,
    clear,
    hasPerm,
  }
})
