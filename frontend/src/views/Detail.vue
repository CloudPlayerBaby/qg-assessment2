<template>
  <div class="detail-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ type === 'lost' ? '失物详情' : '拾物详情' }}</span>
          <div class="header-actions">
            <el-button
              v-if="isLoggedIn && canPrivateChat"
              type="primary"
              plain
              @click="openPrivateChat"
            >
              <el-icon><ChatDotRound /></el-icon>
              私聊发帖人
            </el-button>
            <el-button v-if="isLoggedIn" type="danger" @click="showReportDialog">
              <el-icon><Warning /></el-icon>
              举报该帖子
            </el-button>
          </div>
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

        <div class="comment-section">
          <h3>评论区</h3>
          
          <div v-if="isLoggedIn" class="comment-input">
            <el-form :model="commentForm" label-position="top">
              <el-form-item label="发表评论">
                <el-input
                  v-model="commentForm.content"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入您的评论或留言内容"
                />
              </el-form-item>
              <el-form-item label="联系方式（可选）">
                <el-input
                  v-model="commentForm.contactInfo"
                  placeholder="如果您希望失主/拾主联系您，请在此留下手机号或微信"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitComment" :loading="commentSubmitting">
                  发表评论
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          <div v-else class="login-tip">
            <el-button type="primary" link @click="router.push('/login')">登录后发表评论</el-button>
          </div>

          <div class="comment-list" v-loading="commentsLoading">
            <div v-if="comments.length > 0">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <span class="comment-sender">用户 {{ comment.senderId }}</span>
                  <span class="comment-time">{{ formatDateTime(comment.createTime) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div v-if="comment.contactInfo" class="comment-contact">
                  <span class="contact-label">联系方式：</span>
                  <span class="contact-value">{{ comment.contactInfo }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无评论" :image-size="60" />
          </div>
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

    <PrivateChatDialog
      v-model="chatVisible"
      :post-id="chatPostId"
      :post-type="chatPostType"
      :peer-id="chatPeerId"
      :peer-name="chatPeerName"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning, ChatDotRound } from '@element-plus/icons-vue'
import PrivateChatDialog from '@/components/PrivateChatDialog.vue'
import { lostApi } from '@/api/lost'
import { foundApi } from '@/api/found'
import { commentApi } from '@/api/comment'
import { userApi } from '@/api/user'
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

const comments = ref([])
const commentsLoading = ref(false)
const commentSubmitting = ref(false)
const commentForm = ref({
  postId: null,
  postType: '',
  content: '',
  contactInfo: ''
})

const isLoggedIn = computed(() => userStore.isLoggedIn)
const currentUserId = computed(() => userStore.userInfo?.id)

const chatVisible = ref(false)
const chatPostId = ref(null)
const chatPostType = ref('')
const chatPeerId = ref(null)
const chatPeerName = ref('')

const canPrivateChat = computed(() => {
  if (!item.value || !currentUserId.value) return false
  const ownerId = item.value.userId
  return ownerId != null && Number(ownerId) !== Number(currentUserId.value)
})

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
      commentForm.value.postId = id
      commentForm.value.postType = lowerType
      loadComments()
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  const { id, itemType } = route.params
  commentsLoading.value = true
  try {
    const res = await commentApi.getComments(id, itemType.toLowerCase())
    comments.value = res.data
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentSubmitting.value = true
  try {
    await commentApi.addComment(commentForm.value)
    ElMessage.success('评论发表成功')
    commentForm.value.content = ''
    commentForm.value.contactInfo = ''
    loadComments()
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表失败')
  } finally {
    commentSubmitting.value = false
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

const openPrivateChat = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!item.value?.userId) {
    ElMessage.warning('无法发起私聊')
    return
  }
  chatPostId.value = Number(route.params.id)
  chatPostType.value = type.value
  chatPeerId.value = item.value.userId
  userApi.getUserInfoById(item.value.userId).then((res) => {
    chatPeerName.value = res.data.nickname || res.data.username || '发帖人'
    chatVisible.value = true
  })
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

.header-actions {
  display: flex;
  gap: 12px;
}

.comment-section {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
}

.comment-section h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
}

.comment-input {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.login-tip {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 30px;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #f2f6fc;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-sender {
  font-weight: bold;
  color: #409eff;
  font-size: 14px;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.comment-contact {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #ecf5ff;
  border-radius: 4px;
  font-size: 13px;
}

.contact-label {
  color: #409eff;
}

.contact-value {
  color: #303133;
  font-weight: 500;
}
</style>
