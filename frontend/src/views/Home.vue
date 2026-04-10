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
          @keyup.enter="handleSearch"
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
          <el-option label="其他" value="其他" />
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
              <el-tabs v-model="activeTab" @tab-change="handleTabChange">
                <el-tab-pane label="失物信息" name="lost" />
                <el-tab-pane label="拾物信息" name="found" />
              </el-tabs>
            </div>
          </template>
          <div v-loading="loading" class="post-list">
            <div v-if="currentList.length === 0 && !loading" class="empty-tip">
              暂无{{ activeTab === 'lost' ? '失物' : '拾物' }}信息
            </div>
            <div v-for="item in currentList" :key="item.id" class="post-item" @click="viewDetail(item)">
              <div class="post-type" :class="activeTab">
                {{ activeTab === 'lost' ? '失物' : '拾物' }}
              </div>
              <div class="post-content">
                <div class="post-title-wrapper">
                  <el-tag v-if="item.sortOrder === 1 && item.applyTop === 2" type="danger" size="small" class="top-tag">置顶</el-tag>
                  <div class="post-title">{{ item.itemName }}</div>
                </div>
                <div class="post-info">
                  <span class="post-location">
                    <el-icon><Location /></el-icon>
                    {{ activeTab === 'lost' ? item.lostPlace : item.foundPlace }}
                  </span>
                  <span class="post-time">
                    <el-icon><Clock /></el-icon>
                    {{ formatDateTime(activeTab === 'lost' ? item.lostTime : item.foundTime) }}
                  </span>
                </div>
                <div class="post-desc">{{ item.description }}</div>
                <div v-if="item.contactInfo" class="post-contact">
                  <span class="contact-label">联系方式：</span>
                  <span>{{ item.contactInfo }}</span>
                </div>
              </div>
              <div v-if="item.imageUrl" class="post-image">
                <el-image :src="item.imageUrl" :preview-src-list="[item.imageUrl]" fit="cover" />
              </div>
            </div>
          </div>
          <el-pagination
            v-if="total > 0"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="pagination"
          />
        </el-card>
      </div>

      <div class="sidebar">
        <el-card class="quick-publish-card">
          <el-button type="primary" size="large" class="quick-btn" @click="goPublish">
            <el-icon><Edit /></el-icon>
            发布信息
          </el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { lostApi } from '@/api/lost'
import { foundApi } from '@/api/found'

const router = useRouter()

const activeTab = ref('lost')
const searchKeyword = ref('')
const searchLocation = ref('')
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
const loading = ref(false)
const lostList = ref([])
const foundList = ref([])

const currentList = computed(() => {
  return activeTab.value === 'lost' ? lostList.value : foundList.value
})

const loadLostItems = async () => {
  loading.value = true
  try {
    const res = await lostApi.getHomeLostItems({
      itemName: searchKeyword.value,
      lostPlace: searchLocation.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    lostList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
    lostList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadFoundItems = async () => {
  loading.value = true
  try {
    const res = await foundApi.getHomeFoundItems({
      itemName: searchKeyword.value,
      foundPlace: searchLocation.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    foundList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
    foundList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const loadData = () => {
  if (activeTab.value === 'lost') {
    loadLostItems()
  } else {
    loadFoundItems()
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  loadData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadData()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadData()
}

const viewDetail = (item) => {
  router.push(`/detail/${activeTab.value}/${item.id}`)
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const goPublish = () => {
  router.push('/publish')
}

onMounted(() => {
  loadData()
})
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
  min-height: 400px;
}

.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: #909399;
  font-size: 14px;
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

.post-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.top-tag {
  flex-shrink: 0;
}

.post-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
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
  margin-bottom: 8px;
}

.post-contact {
  font-size: 13px;
  color: #409eff;
}

.contact-label {
  color: #909399;
}

.post-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}

.post-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
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
