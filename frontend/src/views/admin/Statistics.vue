<template>
  <div class="statistics-container">
    <el-row :gutter="24">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">1,234</div>
              <div class="stat-label">发布信息总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon :size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">856</div>
              <div class="stat-label">找回物品数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">567</div>
              <div class="stat-label">活跃用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
              <el-icon :size="32"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">3,456</div>
              <div class="stat-label">消息总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>近7天发布趋势</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表区域" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>物品类型分布</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表区域" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>热门地点</span>
          </template>
          <el-table :data="locationData" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.rank <= 3" :type="row.rank === 1 ? 'danger' : row.rank === 2 ? 'warning' : 'success'">
                  {{ row.rank }}
                </el-tag>
                <span v-else>{{ row.rank }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="地点" />
            <el-table-column prop="count" label="数量" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>AI分析报告</span>
          </template>
          <div class="ai-report">
            <p>📊 根据近7天的数据统计：</p>
            <ul>
              <li>图书馆是失物高发区域，占比28%</li>
              <li>证件类物品最容易丢失，其次是电子设备</li>
              <li>中午12:00-13:00和下午17:00-18:00是丢失高峰期</li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const locationData = ref([
  { rank: 1, location: '图书馆', count: 156 },
  { rank: 2, location: '第一食堂', count: 134 },
  { rank: 3, location: '教学楼A座', count: 98 },
  { rank: 4, location: '宿舍区', count: 87 },
  { rank: 5, location: '操场', count: 65 }
])
</script>

<style scoped>
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

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-report {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
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
