// 2026/10/29 gongml [需求 待补充] Axios 请求封装与 401 重试
import axios, { type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'
import { clearSession, getAccessToken, refreshSession } from './session'

interface RetryConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  withCredentials: true,
})

service.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['X-Request-Id'] = crypto.randomUUID()
  return config
})

service.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error.config as RetryConfig | undefined
    if (error.response?.status === 401 && config && !config._retry && !config.url?.includes('/auth/')) {
      config._retry = true
      try {
        const session = await refreshSession()
        config.headers.Authorization = `Bearer ${session.accessToken}`
        return service(config)
      } catch {
        clearSession()
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(error)
  },
)

export async function http<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await service.request<ApiResponse<T>>(config)
  const body = response.data
  if (body.code !== 0) {
    ElMessage.error(body.message || '请求失败')
    throw new Error(body.message)
  }
  return body.data
}

export default service
