<template>
  <div class="posts-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>置顶审核</span>
          <div class="header-actions">
            <el-input v-model="searchKeyword" placeholder="搜索物品名称" style="width: 200px; margin-right: 12px;" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      <el-tabs v-model="activeTab" class="posts-tabs">
        <el-tab-pane label="失物审核" name="lost">
          <div v-loading="loading" class="post-list">
            <div v-if="lostList.length === 0 && !loading" class="empty-tip">
              暂无待审核的失物信息
            </div>
            <div v-for="item in lostList" :key="item.id" class="post-item">
              <div class="post-type lost">失物</div>
              <div class="post-content">
                <div class="post-title">{{ item.itemName }}</div>
                <div class="post-info">
                  <span class="post-location">
                    <el-icon><Location /></el-icon>
                    {{ item.lostPlace }}
                  </span>
                  <span class="post-time">
                    <el-icon><Clock /></el-icon>
                    {{ formatDateTime(item.lostTime) }}
                  </span>
                </div>
                <div class="post-desc">{{ item.description }}</div>
              </div>
              <div v-if="item.imageUrl" class="post-image">
                <el-image :src="item.imageUrl" :preview-src-list="[item.imageUrl]" fit="cover" />
              </div>
              <div class="post-actions">
                <el-input-number
                  v-model="item.hours"
                  :min="1"
                  :max="168"
                  label="小时"
                  size="small"
                  class="hours-input"
                  placeholder="置顶时长"
                />
                <el-button type="primary" size="small" @click="handleTop(item, 'lost')">通过置顶</el-button>
                <el-button type="danger" size="small" @click="handleReject(item.id, 'lost')">拒绝</el-button>
              </div>
            </div>
          </div>
          <el-pagination
            v-if="lostTotal > 0"
            v-model:current-page="lostPageNum"
            v-model:page-size="lostPageSize"
            :total="lostTotal"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadLostData"
            @current-change="loadLostData"
            class="pagination"
          />
        </el-tab-pane>

        <el-tab-pane label="拾物审核" name="found">
          <div v-loading="loading" class="post-list">
            <div v-if="foundList.length === 0 && !loading" class="empty-tip">
              暂无待审核的拾物信息
            </div>
            <div v-for="item in foundList" :key="item.id" class="post-item">
              <div class="post-type found">拾物</div>
              <div class="post-content">
                <div class="post-title">{{ item.itemName }}</div>
                <div class="post-info">
                  <span class="post-location">
                    <el-icon><Location /></el-icon>
                    {{ item.foundPlace }}
                  </span>
                  <span class="post-time">
                    <el-icon><Clock /></el-icon>
                    {{ formatDateTime(item.foundTime) }}
                  </span>
                </div>
                <div class="post-desc">{{ item.description }}</div>
                <div class="post-contact">
                  <span class="contact-label">联系方式：</span>
                  <span>{{ item.contactInfo }}</span>
                </div>
              </div>
              <div class="post-actions">
                <el-input-number
                  v-model="item.hours"
                  :min="1"
                  :max="168"
                  label="小时"
                  size="small"
                  class="hours-input"
                  placeholder="置顶时长"
                />
                <el-button type="primary" size="small" @click="handleTop(item, 'found')">通过置顶</el-button>
                <el-button type="danger" size="small" @click="handleReject(item.id, 'found')">拒绝</el-button>
              </div>
            </div>
          </div>
          <el-pagination
            v-if="foundTotal > 0"
            v-model:current-page="foundPageNum"
            v-model:page-size="foundPageSize"
            :total="foundTotal"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadFoundData"
            @current-change="loadFoundData"
            class="pagination"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Clock } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

const activeTab = ref('lost')
const loading = ref(false)
const searchKeyword = ref('')

const lostList = ref([])
const lostPageNum = ref(1)
const lostPageSize = ref(5)
const lostTotal = ref(0)

const foundList = ref([])
const foundPageNum = ref(1)
const foundPageSize = ref(5)
const foundTotal = ref(0)

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadLostData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getLostPosts({
      pageNum: lostPageNum.value,
      pageSize: lostPageSize.value,
      itemName: searchKeyword.value
    })
    if (res.data) {
      lostList.value = res.data.records.map(item => ({ ...item, hours: 24 }))
      lostTotal.value = res.data.total
    } else {
      lostList.value = []
      lostTotal.value = 0
    }
  } catch (error) {
    console.error('加载失物审核列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadFoundData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getFoundPosts({
      pageNum: foundPageNum.value,
      pageSize: foundPageSize.value,
      itemName: searchKeyword.value
    })
    if (res.data) {
      foundList.value = res.data.records.map(item => ({ ...item, hours: 24 }))
      foundTotal.value = res.data.total
    } else {
      foundList.value = []
      foundTotal.value = 0
    }
  } catch (error) {
    console.error('加载拾物审核列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleTop = async (item, type) => {
  try {
    await ElMessageBox.confirm(`确认置顶${type === 'lost' ? '失物' : '拾物'}信息 ${item.itemName} ${item.hours}小时？`, '确认置顶', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (type === 'lost') {
      await adminApi.setLostTop({ id: item.id, hours: item.hours })
    } else {
      await adminApi.setFoundTop({ id: item.id, hours: item.hours })
    }
    ElMessage.success('置顶成功')
    type === 'lost' ? loadLostData() : loadFoundData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('置顶失败:', error)
      ElMessage.error('置顶失败')
    }
  }
}

const handleReject = async (id, type) => {
  try {
    await ElMessageBox.confirm('确认拒绝该置顶申请？', '确认拒绝', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (type === 'lost') {
      await adminApi.rejectLostTop(id)
    } else {
      await adminApi.rejectFoundTop(id)
    }
    ElMessage.success('已拒绝')
    type === 'lost' ? loadLostData() : loadFoundData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
      ElMessage.error('拒绝失败')
    }
  }
}

const handleSearch = () => {
  if (activeTab.value === 'lost') {
    lostPageNum.value = 1
    loadLostData()
  } else {
    foundPageNum.value = 1
    loadFoundData()
  }
}

onMounted(() => {
  loadLostData()
  loadFoundData()
})
</script>

<style scoped>
.posts-container {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.posts-tabs {
  margin-top: 10px;
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
  align-items: flex-start;
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

.post-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

.hours-input {
  width: 120px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
