// 2026/10/29 gongml [需求 待补充] 路由元信息类型
import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    title?: string
    perm?: string
  }
}
