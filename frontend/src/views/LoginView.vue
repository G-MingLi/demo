<!-- 2026/10/29 gongml [需求 待补充] 登录页 -->
<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <div class="title">Demo 管理后台</div>
      <div class="subtitle">Vue 3 + Spring Boot 基础架构</div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>

      <div class="hint">初始账号：admin / Admin@123456</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'Admin@123456',
})
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit(): Promise<void> {
  if (!formRef.value) {
    return
  }
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.push(redirect)
  } catch {
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #3a6073 100%);
}

.login-card {
  width: 380px;
  padding: 8px 12px 16px;
}

.title {
  font-size: 22px;
  font-weight: 700;
  text-align: center;
}

.subtitle {
  margin: 8px 0 24px;
  color: #909399;
  font-size: 13px;
  text-align: center;
}

.submit {
  width: 100%;
  margin-top: 4px;
}

.hint {
  margin-top: 16px;
  color: #909399;
  font-size: 12px;
  text-align: center;
}
</style>
