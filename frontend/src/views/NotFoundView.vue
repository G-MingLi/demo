<!-- 2026/10/29 gongml [需求 待补充] 404 页面 -->
<template>
  <div class="error-page">
    <el-result icon="info" title="404" sub-title="页面不存在">
      <template #extra>
        <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
