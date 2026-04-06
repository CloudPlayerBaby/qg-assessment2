<template>
  <div class="home-container">
    <el-card class="search-card">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索物品名称..."
          size="large"
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchLocation" placeholder="选择地点" clearable size="large" class="search-select">
          <el-option label="全部地点" value="" />
          <el-option label="教学楼" value="教学楼" />
          <el-option label="食堂" value="食堂" />
          <el-option label="图书馆" value="图书馆" />
          <el-option label="宿舍" value="宿舍" />
          <el-option label="操场" value="操场" />
        </el-select>
        <el-select v-model="searchType" placeholder="选择类型" clearable size="large" class="search-select">
          <el-option label="全部类型" value="" />
          <el-option label="丢失物品" value="lost" />
          <el-option label="拾取物品" value="found" />
        </el-select>
        <el-button type="primary" size="large" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </el-card>

    <div class="content-wrapper">
      <div class="main-content">
        <el-card class="post-list-card">
          <template #header>
            <div class="card-header">
              <span>最新信息</span>
              <el-radio-group v-model="sortType" size="small">
                <el-radio-button value="time">按时间</el-radio-button>
                <el-radio-button value="hot">按热度</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="post-list">
            <div v-for="item in mockPosts" :key="item.id" class="post-item" @click="viewDetail(item)">
              <div class="post-type" :class="item.type">
                {{ item.type === 'lost' ? '失物' : '拾物' }}
              </div>
              <div class="post-content">
                <div class="post-title">{{ item.title }}</div>
                <div class="post-info">
                  <span class="post-location">
                    <el-icon><Location /></el-icon>
                    {{ item.location }}
                  </span>
                  <span class="post-time">
                    <el-icon><Clock /></el-icon>
                    {{ item.time }}
                  </span>
                </div>
                <div class="post-desc">{{ item.description }}</div>
              </div>
              <div class="post-avatar">
                <el-avatar :size="40">{{ item.user.charAt(0) }}</el-avatar>
              </div>
            </div>
          </div>
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            class="pagination"
          />
        </el-card>
      </div>

      <div class="sidebar">
        <el-card class="stats-card">
          <template #header>
            <span>平台统计</span>
          </template>
          <div class="stats-item">
            <div class="stats-number">1,234</div>
            <div class="stats-label">发布信息</div>
          </div>
          <div class="stats-item">
            <div class="stats-number">856</div>
            <div class="stats-label">找回物品</div>
          </div>
          <div class="stats-item">
            <div class="stats-number">567</div>
            <div class="stats-label">活跃用户</div>
          </div>
        </el-card>

        <el-card class="quick-publish-card">
          <el-button type="primary" size="large" class="quick-btn" @click="goPublish('lost')">
            <el-icon><Warning /></el-icon>
            发布失物信息
          </el-button>
          <el-button type="success" size="large" class="quick-btn" @click="goPublish('found')">
            <el-icon><CircleCheck /></el-icon>
            发布拾物信息
          </el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const searchKeyword = ref('')
const searchLocation = ref('')
const searchType = ref('')
const sortType = ref('time')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(50)

const mockPosts = ref([
  {
    id: 1,
    type: 'lost',
    title: '丢失黑色钱包',
    location: '图书馆二楼',
    time: '2024-04-05 14:30',
    description: '黑色皮质钱包，内有身份证、银行卡和现金若干',
    user: '张三'
  },
  {
    id: 2,
    type: 'found',
    title: '捡到校园卡一张',
    location: '第一食堂',
    time: '2024-04-05 12:15',
    description: '在食堂门口捡到校园卡一张，请到失物招领处认领',
    user: '李四'
  },
  {
    id: 3,
    type: 'lost',
    title: '丢失白色AirPods',
    location: '教学楼A座',
    time: '2024-04-04 16:45',
    description: '白色AirPods Pro，充电盒有轻微划痕',
    user: '王五'
  },
  {
    id: 4,
    type: 'found',
    title: '捡到一串钥匙',
    location: '操场',
    time: '2024-04-04 18:20',
    description: '一串钥匙，有宿舍钥匙和自行车钥匙',
    user: '赵六'
  }
])

const handleSearch = () => {
  console.log('搜索', searchKeyword.value, searchLocation.value, searchType.value)
}

const viewDetail = (item) => {
  console.log('查看详情', item)
}

const goPublish = (type) => {
  router.push('/publish')
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 24px;
}

.search-box {
  display: flex;
  gap: 16px;
  align-items: center;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.search-select {
  width: 150px;
}

.content-wrapper {
  display: flex;
  gap: 24px;
}

.main-content {
  flex: 1;
}

.sidebar {
  width: 300px;
  flex-shrink: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.post-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.post-type {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  color: white;
  flex-shrink: 0;
}

.post-type.lost {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.post-type.found {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.post-content {
  flex: 1;
  min-width: 0;
}

.post-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.post-info {
  display: flex;
  gap: 24px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #909399;
}

.post-location,
.post-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-desc {
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-avatar {
  flex-shrink: 0;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.stats-card {
  margin-bottom: 24px;
}

.stats-item {
  text-align: center;
  padding: 16px 0;
}

.stats-item + .stats-item {
  border-top: 1px solid #ebeef5;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.quick-publish-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-btn {
  width: 100%;
}
</style>
