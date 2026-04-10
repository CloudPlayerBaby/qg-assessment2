<template>
  <div class="report-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>举报管理</span>
        </div>
      </template>
      <div v-loading="loading" class="report-list">
        <div v-if="reportList.length === 0 && !loading" class="empty-tip">
          暂无举报信息
        </div>
        <div v-for="item in reportList" :key="item.id" class="report-item">
          <div class="report-info">
            <div class="report-header">
              <span class="report-id">举报ID: {{ item.id }}</span>
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
            <div class="report-meta">
              <span>举报类型：{{ item.type === 'lost' ? '失物' : '拾物' }}</span>
              <span>目标ID：{{ item.targetId }}</span>
              <span>举报时间：{{ formatDateTime(item.createTime) }}</span>
            </div>
            <div class="report-reason">
              <span class="reason-label">举报理由：</span>
              <span class="reason-text">{{ item.reason }}</span>
            </div>
          </div>
          <div class="report-actions">
            <el-button type="primary" size="small" @click="viewPostDetail(item)">
              查看帖子
            </el-button>
            <el-button
              v-if="item.status === 1"
              type="success"
              size="small"
              @click="handleAccept(item)"
            >
              接受举报
            </el-button>
            <el-button
              v-if="item.status === 1"
              type="danger"
              size="small"
              @click="handleReject(item)"
            >
              拒绝举报
            </el-button>
          </div>
        </div>
      </div>
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadReportList"
        @current-change="loadReportList"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const router = useRouter()

const loading = ref(false)
const reportList = ref([])
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getStatusType = (status) => {
  switch (status) {
    case 1:
      return 'warning'
    case 0:
      return 'success'
    case -1:
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 1:
      return '待处理'
    case 0:
      return '已接受'
    case -1:
      return '已拒绝'
    default:
      return '未知'
  }
}

const loadReportList = async () => {
  loading.value = true
  try {
    const res = await adminApi.getReportList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.data) {
      reportList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      reportList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载举报列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const viewPostDetail = (item) => {
  window.open(`/detail/${item.type.toLowerCase()}/${item.targetId}`, '_blank')
}

const handleAccept = async (item) => {
  try {
    await ElMessageBox.confirm('确认接受该举报？帖子将被封禁。', '确认接受', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminApi.acceptReport(item.id)
    ElMessage.success('已接受举报')
    loadReportList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接受举报失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = async (item) => {
  try {
    await ElMessageBox.confirm('确认拒绝该举报？', '确认拒绝', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminApi.rejectReport(item.id)
    ElMessage.success('已拒绝举报')
    loadReportList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝举报失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadReportList()
})
</script>

<style scoped>
.report-container {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 400px;
}

.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: #909399;
  font-size: 14px;
}

.report-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.report-info {
  flex: 1;
  min-width: 0;
}

.report-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.report-id {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.report-meta {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}

.report-reason {
  font-size: 14px;
  color: #606266;
}

.reason-label {
  color: #909399;
}

.reason-text {
  color: #303133;
}

.report-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
