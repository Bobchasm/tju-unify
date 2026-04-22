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
        <div class="header-actions">
          <button
            type="button"
            class="header-action-btn"
            @click="openHistoryDrawer"
            title="查看服务端保存的历史会话并恢复"
          >
            历史
          </button>
          <button type="button" class="header-action-btn" @click="clearLocalChat" title="清空本地与服务端保存的对话">
            清空
          </button>
        </div>
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

    <!-- 历史对话：从本机 tian-agent 拉取已存档会话 -->
    <div v-show="historyDrawerOpen" class="history-root">
      <div class="history-mask" @click="closeHistoryDrawer"></div>
      <aside class="history-sheet" role="dialog" aria-label="历史对话">
        <div class="history-sheet-head">
          <span class="history-sheet-title">历史对话</span>
          <button type="button" class="history-close-btn" @click="closeHistoryDrawer">关闭</button>
        </div>
        <p class="history-hint">以下为当前小智服务（本机 8000 端口）已保存的会话，点一条即可恢复主界面记录。</p>
        <button
          v-if="sessionId"
          type="button"
          class="history-refresh-btn"
          :disabled="historyDetailLoading || isLoading"
          @click="refreshCurrentFromServer"
        >
          {{ historyDetailLoading ? '加载中…' : '刷新当前会话' }}
        </button>
        <div v-if="historyListLoading" class="history-state">加载列表…</div>
        <div v-else-if="sessionsError" class="history-state history-state--error">{{ sessionsError }}</div>
        <ul v-else class="history-list" :class="{ 'history-list--busy': historyDetailLoading }">
          <li v-if="chatSessions.length === 0" class="history-empty">暂无存档。请先与小智对话至少一轮（流式结束后会自动保存）。</li>
          <li
            v-for="row in chatSessions"
            :key="row.session_id"
            class="history-item"
            :class="{ 'history-item--active': row.session_id === sessionId }"
            @click="applySessionFromServer(row.session_id)"
          >
            <div class="history-item-preview">{{ row.preview || '（无预览）' }}</div>
            <div class="history-item-meta">
              <span>{{ formatSessionTime(row.updated_at) }}</span>
              <span>{{ row.message_count }} 条</span>
              <span v-if="row.session_id === sessionId" class="history-item-badge">当前</span>
            </div>
          </li>
        </ul>
      </aside>
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
import { memoAgentSnapshot } from '../api/memo'

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

const historyDrawerOpen = ref(false)
const chatSessions = ref([])
const historyListLoading = ref(false)
const historyDetailLoading = ref(false)
const sessionsError = ref('')

function formatSessionTime(tsMs) {
  if (tsMs == null || Number.isNaN(Number(tsMs))) return ''
  try {
    return new Date(Number(tsMs)).toLocaleString()
  } catch {
    return ''
  }
}

async function openHistoryDrawer() {
  historyDrawerOpen.value = true
  historyListLoading.value = true
  sessionsError.value = ''
  chatSessions.value = []
  try {
    const data = await agentApi.fetchChatSessions()
    chatSessions.value = Array.isArray(data.sessions) ? data.sessions : []
  } catch (e) {
    console.warn(e)
    sessionsError.value = '无法连接小智服务，请确认已启动 tian-agent（localhost:8000）。'
  } finally {
    historyListLoading.value = false
  }
}

function closeHistoryDrawer() {
  historyDrawerOpen.value = false
}

async function refreshCurrentFromServer() {
  const sid = sessionId.value
  if (!sid) return
  historyDetailLoading.value = true
  try {
    const data = await agentApi.fetchChatHistory(sid)
    const list = data && Array.isArray(data.messages) ? data.messages : []
    if (list.length > 0 && applyServerMessages(list)) {
      sessionId.value = data.session_id || sid
      persistChat()
      closeHistoryDrawer()
      scrollToBottom()
    } else {
      window.alert('服务端暂无该会话的完整存档。')
    }
  } catch (e) {
    console.warn(e)
    window.alert('拉取历史失败，请检查网络与小智服务。')
  } finally {
    historyDetailLoading.value = false
  }
}

async function applySessionFromServer(sid) {
  if (!sid || isLoading.value || historyDetailLoading.value) return
  historyDetailLoading.value = true
  try {
    const data = await agentApi.fetchChatHistory(sid)
    const list = data && Array.isArray(data.messages) ? data.messages : []
    if (list.length > 0 && applyServerMessages(list)) {
      sessionId.value = data.session_id || sid
      try {
        localStorage.setItem(LS_SID, sessionId.value)
      } catch (e) {
        console.warn(e)
      }
      persistChat()
      closeHistoryDrawer()
      scrollToBottom()
    } else {
      window.alert('该会话暂无消息存档。')
    }
  } catch (e) {
    console.warn(e)
    window.alert('加载该会话失败。')
  } finally {
    historyDetailLoading.value = false
  }
}

const LS_MSG = 'xiaozhi_chat_messages_v1'
const LS_SID = 'xiaozhi_session_id_v1'

function persistChat() {
  try {
    const slim = messages.value.map(({ role, content }) => ({ role, content }))
    localStorage.setItem(LS_MSG, JSON.stringify(slim))
    if (sessionId.value) {
      localStorage.setItem(LS_SID, sessionId.value)
    }
  } catch (e) {
    console.warn('保存对话失败', e)
  }
}

function loadChatFromStorage() {
  try {
    const sid = localStorage.getItem(LS_SID)
    const raw = localStorage.getItem(LS_MSG)
    if (!raw) return false
    const arr = JSON.parse(raw)
    if (!Array.isArray(arr) || arr.length === 0) return false
    const ok = arr.every(
      (m) => m && (m.role === 'user' || m.role === 'assistant') && typeof m.content === 'string'
    )
    if (!ok) return false
    messages.value = arr.map(({ role, content }) => ({ role, content }))
    if (sid) sessionId.value = sid
    return true
  } catch {
    return false
  }
}

async function clearLocalChat() {
  if (!window.confirm('清空本地与服务端保存的对话？（不影响备忘录）')) return
  const sid = sessionId.value
  if (sid) {
    try {
      await agentApi.deleteChatHistory(sid)
    } catch (e) {
      console.warn('删除服务端对话历史失败', e)
    }
  }
  localStorage.removeItem(LS_MSG)
  localStorage.removeItem(LS_SID)
  sessionId.value = null
  messages.value = [{ role: 'assistant', content: DEFAULT_ASSISTANT_OPENING }]
  if (auth.isAuthenticated()) {
    memoAgentSnapshot()
      .then((res) => {
        if (res && res.success && res.data) {
          messages.value[0].content = buildWarmOpeningFromSnapshot(res.data)
        }
      })
      .catch(() => {})
  }
}

const DEFAULT_ASSISTANT_OPENING =
  '你好，我是小智，天津大学校园生活助手。我可以结合学校公开资料，为你解答校园办事、校史校情、学习生活等常见问题。请问今天想了解什么？'

function buildWarmOpeningFromSnapshot(s) {
  if (!s) return DEFAULT_ASSISTANT_OPENING
  const parts = []
  parts.push('你好，我是小智～我又来啦！')
  if (s.weekCompletedTasks > 0) {
    parts.push(
      `看你上周在备忘录里勾完成了 **${s.weekCompletedTasks}** 个子任务，太厉害啦，继续加油！`
    )
  } else {
    parts.push('新的一周也可以试试用备忘录拆成小任务，完成一项勾一项，会很有成就感～')
  }
  if (s.weekUpdatedMemos > 0) {
    parts.push(`最近一周你更新了 **${s.weekUpdatedMemos}** 条备忘，有在认真安排生活呢。`)
  }
  if (s.pendingReminders > 0 && s.upcomingReminderTitle) {
    const when = s.upcomingReminderAt ? `（${s.upcomingReminderAt}）` : ''
    parts.push(`接下来还有提醒：「**${s.upcomingReminderTitle}**」${when}，到点别忘啦。`)
  }
  if (Array.isArray(s.pinnedTitles) && s.pinnedTitles.length) {
    parts.push(`置顶备忘：**${s.pinnedTitles.slice(0, 4).join('、')}**。`)
  }
  parts.push(
    '想查校务、二手，或让我「提醒你周五下午三点去取快递、顺便买牛奶」这种话，直接告诉我就行，我会帮你记进备忘录～'
  )
  return parts.join('\n\n')
}

const messages = ref([{ role: 'assistant', content: DEFAULT_ASSISTANT_OPENING }])

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

function applyServerMessages(msgs) {
  if (!Array.isArray(msgs) || msgs.length === 0) return false
  const ok = msgs.every(
    (m) => m && (m.role === 'user' || m.role === 'assistant') && typeof m.content === 'string'
  )
  if (!ok) return false
  messages.value = msgs.map(({ role, content }) => ({ role, content }))
  return true
}

onMounted(async () => {
  scrollToBottom()
  window.addEventListener('scroll', handleScroll)
  let restored = false
  const sidFromLs = (() => {
    try {
      return localStorage.getItem(LS_SID)
    } catch {
      return null
    }
  })()
  if (sidFromLs) {
    try {
      const data = await agentApi.fetchChatHistory(sidFromLs)
      const list = data && Array.isArray(data.messages) ? data.messages : []
      if (list.length > 0 && applyServerMessages(list)) {
        sessionId.value = data.session_id || sidFromLs
        persistChat()
        restored = true
      }
    } catch (e) {
      console.warn('从服务端加载对话历史失败，尝试本地缓存', e)
    }
  }
  if (!restored) {
    restored = loadChatFromStorage()
  }
  if (!restored && auth.isAuthenticated()) {
    try {
      const res = await memoAgentSnapshot()
      if (res && res.success && res.data) {
        messages.value[0].content = buildWarmOpeningFromSnapshot(res.data)
      }
    } catch (e) {
      console.warn('备忘快照加载失败，使用默认开场', e)
    }
  }
  scrollToBottom()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
  persistChat()
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
        persistChat()
      },
      (error) => {
        console.error('发送消息失败:', error)
        messages.value[assistantMessageIndex].content = '抱歉，我遇到了一些问题，请稍后再试。'
        isLoading.value = false
        persistChat()
      },
      (sid) => {
        if (sid) {
          sessionId.value = sid
          localStorage.setItem(LS_SID, sid)
        }
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

.chat-page header .header-actions {
  display: flex;
  align-items: center;
  gap: 1.5vw;
  flex-shrink: 0;
}

.chat-page header .header-action-btn {
  min-width: 10vw;
  padding: 1.2vw 2.2vw;
  border: none;
  border-radius: 2vw;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  font-size: 2.8vw;
  cursor: pointer;
  white-space: nowrap;
}

.chat-page header .header-action-btn:active {
  background: rgba(255, 255, 255, 0.35);
}

/****************** 历史对话抽屉 ******************/
.chat-page .history-root {
  position: fixed;
  inset: 0;
  z-index: 1000;
  pointer-events: auto;
}

.chat-page .history-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
}

.chat-page .history-sheet {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  max-height: 78vh;
  background: #fff;
  border-radius: 4vw 4vw 0 0;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.chat-page .history-sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 3.5vw 4vw;
  border-bottom: 1px solid #eee;
}

.chat-page .history-sheet-title {
  font-size: 4vw;
  font-weight: 600;
  color: #222;
}

.chat-page .history-close-btn {
  border: none;
  background: #f0f4f8;
  color: #3a7bd5;
  font-size: 3.2vw;
  padding: 1.5vw 3.5vw;
  border-radius: 2vw;
  cursor: pointer;
}

.chat-page .history-hint {
  margin: 0;
  padding: 2.5vw 4vw;
  font-size: 3vw;
  color: #666;
  line-height: 1.45;
  background: #fafbfc;
  border-bottom: 1px solid #eee;
}

.chat-page .history-refresh-btn {
  margin: 2.5vw 4vw 0;
  align-self: flex-start;
  border: none;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: #fff;
  font-size: 3.2vw;
  padding: 2vw 4vw;
  border-radius: 2vw;
  cursor: pointer;
}

.chat-page .history-refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.chat-page .history-state {
  padding: 5vw 4vw;
  font-size: 3.4vw;
  color: #666;
  text-align: center;
}

.chat-page .history-state--error {
  color: #c0392b;
}

.chat-page .history-list {
  list-style: none;
  margin: 0;
  padding: 0 0 4vw;
  overflow-y: auto;
  flex: 1;
  -webkit-overflow-scrolling: touch;
}

.chat-page .history-list--busy {
  opacity: 0.55;
  pointer-events: none;
}

.chat-page .history-empty {
  padding: 6vw 4vw;
  font-size: 3.2vw;
  color: #888;
  text-align: center;
}

.chat-page .history-item {
  padding: 3.5vw 4vw;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.chat-page .history-item:active {
  background: #f5f9fc;
}

.chat-page .history-item--active {
  background: #eef6ff;
}

.chat-page .history-item-preview {
  font-size: 3.5vw;
  color: #333;
  line-height: 1.4;
  word-break: break-word;
}

.chat-page .history-item-meta {
  margin-top: 1.5vw;
  display: flex;
  flex-wrap: wrap;
  gap: 2vw;
  font-size: 2.8vw;
  color: #999;
}

.chat-page .history-item-badge {
  color: #3a7bd5;
  font-weight: 600;
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
  
  .chat-page header .header-action-btn {
    min-width: 44px;
    padding: 6px 12px;
    font-size: 14px;
    border-radius: 8px;
  }

  .chat-page .history-sheet {
    max-height: 70vh;
    border-radius: 16px 16px 0 0;
  }

  .chat-page .history-sheet-title {
    font-size: 18px;
  }

  .chat-page .history-close-btn {
    font-size: 14px;
    padding: 6px 14px;
    border-radius: 8px;
  }

  .chat-page .history-hint {
    font-size: 13px;
    padding: 10px 16px;
  }

  .chat-page .history-refresh-btn {
    font-size: 14px;
    margin: 10px 16px 0;
    padding: 8px 16px;
    border-radius: 8px;
  }

  .chat-page .history-state {
    font-size: 14px;
    padding: 20px 16px;
  }

  .chat-page .history-empty {
    font-size: 14px;
    padding: 24px 16px;
  }

  .chat-page .history-item {
    padding: 14px 16px;
  }

  .chat-page .history-item-preview {
    font-size: 15px;
  }

  .chat-page .history-item-meta {
    font-size: 12px;
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