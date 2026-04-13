<template>
  <div class="statistics-container">
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ formatNumber(postNumber) }}</div>
              <div class="stat-label">发布信息总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon :size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ formatNumber(completedPostNumber) }}</div>
              <div class="stat-label">找回物品数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ formatNumber(activeUsersNumber) }}</div>
              <div class="stat-label">活跃用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>AI分析报告</span>
              <el-button type="primary" size="small" @click="loadAiReport" :loading="generatingReport">
                <el-icon><MagicStick /></el-icon>
                生成分析报告
              </el-button>
            </div>
          </template>
          <div class="ai-report">
            <p v-if="!aiReport" style="color: #909399; text-align: center; padding: 40px;">
              点击上方按钮生成AI分析报告
            </p>
            <div v-else style="white-space: pre-wrap; line-height: 1.8;">
              {{ aiReport }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { MagicStick, Document, CircleCheck, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const postNumber = ref(0)
const completedPostNumber = ref(0)
const activeUsersNumber = ref(0)
const aiReport = ref('')
const generatingReport = ref(false)

const formatNumber = (num) => {
  if (num === null || num === undefined) return '-'
  return num.toLocaleString()
}

const loadStatistics = async () => {
  try {
    const [postRes, completedRes, activeRes] = await Promise.all([
      adminApi.getPostNumber(),
      adminApi.getCompletedPostNumber(),
      adminApi.getActiveUsersNumber()
    ])
    postNumber.value = postRes.data || 0
    completedPostNumber.value = completedRes.data || 0
    activeUsersNumber.value = activeRes.data || 0
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadAiReport = async () => {
  generatingReport.value = true
  try {
    const res = await adminApi.getAiAnalysisReport()
    aiReport.value = res.data || ''
    ElMessage.success('分析报告生成成功')
  } catch (error) {
    console.error('生成AI分析报告失败:', error)
    ElMessage.error('生成分析报告失败')
  } finally {
    generatingReport.value = false
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics-container {
  width: 100%;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.ai-report {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  min-height: 200px;
}

.ai-report p {
  font-weight: bold;
  margin-bottom: 12px;
}

.ai-report ul {
  padding-left: 20px;
}

.ai-report li {
  margin-bottom: 8px;
  color: #606266;
}
</style>
