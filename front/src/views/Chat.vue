<template>
  <div class="chat-page">
    <!-- header部分 - 统一蓝色渐变风格 -->
    <header>
      <div class="header-content">
        <div class="back-btn" @click="goBack">
          <span>←</span>
        </div>
        <div class="chat-title">
          <span class="title-icon">🤖</span>
          <span>小智助手</span>
        </div>
        <div class="placeholder"></div>
      </div>
      <p class="subtitle">校园生活智能助手，随时为您解答</p>
    </header>

    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message" :class="msg.role">
        <div class="message-avatar">
          {{ msg.role === 'user' ? '👤' : '🤖' }}
        </div>
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

    <!-- 输入区域 -->
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
        <p>发送</p>
      </button>
    </div>

    <!-- 返回顶部悬浮按钮 -->
    <div class="back-top-fab" @click="scrollToTop" v-show="showBackTop">
      <span>↑</span>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount, computed } from 'vue'
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
const showBackTop = ref(false)
const isLoggedIn = computed(() => auth.isAuthenticated())

const messages = ref([
  { role: 'assistant', content: '你好，我是小智，天津大学校园生活助手。我可以结合学校公开资料，为你解答校园办事、校史校情、学习生活等常见问题。请问今天想了解什么？' }
])

// 监听滚动显示返回顶部按钮
const handleScroll = () => {
  showBackTop.value = window.scrollY > 300
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  scrollToBottom()
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})

const goBack = () => {
  router.push('/')
}

const goToUser = () => {
  if (isLoggedIn.value) {
    router.push('/profile')
  } else {
    router.push('/trade/login')
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
      messages.value.slice(0, -1),
      sessionId.value,
      (chunk) => {
        messages.value[assistantMessageIndex].content += chunk
        scrollToBottom()
      },
      () => {
        isLoading.value = false
      },
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
/****************** 全局样式 ******************/
.chat-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 0;
  padding-bottom: 14vw;  /* 为底部导航栏留出空间 */
}

/****************** header部分 - 统一蓝色渐变 ******************/
.chat-page header {
  width: 100%;
  margin: 0;
  border-radius: 0;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  padding: 5vw 4vw 3vw;
  box-sizing: border-box;
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.3);
}

.chat-page header .header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-page header .back-btn {
  width: 10vw;
  height: 10vw;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chat-page header .back-btn span {
  font-size: 6vw;
  color: white;
  font-weight: bold;
}

.chat-page header .back-btn:active {
  transform: scale(0.95);
  background: rgba(255, 255, 255, 0.3);
}

.chat-page header .chat-title {
  display: flex;
  align-items: center;
  gap: 2vw;
}

.chat-page header .chat-title .title-icon {
  font-size: 6vw;
}

.chat-page header .chat-title span:last-child {
  font-size: 5vw;
  font-weight: bold;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chat-page header .placeholder {
  width: 10vw;
}

.chat-page header .subtitle {
  font-size: 3vw;
  color: rgba(255, 255, 255, 0.85);
  margin: 2vw 0 0 0;
  text-align: center;
}

/****************** 聊天消息区域 ******************/
.chat-messages {
  padding: 4vw;
  display: flex;
  flex-direction: column;
  gap: 4vw;
  overflow-y: auto;
  min-height: 60vh;
}

.message {
  display: flex;
  gap: 3vw;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 10vw;
  height: 10vw;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 5vw;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message-content {
  background: white;
  padding: 3vw 4vw;
  border-radius: 3vw;
  font-size: 3.8vw;
  line-height: 1.5;
  max-width: 70%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  word-break: break-word;
}

.message.user .message-content {
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
}

.message-content--assistant .md-body {
  font-size: 3.8vw;
  line-height: 1.6;
  color: #333;
}

.message-content--assistant .md-body :deep(p) {
  margin: 0 0 1em;
}

.message-content--assistant .md-body :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content--assistant .md-body :deep(ul),
.message-content--assistant .md-body :deep(ol) {
  margin: 0.5em 0 1em;
  padding-left: 1.5em;
}

.message-content--assistant .md-body :deep(li) {
  margin: 0.3em 0;
}

.message-content--assistant .md-body :deep(strong) {
  font-weight: 600;
  color: #3a7bd5;
}

.message-content--assistant .md-body :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8e8;
  margin: 1em 0;
}

.message-content--assistant .md-body :deep(blockquote) {
  margin: 0.5em 0;
  padding-left: 1em;
  border-left: 0.5vw solid #3a7bd5;
  color: #666;
}

.plain-body {
  white-space: pre-wrap;
}

.cursor-blink {
  display: inline-block;
  width: 2vw;
  height: 4vw;
  background-color: #3a7bd5;
  animation: blink 1s infinite;
  margin-left: 0.5vw;
  vertical-align: middle;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/****************** 输入区域 ******************/
.chat-input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 3vw 4vw;
  padding-bottom: calc(3vw + env(safe-area-inset-bottom, 0));
  background: white;
  display: flex;
  gap: 3vw;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  z-index: 99;
}

.chat-input {
  flex: 1;
  padding: 3vw 4vw;
  border: 1px solid #e0e0e0;
  border-radius: 8vw;
  font-size: 3.8vw;
  outline: none;
  transition: all 0.3s ease;
}

.chat-input:focus {
  border-color: #3a7bd5;
  box-shadow: 0 0 0 2px rgba(58, 123, 213, 0.2);
}

.send-btn {
  padding: 3vw 8vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
  border: none;
  border-radius: 4vw;
  font-size: 3.5vw;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  flex-direction: row;      /* 强制横向排列 */
  align-items: center;
  justify-content: center;  /* 居中 */
  gap: 1.5vw;
  white-space: nowrap;      /* 防止文字换行 */
  transition: all 0.3s ease;
}

.send-btn:active {
  transform: scale(0.95);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/****************** 返回顶部悬浮按钮 ******************/
.chat-page .back-top-fab {
  position: fixed;
  right: 5vw;
  bottom: 20vw;
  width: 12vw;
  height: 12vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.4);
  cursor: pointer;
  z-index: 99;
  transition: all 0.2s ease;
}

.chat-page .back-top-fab:active {
  transform: scale(0.92);
}

.chat-page .back-top-fab span {
  color: white;
  font-size: 6vw;
  font-weight: bold;
}

/****************** 响应式适配 - 大屏幕 ******************/
@media (min-width: 768px) {
  .chat-page {
    padding-bottom: 70px;  /* PC端底部导航栏高度 */
  }
  
  .chat-page header .back-btn {
    width: 44px;
    height: 44px;
  }
  
  .chat-page header .back-btn span {
    font-size: 24px;
  }
  
  .chat-page header .chat-title .title-icon {
    font-size: 28px;
  }
  
  .chat-page header .chat-title span:last-child {
    font-size: 22px;
  }
  
  .chat-page header .placeholder {
    width: 44px;
  }
  
  .chat-page header .subtitle {
    font-size: 14px;
  }
  
  .message-avatar {
    width: 44px;
    height: 44px;
    font-size: 22px;
  }
  
  .message-content {
    font-size: 16px;
    padding: 12px 18px;
  }
  
  .message-content--assistant .md-body {
    font-size: 16px;
  }
  
  .chat-input {
    padding: 12px 18px;
    font-size: 16px;
  }
  
  .send-btn {
    padding: 12px 24px;
    font-size: 14px;
    border-radius: 20px;  /* PC端对应圆角矩形 */
  }
  
  .chat-page .back-top-fab {
    width: 50px;
    height: 50px;
    bottom: 100px;
  }
  
  .chat-page .back-top-fab span {
    font-size: 24px;
  }
}
</style>