// 2026/10/29 gongml [需求 待补充] 用户接口
import { http } from '@/utils/request'
import type { PageResult, UserItem } from '@/types/api'

export function fetchUsers(params: { page: number; size: number }): Promise<PageResult<UserItem>> {
  return http<PageResult<UserItem>>({
    url: '/v1/users',
    method: 'get',
    params,
  })
}
