<template>
  <el-dialog
    v-model="visible"
    :title="`与 ${peerName || '对方'} 私聊`"
    width="520px"
    destroy-on-close
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
      <el-empty v-if="!loading && messages.length === 0" description="暂无消息" :image-size="64" />
    </div>
    <div class="msg-input-row">
      <el-input
        v-model="draft"
        type="textarea"
        :rows="2"
        maxlength="2000"
        show-word-limit
        placeholder="输入内容后点发送"
      />
      <el-button type="primary" class="send-btn" :loading="sendLoading" @click="send">发送</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import { privateMessageApi } from '@/api/privateMessage'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  postId: { type: Number, default: null },
  postType: { type: String, default: '' },
  peerId: { type: Number, default: null },
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
const sendLoading = ref(false)
const listRef = ref(null)

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

function loadList() {
  if (!props.postId || !props.postType || !props.peerId) return
  loading.value = true
  privateMessageApi
    .list({
      postId: props.postId,
      postType: props.postType,
      peerId: props.peerId
    })
    .then((res) => {
      messages.value = res.data || []
      scrollBottom()
    })
    .finally(() => {
      loading.value = false
    })
}

function doMarkRead() {
  if (!props.postId || !props.postType || !props.peerId) return
  privateMessageApi
    .markRead({
      postId: props.postId,
      postType: props.postType,
      peerId: props.peerId
    })
    .then(() => {
      window.dispatchEvent(new CustomEvent('refresh-unread'))
    })
}

function onWsChat(ev) {
  const d = ev.detail
  if (!d || d.type !== 'newPm') return
  if (!props.modelValue) return
  if (d.postId !== props.postId || d.postType !== props.postType) return
  loadList()
  doMarkRead()
}

function handleClosed() {
  window.removeEventListener('ws-chat', onWsChat)
  messages.value = []
  draft.value = ''
}

function send() {
  const text = draft.value.trim()
  if (!text || !props.postId || !props.postType || !props.peerId) return
  sendLoading.value = true
  privateMessageApi
    .send({
      postId: props.postId,
      postType: props.postType,
      receiverId: props.peerId,
      content: text
    })
    .then(() => {
      draft.value = ''
      loadList()
    })
    .finally(() => {
      sendLoading.value = false
    })
}

watch(
  () => [props.modelValue, props.postId, props.postType, props.peerId],
  () => {
    if (!props.modelValue) {
      window.removeEventListener('ws-chat', onWsChat)
      return
    }
    if (!props.postId || !props.postType || !props.peerId) return
    window.removeEventListener('ws-chat', onWsChat)
    window.addEventListener('ws-chat', onWsChat)
    loadList()
    doMarkRead()
  }
)
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
