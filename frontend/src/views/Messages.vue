<template>
  <div class="messages-container">
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card class="chat-list-card">
          <template #header>
            <div class="card-header">
              <span>消息列表</span>
              <el-badge :value="3" class="badge">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </div>
          </template>
          <div class="chat-list">
            <div
              v-for="item in chatList"
              :key="item.id"
              class="chat-item"
              :class="{ active: currentChatId === item.id }"
              @click="selectChat(item)"
            >
              <el-avatar :size="48">{{ item.name.charAt(0) }}</el-avatar>
              <div class="chat-info">
                <div class="chat-name">
                  {{ item.name }}
                  <el-badge v-if="item.unread" :value="item.unread" type="danger" class="unread-badge" />
                </div>
                <div class="chat-last-msg">{{ item.lastMsg }}</div>
              </div>
              <div class="chat-time">{{ item.time }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="chat-detail-card" v-if="currentChat">
          <template #header>
            <div class="card-header">
              <span>{{ currentChat.name }}</span>
            </div>
          </template>
          <div class="chat-messages" ref="messagesRef">
            <div v-for="msg in currentChat.messages" :key="msg.id" class="message-item" :class="msg.type">
              <div class="message-content">
                <div class="message-bubble">{{ msg.content }}</div>
                <div class="message-time">{{ msg.time }}</div>
              </div>
              <el-avatar :size="36">{{ msg.type === 'self' ? '我' : currentChat.name.charAt(0) }}</el-avatar>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息..."
              @keyup.enter.ctrl="sendMessage"
            />
            <div class="input-actions">
              <el-button text><el-icon><Picture /></el-icon></el-button>
              <el-button type="primary" @click="sendMessage">发送</el-button>
            </div>
          </div>
        </el-card>
        <el-empty v-else description="选择一个聊天开始对话" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'

const messagesRef = ref(null)
const currentChatId = ref(null)
const inputMessage = ref('')

const chatList = ref([
  {
    id: 1,
    name: '张三',
    lastMsg: '好的，谢谢！',
    time: '14:30',
    unread: 2,
    messages: [
      { id: 1, type: 'other', content: '你好，请问是你捡到我的钱包了吗？', time: '14:20' },
      { id: 2, type: 'self', content: '是的，你描述一下钱包的特征', time: '14:22' },
      { id: 3, type: 'other', content: '黑色的，里面有身份证和银行卡', time: '14:25' },
      { id: 4, type: 'self', content: '对的，那明天我们在食堂门口见吧', time: '14:28' },
      { id: 5, type: 'other', content: '好的，谢谢！', time: '14:30' }
    ]
  },
  {
    id: 2,
    name: '李四',
    lastMsg: '请问在哪里可以认领？',
    time: '昨天',
    unread: 1,
    messages: [
      { id: 1, type: 'other', content: '你好，我看到你发布的捡到校园卡的信息', time: '昨天 16:00' },
      { id: 2, type: 'other', content: '请问在哪里可以认领？', time: '昨天 16:05' }
    ]
  },
  {
    id: 3,
    name: '王五',
    lastMsg: '好的，我知道了',
    time: '前天',
    unread: 0,
    messages: [
      { id: 1, type: 'self', content: '你好，请问你丢的耳机是什么颜色的？', time: '前天 10:00' },
      { id: 2, type: 'other', content: '白色的', time: '前天 10:05' },
      { id: 3, type: 'self', content: '好的，我知道了', time: '前天 10:10' }
    ]
  }
])

const currentChat = ref(null)

const selectChat = (item) => {
  currentChatId.value = item.id
  currentChat.value = item
  item.unread = 0
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || !currentChat.value) return
  const newMsg = {
    id: Date.now(),
    type: 'self',
    content: inputMessage.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  currentChat.value.messages.push(newMsg)
  currentChat.value.lastMsg = inputMessage.value
  currentChat.value.time = newMsg.time
  inputMessage.value = ''
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  selectChat(chatList.value[0])
})
</script>

<style scoped>
.messages-container {
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - 140px);
}

.chat-list-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-list {
  height: calc(100% - 60px);
  overflow-y: auto;
}

.chat-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.chat-item:hover {
  background-color: #f5f7fa;
}

.chat-item.active {
  background-color: #ecf5ff;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-name {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-last-msg {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 12px;
  color: #c0c4cc;
}

.unread-badge {
  margin-left: auto;
}

.chat-detail-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 60%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.message-item.other .message-bubble {
  background-color: #fff;
  border: 1px solid #e4e7ed;
}

.message-item.self .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
  text-align: right;
}

.message-item.self .message-time {
  text-align: left;
}

.chat-input {
  border-top: 1px solid #e4e7ed;
  padding: 16px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}
</style>
