<template>
  <div class="detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ type === 'lost' ? '失物详情' : '拾物详情' }}</span>
          <el-button v-if="isLoggedIn" type="danger" @click="showReportDialog">
            <el-icon><Warning /></el-icon>
            举报该帖子
          </el-button>
        </div>
      </template>

      <div v-if="item" class="detail-content">
        <div class="detail-header">
          <div class="detail-type" :class="type">
            {{ type === 'lost' ? '失物' : '拾物' }}
          </div>
          <div class="detail-title-wrapper">
            <el-tag v-if="item.sortOrder === 1 && item.applyTop === 2" type="danger" size="small" class="top-tag">置顶</el-tag>
            <h1 class="detail-title">{{ item.itemName }}</h1>
          </div>
        </div>

        <div class="detail-info">
          <div class="info-item">
            <span class="info-label">{{ type === 'lost' ? '丢失地点' : '拾取地点' }}：</span>
            <span class="info-value">{{ type === 'lost' ? item.lostPlace : item.foundPlace }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">{{ type === 'lost' ? '丢失时间' : '拾取时间' }}：</span>
            <span class="info-value">{{ formatDateTime(type === 'lost' ? item.lostTime : item.foundTime) }}</span>
          </div>
          <div v-if="item.contactInfo" class="info-item">
            <span class="info-label">联系方式：</span>
            <span class="info-value contact">{{ item.contactInfo }}</span>
          </div>
        </div>

        <div class="detail-desc">
          <h3>物品描述</h3>
          <p>{{ item.description }}</p>
        </div>

        <div v-if="item.imageUrl" class="detail-image">
          <el-image :src="item.imageUrl" :preview-src-list="[item.imageUrl]" fit="contain" />
        </div>
      </div>

      <el-empty v-else description="帖子不存在" />
    </el-card>

    <el-dialog
      v-model="reportDialogVisible"
      title="举报帖子"
      width="500px"
    >
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="举报理由">
          <el-input
            v-model="reportForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入举报理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport" :loading="submitting">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { lostApi } from '@/api/lost'
import { foundApi } from '@/api/found'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const type = ref('')
const item = ref(null)
const loading = ref(false)
const reportDialogVisible = ref(false)
const submitting = ref(false)
const reportForm = ref({
  targetId: null,
  type: '',
  reason: ''
})

const isLoggedIn = computed(() => userStore.isLoggedIn)

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadDetail = async () => {
  const { id, itemType } = route.params
  const lowerType = itemType.toLowerCase()
  type.value = lowerType
  loading.value = true

  try {
    let res
    if (lowerType === 'lost') {
      res = await lostApi.getDetail(id)
    } else {
      res = await foundApi.getDetail(id)
    }
    if (res.data) {
      item.value = res.data
      reportForm.value.targetId = id
      reportForm.value.type = lowerType
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const showReportDialog = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  reportForm.value.reason = ''
  reportDialogVisible.value = true
}

const submitReport = async () => {
  if (!reportForm.value.reason.trim()) {
    ElMessage.warning('请输入举报理由')
    return
  }

  submitting.value = true
  try {
    if (type.value === 'lost') {
      await lostApi.report(reportForm.value)
    } else {
      await foundApi.report(reportForm.value)
    }
    ElMessage.success('举报成功，我们会尽快处理')
    reportDialogVisible.value = false
  } catch (error) {
    console.error('举报失败:', error)
    ElMessage.error('举报失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-content {
  padding: 16px 0;
}

.detail-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-type {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: white;
  flex-shrink: 0;
}

.detail-type.lost {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.detail-type.found {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.detail-title-wrapper {
  flex: 1;
  min-width: 0;
}

.detail-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
  margin-top: 8px;
}

.top-tag {
  margin-right: 8px;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.info-item {
  font-size: 15px;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #303133;
}

.info-value.contact {
  color: #409eff;
  font-weight: 500;
}

.detail-desc {
  margin-bottom: 24px;
}

.detail-desc h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}

.detail-desc p {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin: 0;
}

.detail-image {
  width: 100%;
  max-width: 500px;
}

.detail-image :deep(.el-image) {
  width: 100%;
  border-radius: 8px;
}
</style>
