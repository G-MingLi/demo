// 2026/10/29 gongml [需求 待补充] 按钮级权限指令
import type { Directive } from 'vue'
import { useAuthStore } from '@/stores/auth'

const perm: Directive<HTMLElement, string> = {
  mounted(el, binding) {
    const auth = useAuthStore()
    if (binding.value && !auth.hasPerm(binding.value)) {
      el.remove()
    }
  },
}

export default perm
