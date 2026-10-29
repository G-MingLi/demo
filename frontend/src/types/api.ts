// 2026/10/29 gongml [需求 待补充] 接口数据类型
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  traceId: string
  timestamp: string
}

export interface LoginUser {
  id: number
  username: string
  nickname: string
  permissions: string[]
}

export interface LoginResponse {
  accessToken: string
  user: LoginUser
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

export interface UserItem {
  id: number
  username: string
  nickname: string
  status: number
  createdAt: string
}
