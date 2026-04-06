<template>
  <div class="users-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-input v-model="searchKeyword" placeholder="搜索用户名/邮箱" style="width: 250px; margin-right: 12px;" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      <el-table :data="userList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phoneNumber" label="手机号" width="130" />
        <el-table-column prop="identity" label="身份" width="100">
          <template #default="{ row }">
            <el-tag :type="row.identity === 1 ? 'danger' : 'primary'">
              {{ row.identity === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              @click="handleBan(row)"
            >
              封禁
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="handleUnban(row)"
            >
              解封
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(50)

const userList = ref([
  {
    id: 1,
    username: 'zhangsan',
    nickname: '张三',
    email: 'zhangsan@example.com',
    phoneNumber: '13800138001',
    identity: 0,
    status: 1,
    createTime: '2024-03-01 10:00:00'
  },
  {
    id: 2,
    username: 'lisi',
    nickname: '李四',
    email: 'lisi@example.com',
    phoneNumber: '13800138002',
    identity: 0,
    status: 0,
    createTime: '2024-03-05 14:30:00'
  },
  {
    id: 3,
    username: 'admin',
    nickname: '管理员',
    email: 'admin@example.com',
    phoneNumber: '13800138000',
    identity: 1,
    status: 1,
    createTime: '2024-02-01 08:00:00'
  }
])

const handleSearch = () => {
  console.log('搜索', searchKeyword.value)
}

const handleBan = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要封禁用户「${row.nickname}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    row.status = 0
    ElMessage.success('封禁成功')
  } catch {
  }
}

const handleUnban = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解封用户「${row.nickname}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    row.status = 1
    ElMessage.success('解封成功')
  } catch {
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
