<template>
  <div class="posts-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>信息管理</span>
          <div class="header-actions">
            <el-select v-model="searchStatus" placeholder="状态筛选" clearable style="width: 150px; margin-right: 12px;">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索标题" style="width: 200px; margin-right: 12px;" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      <el-table :data="postList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'lost' ? 'danger' : 'success'">
              {{ row.type === 'lost' ? '丢失' : '拾取' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="user" label="发布者" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop" type="danger">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="success"
              size="small"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
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
const searchStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(50)

const postList = ref([
  {
    id: 1,
    type: 'lost',
    title: '丢失黑色钱包',
    location: '图书馆二楼',
    user: '张三',
    status: 'pending',
    isTop: true,
    createTime: '2024-04-05 14:30:00'
  },
  {
    id: 2,
    type: 'found',
    title: '捡到校园卡一张',
    location: '第一食堂',
    user: '李四',
    status: 'approved',
    isTop: false,
    createTime: '2024-04-05 12:15:00'
  },
  {
    id: 3,
    type: 'lost',
    title: '丢失白色AirPods',
    location: '教学楼A座',
    user: '王五',
    status: 'rejected',
    isTop: false,
    createTime: '2024-04-04 16:45:00'
  }
])

const handleSearch = () => {
  console.log('搜索', searchKeyword.value, searchStatus.value)
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要通过这条信息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    row.status = 'approved'
    ElMessage.success('审核通过')
  } catch {
  }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    row.status = 'rejected'
    ElMessage.success('已拒绝')
  } catch {
  }
}

const handleView = (row) => {
  console.log('查看详情', row)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条信息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('删除成功')
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
