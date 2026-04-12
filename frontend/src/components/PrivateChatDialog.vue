<template>
  <el-dialog
    v-model="visible"
    :title="`与 ${peerName || '对方'} 私聊`"
    width="520px"
    destroy-on-close
    class="private-chat-dialog"
    @closed="handleClosed"
  >
    <div ref="listRef" class="msg-list">
      <div
        v-for="m in messages"
        :key="m.id"
        class="msg-row"
        :class="{ mine: m.senderId === myUserId }"
      >
        <div class="msg-bubble">
          <div class="msg-meta">
            {{ m.senderId === myUserId ? '我' : '对方' }} · {{ formatTime(m.createTime) }}
          </div>
          <div class="msg-text">{{ m.content }}</div>
        </div>
      </div>
      <el-empty v-if="!loading && messages.length === 0" description="暂无消息，发送第一条吧" :image-size="64" />
    </div>
    <div class="msg-input-row">
      <el-input
        v-model="draft"
        type="textarea"
        :rows="2"
        maxlength="2000"
        show-word-limit
        placeholder="输入消息，Enter 发送"
        @keydown.enter.exact.prevent="send"
      />
      <el-button type="primary" class="send-btn" @click="send">发送</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { privateMessageApi } from '@/api/privateMessage'
import { useUserStore } from '@/stores/user'
import { sendChatPayload } from '@/utils/chatSocket'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  sessionId: { type: String, default: '' },
  peerName: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const userStore = useUserStore()
const myUserId = computed(() => userStore.userInfo?.id)

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const messages = ref([])
const draft = ref('')
const loading = ref(false)
const listRef = ref(null)

function onWsChat(ev) {
  const data = ev.detail
  if (!data || !props.modelValue || !props.sessionId) return
  if (data.type === 'newPrivateMessage' && data.message) {
    const msg = data.message
    if (msg.sessionId === props.sessionId) {
      const exists = messages.value.some((x) => x.id === msg.id)
      if (!exists) {
        messages.value.push(msg)
        scrollBottom()
      }
      if (msg.receiverId === myUserId.value) {
        privateMessageApi.markRead(props.sessionId).then(() => {
          window.dispatchEvent(new CustomEvent('refresh-unread'))
        })
      }
    }
  }
  if (data.type === 'error' && data.message) {
    ElMessage.warning(data.message)
  }
}

function scrollBottom() {
  nextTick(() => {
    const el = listRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function loadHistory() {
  if (!props.sessionId) return
  loading.value = true
  privateMessageApi
    .getHistory(props.sessionId)
    .then((res) => {
      messages.value = res.data || []
      scrollBottom()
    })
    .finally(() => {
      loading.value = false
    })
}

function markRead() {
  if (!props.sessionId) return
  privateMessageApi.markRead(props.sessionId).then(() => {
    window.dispatchEvent(new CustomEvent('refresh-unread'))
  })
}

function bindWsListener() {
  window.addEventListener('ws-chat', onWsChat)
}

function unbindWsListener() {
  window.removeEventListener('ws-chat', onWsChat)
}

function send() {
  const text = draft.value.trim()
  if (!text || !props.sessionId) return
  if (!sendChatPayload({ type: 'send', sessionId: props.sessionId, content: text })) {
    ElMessage.warning('实时连接未就绪，请稍候再试或刷新页面')
    return
  }
  draft.value = ''
}

function handleClosed() {
  unbindWsListener()
  messages.value = []
  draft.value = ''
}

watch(
  () => [props.modelValue, props.sessionId],
  ([open, sid]) => {
    if (!open) {
      unbindWsListener()
      return
    }
    if (!sid) return
    unbindWsListener()
    loadHistory()
    markRead()
    bindWsListener()
  }
)

defineExpose({ loadHistory })
</script>

<style scoped>
.msg-list {
  max-height: 360px;
  overflow-y: auto;
  padding: 8px 4px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.msg-row {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
}

.msg-row.mine {
  justify-content: flex-end;
}

.msg-bubble {
  max-width: 78%;
  padding: 8px 12px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.msg-row.mine .msg-bubble {
  background: #ecf5ff;
}

.msg-meta {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.msg-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-input-row {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.send-btn {
  align-self: flex-end;
}
</style>
