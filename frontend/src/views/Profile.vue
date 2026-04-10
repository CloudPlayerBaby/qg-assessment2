<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card class="info-card">
          <div class="user-avatar">
            <el-avatar :size="120" :src="userInfo?.avatarUrl">
              {{ userInfo?.nickname?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="user-name">{{ userInfo?.nickname || userInfo?.username }}</div>
          <div class="user-role" v-if="userInfo?.identity === 1">管理员</div>
          <div class="user-role" v-else>普通用户</div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="main-card">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="个人信息" name="info">
              <el-form :model="profileForm" label-width="100px">
                <el-form-item label="用户名">
                  <el-input v-model="profileForm.username" disabled />
                </el-form-item>
                <el-form-item label="昵称">
                  <el-input v-model="profileForm.nickname" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" disabled />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phoneNumber" />
                </el-form-item>
                <el-form-item label="头像">
                  <el-upload
                    action="#"
                    list-type="picture-card"
                    :auto-upload="false"
                    :limit="1"
                    :show-file-list="false"
                    :on-change="handleAvatarChange"
                  >
                    <el-avatar :size="80" :src="profileForm.avatarUrl">
                      <el-icon><Plus /></el-icon>
                    </el-avatar>
                  </el-upload>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSave" :loading="loading">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" label-width="100px">
                <el-form-item label="原密码">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="我发布的失物" name="lost">
              <div class="lost-list">
                <div v-if="lostList.length === 0 && !loadingLost" class="empty-tip">
                  还没有发布过失物信息哦
                </div>
                <el-card v-for="item in lostList" :key="item.id" class="lost-item" shadow="hover">
                  <div class="lost-item-header">
                    <div class="item-name">{{ item.itemName }}</div>
                    <div class="item-time">{{ formatDate(item.createTime) }}</div>
                  </div>
                  <div class="lost-item-content">
                    <el-image v-if="item.imageUrl" :src="item.imageUrl" :preview-src-list="[item.imageUrl]" class="item-image" fit="cover" />
                    <div class="item-info">
                      <div class="info-row">
                        <span class="label">丢失地点：</span>
                        <span>{{ item.lostPlace }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">丢失时间：</span>
                        <span>{{ formatDateTime(item.lostTime) }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">物品描述：</span>
                        <span>{{ item.description }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="lost-item-footer">
                    <div class="footer-left">
                      <el-tag v-if="item.status === -1" type="danger">已封禁</el-tag>
                      <el-tag v-else-if="item.applyTop === 1" type="warning">置顶审核中</el-tag>
                      <el-tag v-else-if="item.applyTop === 2" type="success">置顶审核通过</el-tag>
                      <el-tag v-else-if="item.applyTop === -1" type="danger">置顶审核被驳回</el-tag>
                    </div>
                    <div class="footer-right">
                      <el-button v-if="item.status !== -1" type="primary" size="small" @click.stop="handleEditLost(item)">编辑</el-button>
                      <el-button type="danger" size="small" @click.stop="handleDeleteLost(item)">删除</el-button>
                    </div>
                  </div>
                </el-card>
                
                <el-pagination
                  v-if="total > 0"
                  v-model:current-page="pageNum"
                  v-model:page-size="pageSize"
                  :total="total"
                  :page-sizes="[5, 10, 20]"
                  layout="total, sizes, prev, pager, next"
                  @size-change="loadLostItems"
                  @current-change="loadLostItems"
                  class="pagination"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane label="我发布的拾物" name="found">
              <div class="found-list">
                <div v-if="foundList.length === 0 && !loadingFound" class="empty-tip">
                  还没有发布过拾物信息哦
                </div>
                <el-card v-for="item in foundList" :key="item.id" class="found-item" shadow="hover">
                  <div class="found-item-header">
                    <div class="item-name">{{ item.itemName }}</div>
                    <div class="item-time">{{ formatDate(item.createTime) }}</div>
                  </div>
                  <div class="found-item-content">
                    <div class="item-info">
                      <div class="info-row">
                        <span class="label">拾取地点：</span>
                        <span>{{ item.foundPlace }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">拾取时间：</span>
                        <span>{{ formatDateTime(item.foundTime) }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">物品描述：</span>
                        <span>{{ item.description }}</span>
                      </div>
                      <div class="info-row">
                        <span class="label">联系方式：</span>
                        <span>{{ item.contactInfo }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="found-item-footer">
                    <div class="footer-left">
                      <el-tag v-if="item.status === -1" type="danger">已封禁</el-tag>
                      <el-tag v-else-if="item.applyTop === 1" type="warning">置顶审核中</el-tag>
                      <el-tag v-else-if="item.applyTop === 2" type="success">置顶审核通过</el-tag>
                      <el-tag v-else-if="item.applyTop === -1" type="danger">置顶审核被驳回</el-tag>
                    </div>
                    <div class="footer-right">
                      <el-button v-if="item.status !== -1" type="primary" size="small" @click.stop="handleEditFound(item)">编辑</el-button>
                      <el-button type="danger" size="small" @click.stop="handleDeleteFound(item)">删除</el-button>
                    </div>
                  </div>
                </el-card>
                
                <el-pagination
                  v-if="foundTotal > 0"
                  v-model:current-page="foundPageNum"
                  v-model:page-size="foundPageSize"
                  :total="foundTotal"
                  :page-sizes="[5, 10, 20]"
                  layout="total, sizes, prev, pager, next"
                  @size-change="loadFoundItems"
                  @current-change="loadFoundItems"
                  class="pagination"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import { fileApi } from '@/api/file'
import { lostApi } from '@/api/lost'
import { foundApi } from '@/api/found'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const userInfo = ref(null)
const activeTab = ref('info')

const profileForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phoneNumber: '',
  avatarUrl: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const lostList = ref([])
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const loadingLost = ref(false)

const foundList = ref([])
const foundPageNum = ref(1)
const foundPageSize = ref(5)
const foundTotal = ref(0)
const loadingFound = ref(false)

const loading = ref(false)
const passwordLoading = ref(false)
const uploadLoading = ref(false)

const loadUserInfo = async () => {
  try {
    const res = await userApi.getUserInfo()
    userInfo.value = res.data
    Object.assign(profileForm, res.data)
    userStore.setUserInfo(res.data)
  } catch (error) {
    console.error(error)
  }
}

const loadLostItems = async () => {
  loadingLost.value = true
  try {
    const res = await lostApi.getMyLostItems({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    lostList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
    lostList.value = []
    total.value = 0
  } finally {
    loadingLost.value = false
  }
}

const loadFoundItems = async () => {
  loadingFound.value = true
  try {
    const res = await foundApi.getMyFoundItems({
      pageNum: foundPageNum.value,
      pageSize: foundPageSize.value
    })
    foundList.value = res.data.records || []
    foundTotal.value = res.data.total || 0
  } catch (error) {
    console.error(error)
    foundList.value = []
    foundTotal.value = 0
  } finally {
    loadingFound.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const handleEditLost = (item) => {
  router.push({
    path: '/edit-lost',
    query: { id: item.id }
  })
}

const handleEditFound = (item) => {
  router.push({
    path: '/edit-found',
    query: { id: item.id }
  })
}

const handleDeleteLost = async (item) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条失物信息吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await lostApi.deleteLostItem(item.id)
    ElMessage.success('删除成功')
    loadLostItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      if (error.response && error.response.data && error.response.data.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('删除失败')
      }
    }
  }
}

const handleDeleteFound = async (item) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条拾物信息吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await foundApi.deleteFoundItem(item.id)
    ElMessage.success('删除成功')
    loadFoundItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      if (error.response && error.response.data && error.response.data.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('删除失败')
      }
    }
  }
}

onMounted(() => {
  loadUserInfo()
})

const handleAvatarChange = async (file) => {
  if (file.raw) {
    uploadLoading.value = true
    try {
      const res = await fileApi.upload(file.raw)
      profileForm.avatarUrl = res.data
      ElMessage.success('头像上传成功')
    } catch (error) {
      console.error(error)
      ElMessage.error('头像上传失败')
    } finally {
      uploadLoading.value = false
    }
  }
}

const handleSave = async () => {
  loading.value = true
  try {
    await userApi.updateProfile({
      nickname: profileForm.nickname,
      phoneNumber: profileForm.phoneNumber,
      avatarUrl: profileForm.avatarUrl
    })
    ElMessage.success('保存成功')
    await loadUserInfo()
  } catch (error) {
    console.error(error)
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('保存失败')
    }
  } finally {
    loading.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return
  }
  passwordLoading.value = true
  try {
    await userApi.updatePassword(passwordForm)
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error(error)
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('密码修改失败')
    }
  } finally {
    passwordLoading.value = false
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'lost') {
    loadLostItems()
  } else if (tabName === 'found') {
    loadFoundItems()
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.info-card {
  text-align: center;
}

.user-avatar {
  margin-bottom: 16px;
}

.user-name {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.user-role {
  font-size: 14px;
  color: #909399;
}

.main-card {
  min-height: 600px;
}

.main-card :deep(.el-tabs__content) {
  overflow: visible;
}

.main-card :deep(.el-tab-pane) {
  overflow: visible;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
}

.lost-list,
.found-list {
  min-height: 400px;
}

.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: #909399;
  font-size: 14px;
}

.lost-item,
.found-item {
  margin-bottom: 16px;
}

.lost-item-header,
.found-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.item-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.item-time {
  font-size: 12px;
  color: #909399;
}

.lost-item-content,
.found-item-content {
  display: flex;
  gap: 16px;
}

.item-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
}

.info-row {
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.info-row .label {
  color: #909399;
}

.lost-item-footer,
.found-item-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
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
