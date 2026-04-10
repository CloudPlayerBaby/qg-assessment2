<template>
  <div class="post-manage-container">
    <el-card class="manage-card">
      <template #header>
        <div class="card-header">
          <span>信息管理</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="失物信息" name="lost">
          <div class="search-form">
            <el-input v-model="searchForm.itemName" placeholder="物品名称" style="width: 200px; margin-right: 10px;" clearable />
            <el-select v-model="searchForm.lostPlace" placeholder="丢失地点" style="width: 200px; margin-right: 10px;" clearable>
              <el-option label="教学楼" value="教学楼" />
              <el-option label="食堂" value="食堂" />
              <el-option label="图书馆" value="图书馆" />
              <el-option label="宿舍" value="宿舍" />
              <el-option label="操场" value="操场" />
              <el-option label="其他" value="其他" />
            </el-select>
            <el-button type="primary" @click="loadLostPosts">搜索</el-button>
            <el-button @click="resetSearch('lost')">重置</el-button>
          </div>
          
          <el-table :data="lostList" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="itemName" label="物品名称" width="150" />
            <el-table-column prop="lostPlace" label="丢失地点" width="100" />
            <el-table-column prop="lostTime" label="丢失时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.lostTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyTop" label="置顶状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getApplyTopType(row.applyTop)">
                  {{ getApplyTopText(row.applyTop) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="280">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleViewDetail(row, 'lost')">查看详情</el-button>
                <el-button 
                  v-if="row.status !== -1" 
                  type="warning" 
                  size="small" 
                  @click="handleBanPost(row, 'lost')"
                >
                  封禁
                </el-button>
                <el-button 
                  v-else 
                  type="success" 
                  size="small" 
                  @click="handleUnBanPost(row, 'lost')"
                >
                  解封
                </el-button>
                <el-button type="danger" size="small" @click="handleDeletePost(row, 'lost')">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-if="lostTotal > 0"
            v-model:current-page="lostPageNum"
            v-model:page-size="lostPageSize"
            :total="lostTotal"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadLostPosts"
            @current-change="loadLostPosts"
            class="pagination"
          />
        </el-tab-pane>
        
        <el-tab-pane label="拾物信息" name="found">
          <div class="search-form">
            <el-input v-model="searchForm.itemName" placeholder="物品名称" style="width: 200px; margin-right: 10px;" clearable />
            <el-select v-model="searchForm.foundPlace" placeholder="拾取地点" style="width: 200px; margin-right: 10px;" clearable>
              <el-option label="教学楼" value="教学楼" />
              <el-option label="食堂" value="食堂" />
              <el-option label="图书馆" value="图书馆" />
              <el-option label="宿舍" value="宿舍" />
              <el-option label="操场" value="操场" />
              <el-option label="其他" value="其他" />
            </el-select>
            <el-button type="primary" @click="loadFoundPosts">搜索</el-button>
            <el-button @click="resetSearch('found')">重置</el-button>
          </div>
          
          <el-table :data="foundList" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="itemName" label="物品名称" width="150" />
            <el-table-column prop="foundPlace" label="拾取地点" width="100" />
            <el-table-column prop="foundTime" label="拾取时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.foundTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyTop" label="置顶状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getApplyTopType(row.applyTop)">
                  {{ getApplyTopText(row.applyTop) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="280">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleViewDetail(row, 'found')">查看详情</el-button>
                <el-button 
                  v-if="row.status !== -1" 
                  type="warning" 
                  size="small" 
                  @click="handleBanPost(row, 'found')"
                >
                  封禁
                </el-button>
                <el-button 
                  v-else 
                  type="success" 
                  size="small" 
                  @click="handleUnBanPost(row, 'found')"
                >
                  解封
                </el-button>
                <el-button type="danger" size="small" @click="handleDeletePost(row, 'found')">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-if="foundTotal > 0"
            v-model:current-page="foundPageNum"
            v-model:page-size="foundPageSize"
            :total="foundTotal"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadFoundPosts"
            @current-change="loadFoundPosts"
            class="pagination"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const router = useRouter()
const activeTab = ref('lost')
const loading = ref(false)

const searchForm = reactive({
  itemName: '',
  lostPlace: '',
  foundPlace: ''
})

const lostList = ref([])
const lostPageNum = ref(1)
const lostPageSize = ref(10)
const lostTotal = ref(0)

const foundList = ref([])
const foundPageNum = ref(1)
const foundPageSize = ref(10)
const foundTotal = ref(0)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getStatusType = (status) => {
  switch (status) {
    case 1:
      return 'success'
    case 0:
      return 'warning'
    case -1:
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 1:
      return '正常'
    case 0:
      return '受理中'
    case -1:
      return '已封禁'
    default:
      return '未知'
  }
}

const getApplyTopType = (applyTop) => {
  switch (applyTop) {
    case 2:
      return 'success'
    case 1:
      return 'warning'
    case -1:
      return 'danger'
    default:
      return 'info'
  }
}

const getApplyTopText = (applyTop) => {
  switch (applyTop) {
    case 2:
      return '置顶通过'
    case 1:
      return '审核中'
    case -1:
      return '审核被驳回'
    default:
      return '未申请'
  }
}

const loadLostPosts = async () => {
  loading.value = true
  try {
    const res = await adminApi.getAllLostPosts({
      itemName: searchForm.itemName,
      lostPlace: searchForm.lostPlace,
      pageNum: lostPageNum.value,
      pageSize: lostPageSize.value
    })
    lostList.value = res.data.records || []
    lostTotal.value = res.data.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error('加载失物信息失败')
  } finally {
    loading.value = false
  }
}

const loadFoundPosts = async () => {
  loading.value = true
  try {
    const res = await adminApi.getAllFoundPosts({
      itemName: searchForm.itemName,
      foundPlace: searchForm.foundPlace,
      pageNum: foundPageNum.value,
      pageSize: foundPageSize.value
    })
    foundList.value = res.data.records || []
    foundTotal.value = res.data.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error('加载拾物信息失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row, type) => {
  router.push(`/detail/${type}/${row.id}`)
}

const handleBanPost = async (row, type) => {
  try {
    await ElMessageBox.confirm(
      `确定要封禁这条${type === 'lost' ? '失物' : '拾物'}信息吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await adminApi.banPost(row.id, type)
    ElMessage.success('封禁成功')
    if (type === 'lost') {
      await loadLostPosts()
    } else {
      await loadFoundPosts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('封禁失败')
    }
  }
}

const handleUnBanPost = async (row, type) => {
  try {
    await ElMessageBox.confirm(
      `确定要解封这条${type === 'lost' ? '失物' : '拾物'}信息吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await adminApi.unBanPost(row.id, type)
    ElMessage.success('解封成功')
    if (type === 'lost') {
      await loadLostPosts()
    } else {
      await loadFoundPosts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('解封失败')
    }
  }
}

const handleDeletePost = async (row, type) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条${type === 'lost' ? '失物' : '拾物'}信息吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await adminApi.deletePost(row.id, type)
    ElMessage.success('删除成功')
    if (type === 'lost') {
      await loadLostPosts()
    } else {
      await loadFoundPosts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

const resetSearch = (type) => {
  searchForm.itemName = ''
  if (type === 'lost') {
    searchForm.lostPlace = ''
    lostPageNum.value = 1
    loadLostPosts()
  } else {
    searchForm.foundPlace = ''
    foundPageNum.value = 1
    loadFoundPosts()
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'lost') {
    loadLostPosts()
  } else {
    loadFoundPosts()
  }
}

onMounted(() => {
  loadLostPosts()
})
</script>

<style scoped>
.post-manage-container {
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

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
