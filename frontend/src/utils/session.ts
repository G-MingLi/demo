// 2026/10/29 gongml [需求 待补充] 访问令牌与静默刷新
import axios from 'axios'
import type { ApiResponse, LoginResponse } from '@/types/api'

let accessToken = ''
let refreshPromise: Promise<LoginResponse> | null = null

export function setAccessToken(token: string): void {
  accessToken = token
}

export function getAccessToken(): string {
  return accessToken
}

export function clearSession(): void {
  accessToken = ''
}

export function refreshSession(): Promise<LoginResponse> {
  if (!refreshPromise) {
    refreshPromise = axios
      .post<ApiResponse<LoginResponse>>('/api/v1/auth/refresh', {}, { withCredentials: true })
      .then((response) => {
        if (response.data.code !== 0) {
          throw new Error(response.data.message)
        }
        setAccessToken(response.data.data.accessToken)
        return response.data.data
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}
