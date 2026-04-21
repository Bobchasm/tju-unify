<template>
  <div class="chat-page">
    <div class="chat-header">
      <button class="back-btn" @click="goBack">←</button>
      <div class="chat-title">小智助手</div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message" :class="msg.role">
        <div class="message-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
        <div class="message-content" :class="{ 'message-content--assistant': msg.role === 'assistant' }">
          <template v-if="msg.role === 'assistant'">
            <div class="md-body" v-html="assistantHtml(msg.content)"></div>
          </template>
          <template v-else>
            <div class="plain-body">{{ msg.content }}</div>
          </template>
          <span v-if="isLoading && index === messages.length - 1 && msg.role === 'assistant'" class="cursor-blink"></span>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <input
        type="text"
        class="chat-input"
        v-model="inputMessage"
        placeholder="请输入您的问题..."
        @keyup.enter="sendMessage"
        :disabled="isLoading"
      />
      <button class="send-btn" @click="sendMessage" :disabled="isLoading || !inputMessage.trim()">
        发送
      </button>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goHome">
        <div class="nav-icon">🏠</div>
        <div>首页</div>
      </div>
      <div class="nav-item active">
        <div class="nav-icon">💬</div>
        <div>小智</div>
      </div>
      <div class="nav-item" @click="goToUser">
        <div class="nav-icon">👤</div>
        <div>我的</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import agentApi from '../api/agent'
import auth from '../api/auth'

marked.use({
  gfm: true,
  breaks: true
})

/** 助手消息转安全 HTML（Markdown），支持粗体、列表、换行等 */
function assistantHtml(text) {
  const raw = text || ''
  if (!raw.trim()) return ''
  try {
    const unsafe = marked.parse(raw, { async: false })
    return DOMPurify.sanitize(unsafe, {
      ALLOWED_TAGS: [
        'p', 'br', 'strong', 'em', 'u', 's', 'h1', 'h2', 'h3', 'h4', 'blockquote', 'code', 'pre',
        'ul', 'ol', 'li', 'a', 'hr', 'table', 'thead', 'tbody', 'tr', 'th', 'td'
      ],
      ALLOWED_ATTR: ['href', 'title', 'target', 'rel']
    })
  } catch {
    return DOMPurify.sanitize(raw.replace(/\n/g, '<br/>'))
  }
}

const router = useRouter()
const messagesContainer = ref(null)
const inputMessage = ref('')
const isLoading = ref(false)
const sessionId = ref(null)
const isLoggedIn = computed(() => auth.isAuthenticated())

const messages = ref([
  { role: 'assistant', content: '你好，我是小智，天津大学校园生活助手。我可以结合学校公开资料，为你解答校园办事、校史校情、学习生活等常见问题。请问今天想了解什么？' }
])

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  scrollToBottom()
})

const goBack = () => {
  router.push('/')
}

const goHome = () => {
  router.push('/')
}

const goToMarket = () => {
  router.push('/market')
}

const goToUser = () => {
  if (isLoggedIn.value) {
    router.push('/profile')
  } else {
    router.push('/login')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) {
    return
  }

  const userMessage = inputMessage.value.trim()
  messages.value.push({ role: 'user', content: userMessage })
  inputMessage.value = ''
  isLoading.value = true
  
  // 添加空的助手消息用于流式填充
  const assistantMessageIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })
  scrollToBottom()

  try {
    await agentApi.sendMessageStream(
      messages.value.slice(0, -1), // 不包含刚添加的空消息
      sessionId.value,
      // 接收chunk回调
      (chunk) => {
        messages.value[assistantMessageIndex].content += chunk
        scrollToBottom()
      },
      // 完成回调
      () => {
        isLoading.value = false
      },
      // 错误回调
      (error) => {
        console.error('发送消息失败:', error)
        messages.value[assistantMessageIndex].content = '抱歉，我遇到了一些问题，请稍后再试。'
        isLoading.value = false
      }
    )
  } catch (error) {
    console.error('发送消息失败:', error)
    messages.value[messages.value.length - 1].content = '抱歉，我遇到了一些问题，请稍后再试。'
    isLoading.value = false
  }
}
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.back-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-size: 20px;
  padding: 8px;
  border-radius: 50%;
  cursor: pointer;
}

.chat-title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.chat-messages {
  flex: 1;
  padding: 80px 16px 100px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  max-width: 75%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  word-break: break-word;
}

.message-content--assistant .md-body {
  font-size: 15px;
  line-height: 1.65;
  color: #1a1a1a;
}

.message-content--assistant .md-body :deep(p) {
  margin: 0 0 0.65em;
}

.message-content--assistant .md-body :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content--assistant .md-body :deep(ul),
.message-content--assistant .md-body :deep(ol) {
  margin: 0.4em 0 0.65em;
  padding-left: 1.35em;
}

.message-content--assistant .md-body :deep(li) {
  margin: 0.25em 0;
}

.message-content--assistant .md-body :deep(strong) {
  font-weight: 600;
  color: #111;
}

.message-content--assistant .md-body :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8e8;
  margin: 0.75em 0;
}

.message-content--assistant .md-body :deep(blockquote) {
  margin: 0.5em 0;
  padding-left: 0.75em;
  border-left: 3px solid #667eea;
  color: #555;
}

.plain-body {
  white-space: pre-wrap;
}

.message.user .message-content {
  background: #667eea;
  color: #fff;
}

.cursor-blink {
  animation: blink 1s infinite;
  display: inline-block;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.chat-input-area {
  position: fixed;
  bottom: 80px;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: white;
  display: flex;
  gap: 12px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  font-size: 15px;
}

.send-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
