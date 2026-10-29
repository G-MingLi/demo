<!-- 2026/10/29 gongml [需求 待补充] 首页 -->
<template>
  <div>
    <h2>你好，{{ auth.user?.nickname || auth.user?.username }}</h2>
    <p class="intro">这是基础架构的首页。登录、鉴权、统一响应、异常处理、Flyway 已经接通。</p>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <div class="card-title">登录鉴权</div>
          <div class="card-value">JWT + Refresh Token</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <div class="card-title">权限模型</div>
          <div class="card-value">用户 / 角色 / 权限</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <div class="card-title">当前角色</div>
          <div class="card-value">{{ auth.user?.permissions.length || 0 }} 个权限点</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<style scoped>
h2 {
  margin-top: 0;
}

.intro {
  color: #606266;
  margin-bottom: 20px;
}

.card-title {
  color: #909399;
  font-size: 13px;
}

.card-value {
  margin-top: 10px;
  font-size: 18px;
  font-weight: 600;
}
</style>
