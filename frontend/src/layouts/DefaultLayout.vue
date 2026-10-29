<!-- 2026/10/29 gongml [需求 待补充] 后台主布局 -->
<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">Demo Admin</div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item v-if="auth.hasPerm('user:list')" index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="page-title">{{ route.meta.title }}</div>
        <el-dropdown @command="handleCommand">
          <span class="user">
            {{ auth.user?.nickname || auth.user?.username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, House, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

async function handleCommand(command: string): Promise<void> {
  if (command === 'logout') {
    await auth.logout()
    await router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  background: #1f2d3d;
  color: #fff;
}

.brand {
  height: 60px;
  line-height: 60px;
  padding-left: 20px;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.menu {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: #c0c4cc;
}

.menu :deep(.el-menu-item.is-active),
.menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: #263445;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
}

.user {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #303133;
}

.main {
  background: #f5f7fa;
}
</style>
