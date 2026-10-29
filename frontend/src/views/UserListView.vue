<!-- 2026/10/29 gongml [需求 待补充] 用户列表页 -->
<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="title">用户列表</span>
      <el-button v-perm="'user:add'" type="primary">新增用户</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="180" />
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUsers } from '@/api/user'
import type { UserItem } from '@/types/api'

const rows = ref<UserItem[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

async function load(): Promise<void> {
  loading.value = true
  try {
    const data = await fetchUsers({ page: page.value, size: size.value })
    rows.value = data.list
    total.value = data.total
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
