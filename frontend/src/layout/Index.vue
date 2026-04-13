<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <el-icon :size="28"><DocumentCopy /></el-icon>
          <span>校园AI失物招领</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          router
          class="header-menu"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/publish">发布信息</el-menu-item>
        </el-menu>
        <div class="header-right">
          <div v-if="isLoggedIn" class="message-container" @click="router.push('/profile?tab=messages')">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </el-badge>
          </div>
          <div v-if="isLoggedIn" class="ai-assistant-container" @click="showAiDialog = true">
            <el-icon :size="20"><MagicStick /></el-icon>
          </div>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userInfo?.avatarUrl">
                {{ userInfo?.nickname?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item v-if="userInfo?.identity === 1" command="admin">
                  <el-icon><Setting /></el-icon>
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    <el-main class="layout-main">
      <router-view />
    </el-main>

    <!-- AI 助手对话框 -->
    <el-dialog
      v-model="showAiDialog"
      title="AI 校园小助手"
      width="500px"
      :close-on-click-modal="false"
      class="ai-dialog"
    >
      <div class="ai-chat-history" ref="chatHistoryRef">
        <div v-for="(msg, index) in aiChatHistory" :key="index" :class="['chat-bubble', msg.role]">
          <div class="bubble-content">{{ msg.content }}</div>
        </div>
      </div>
      <template #footer>
        <div class="ai-input-wrapper">
          <el-input
            v-model="aiInput"
            placeholder="问问我失物招领相关问题吧..."
            @keyup.enter="handleAiChat"
            :disabled="aiLoading"
          >
            <template #append>
              <el-button @click="handleAiChat" :loading="aiLoading">
                发送
              </el-button>
            </template>
          </el-input>
        </div>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { DocumentCopy, User, Setting, SwitchButton, ChatDotRound, MagicStick } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { commentApi } from '@/api/comment'
import { privateMessageApi } from '@/api/privateMessage'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const isLoggedIn = computed(() => userStore.isLoggedIn)

const activeMenu = computed(() => route.path)

const unreadCount = ref(0)
let timer = null

// AI 助手相关
const showAiDialog = ref(false)
const aiInput = ref('')
const aiLoading = ref(false)
const aiChatHistory = ref([
  { role: 'ai', content: '你好！我是你的失物招领小助手，有什么可以帮你的吗？' }
])
const chatHistoryRef = ref(null)

const handleAiChat = async () => {
  if (!aiInput.value.trim() || aiLoading.value) return
  
  const userMsg = aiInput.value.trim()
  aiChatHistory.value.push({ role: 'user', content: userMsg })
  aiInput.value = ''
  aiLoading.value = true
  
  scrollToBottom()

  try {
    const res = await request.get('/ai/chat', { params: { message: userMsg } })
    aiChatHistory.value.push({ role: 'ai', content: res.data })
  } catch (error) {
    aiChatHistory.value.push({ role: 'ai', content: '抱歉，小助手现在开小差了，请稍后再试。' })
  } finally {
    aiLoading.value = false
    scrollToBottom()
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatHistoryRef.value) {
    chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
  }
}
let ws = null

const fetchUnreadTotal = async () => {
  if (!isLoggedIn.value) return
  try {
    const [cRes, pRes] = await Promise.all([
      commentApi.getUnreadCount(),
      privateMessageApi.getUnreadCount()
    ])
    unreadCount.value = (cRes.data || 0) + (pRes.data || 0)
  } catch (error) {
    console.error('获取未读消息失败:', error)
  }
}

function buildWsUrl() {
  const token = localStorage.getItem('token') || ''
  if (import.meta.env.DEV) {
    return `ws://localhost:8080/ws/private?token=${encodeURIComponent(token)}`
  }
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${proto}//${window.location.host}/ws/private?token=${encodeURIComponent(token)}`
}

function disconnectWs() {
  if (ws) {
    ws.close()
    ws = null
  }
}

function connectWs() {
  disconnectWs()
  if (!isLoggedIn.value) return
  ws = new WebSocket(buildWsUrl())
  ws.onmessage = (ev) => {
    const d = JSON.parse(ev.data)
    window.dispatchEvent(new CustomEvent('ws-chat', { detail: d }))
    if (d.type === 'newComment') {
      ElNotification({
        title: '新留言',
        message: d.preview || '有人给您的帖子留言了'
      })
      fetchUnreadTotal()
    }
    if (d.type === 'newPm') {
      ElNotification({
        title: '新私信',
        message: '您收到一条与该帖子相关的私信'
      })
      fetchUnreadTotal()
    }
  }
  ws.onerror = () => {}
}

onMounted(() => {
  fetchUnreadTotal()
  timer = setInterval(fetchUnreadTotal, 30000)
  connectWs()
  window.addEventListener('refresh-unread', fetchUnreadTotal)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  disconnectWs()
  window.removeEventListener('refresh-unread', fetchUnreadTotal)
})

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
    } catch {
    }
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.layout-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 64px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
  cursor: pointer;
}

.header-menu {
  flex: 1;
  margin-left: 40px;
  border-bottom: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.message-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  transition: background-color 0.3s;
  color: #606266;
}

.message-container:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.ai-assistant-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  transition: background-color 0.3s;
  color: #606266;
}

.ai-assistant-container:hover {
  background-color: #f5f7fa;
  color: #67c23a;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #333;
}

.message-badge {
  margin-left: 8px;
}

.layout-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 20px;
}

.ai-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.ai-chat-history {
  height: 400px;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chat-bubble {
  max-width: 80%;
  padding: 10px 15px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

.chat-bubble.ai {
  align-self: flex-start;
  background-color: #fff;
  color: #333;
  border-bottom-left-radius: 2px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.chat-bubble.user {
  align-self: flex-end;
  background-color: #409eff;
  color: #fff;
  border-bottom-right-radius: 2px;
}

.ai-input-wrapper {
  padding: 10px 0;
}
</style>
