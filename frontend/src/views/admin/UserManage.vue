<template>
  <div class="user-manage-container">
    <el-card class="manage-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>
      
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
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
            <el-tag :type="row.status === 0 ? 'danger' : 'success'">
              {{ row.status === 0 ? '已封禁' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button 
              v-if="row.status !== 0" 
              type="danger" 
              size="small" 
              @click="handleBanUser(row)"
              :loading="row.id === loadingRowId"
            >
              封禁
            </el-button>
            <el-button 
              v-else 
              type="success" 
              size="small" 
              @click="handleUnBanUser(row)"
              :loading="row.id === loadingRowId"
            >
              解禁
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadUsers"
        @current-change="loadUsers"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const userList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const loadingRowId = ref(null)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUserList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    userList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleBanUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要封禁用户 ${row.nickname || row.username} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    loadingRowId.value = row.id
    await adminApi.banUser(row.id)
    ElMessage.success('封禁成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('封禁失败')
    }
  } finally {
    loadingRowId.value = null
  }
}

const handleUnBanUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要解禁用户 ${row.nickname || row.username} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    loadingRowId.value = row.id
    await adminApi.unBanUser(row.id)
    ElMessage.success('解禁成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('解禁失败')
    }
  } finally {
    loadingRowId.value = null
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
  width: 100%;
}

.manage-card {
  min-height: 500px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
