<template>
  <div class="ai-chat-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <button @click="goBack" class="back-btn">
          <i class="fa fa-arrow-left"></i>
        </button>
        <div class="header-title">
          <h1>AI智能客服</h1>
          <p>小饿随时为您服务</p>
        </div>
      </div>
      <div class="header-right">
        <div class="ai-status" :class="aiStatus.type">
          <span class="status-dot"></span>
          {{ aiStatus.text }}
        </div>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-area">
      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-section">
          <div class="welcome-card">
            <div class="welcome-icon">🤖</div>
            <h2>您好！我是小饿AI助手</h2>
            <p>我可以帮助您解决各种外卖相关问题</p>
            
            <div class="service-features">
              <div class="feature-card">
                <i class="fa fa-search"></i>
                <h3>美食推荐</h3>
                <p>根据您的喜好推荐美食和商家</p>
              </div>
              <div class="feature-card">
                <i class="fa fa-list-alt"></i>
                <h3>订单查询</h3>
                <p>查询订单状态和配送信息</p>
              </div>
              <div class="feature-card">
                <i class="fa fa-gift"></i>
                <h3>优惠活动</h3>
                <p>获取最新的优惠券和活动信息</p>
              </div>
              <div class="feature-card">
                <i class="fa fa-question-circle"></i>
                <h3>使用帮助</h3>
                <p>解答平台使用相关问题</p>
              </div>
            </div>

            <!-- 快捷问题 -->
            <div class="quick-questions">
              <h3>常见问题</h3>
              <div class="question-grid">
                <button
                  v-for="question in quickQuestions"
                  :key="question"
                  @click="askQuickQuestion(question)"
                  class="question-btn"
                >
                  {{ question }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 对话消息 -->
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', message.type]"
        >
          <div class="message-avatar">
            <i :class="message.type === 'user' ? 'fa fa-user' : 'fa fa-robot'"></i>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div v-if="message.processingTime" class="message-meta">
                <i class="fa fa-clock-o"></i>
                处理时间: {{ message.processingTime }}ms
              </div>
            </div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>

        <!-- AI正在输入 -->
        <div v-if="isTyping" class="message ai typing">
          <div class="message-avatar">
            <i class="fa fa-robot"></i>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <div class="typing-text">小饿正在思考中...</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <div class="input-container">
          <div class="input-wrapper">
            <textarea
              v-model="inputMessage"
              @keydown="handleKeyDown"
              @input="onInputChange"
              placeholder="输入您的问题，按Enter发送..."
              :disabled="isTyping"
              ref="messageInput"
              rows="1"
              maxlength="500"
            ></textarea>
            <div class="input-footer">
              <div class="input-info">
                <span class="char-count">{{ inputMessage.length }}/500</span>
                <span v-if="detectedChatType !== 'general'" class="chat-type">
                  {{ getChatTypeText(detectedChatType) }}
                </span>
              </div>
              <div class="input-actions">
                <button @click="clearChat" class="action-btn" title="清空对话">
                  <i class="fa fa-trash"></i>
                </button>
                <button @click="showHistory = !showHistory" class="action-btn" title="历史记录">
                  <i class="fa fa-history"></i>
                </button>
              </div>
            </div>
          </div>
          <button
            @click="sendMessage"
            :disabled="!inputMessage.trim() || isTyping"
            class="send-btn"
          >
            <i class="fa fa-paper-plane"></i>
            <span>发送</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 历史记录侧边栏 -->
    <transition name="slide-left">
      <div v-if="showHistory" class="history-sidebar">
        <div class="sidebar-header">
          <h3>对话历史</h3>
          <button @click="showHistory = false" class="close-btn">
            <i class="fa fa-times"></i>
          </button>
        </div>
        <div class="sidebar-content">
          <button @click="loadChatHistory" class="refresh-btn" :disabled="loadingHistory">
            <i class="fa fa-refresh" :class="{ 'fa-spin': loadingHistory }"></i>
            刷新历史
          </button>
          
          <div v-if="chatHistory.length > 0" class="history-list">
            <div 
              v-for="history in chatHistory" 
              :key="history.id"
              class="history-item"
              @click="loadHistorySession(history.sessionId)"
            >
              <div class="history-content">
                <div class="history-message">{{ truncateText(history.userMessage, 50) }}</div>
                <div class="history-time">{{ formatTime(history.createTime) }}</div>
              </div>
              <button @click.stop="deleteHistory(history.id)" class="delete-btn">
                <i class="fa fa-trash"></i>
              </button>
            </div>
          </div>
          
          <div v-else class="history-empty">
            <i class="fa fa-comments-o"></i>
            <p>暂无对话历史</p>
          </div>
        </div>
      </div>
    </transition>

    <!-- 遮罩层 -->
    <div v-if="showHistory" class="overlay" @click="showHistory = false"></div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import aiChatService from '../services/aiChatService'

export default {
  name: 'AiChat',
  setup() {
    const router = useRouter()
    
    // 响应式数据
    const messages = ref([])
    const chatHistory = ref([])
    const inputMessage = ref('')
    const isTyping = ref(false)
    const loadingHistory = ref(false)
    const showHistory = ref(false)
    const messagesContainer = ref(null)
    const messageInput = ref(null)
    const currentSessionId = ref(null)
    const detectedChatType = ref('general')
    
    // AI状态
    const aiStatus = ref({
      type: 'checking',
      text: '检查中...'
    })

    // 快捷问题
    const quickQuestions = ref([
      '推荐附近的美食',
      '今天有什么优惠活动',
      '如何查看我的订单',
      '配送一般需要多长时间',
      '怎么联系商家',
      '如何申请退款',
      '会员有什么特权',
      '配送费怎么计算'
    ])

    // 返回上一页
    const goBack = () => {
      router.go(-1)
    }

    // 检查AI状态
    const checkAiStatus = async () => {
      try {
        const result = await aiChatService.healthCheck()
        if (result.success) {
          aiStatus.value = {
            type: 'online',
            text: '在线服务'
          }
        } else {
          aiStatus.value = {
            type: 'offline',
            text: '服务异常'
          }
        }
      } catch (error) {
        aiStatus.value = {
          type: 'offline',
          text: '连接失败'
        }
      }
    }

    // 清空对话
    const clearChat = () => {
      if (messages.value.length > 0 && confirm('确定要清空当前对话吗？')) {
        messages.value = []
        currentSessionId.value = null
      }
    }

    // 处理键盘事件
    const handleKeyDown = (event) => {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        sendMessage()
      }
    }

    // 输入变化处理
    const onInputChange = () => {
      // 自动调整textarea高度
      const textarea = messageInput.value
      if (textarea) {
        textarea.style.height = 'auto'
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
      }
      
      // 检测聊天类型
      if (inputMessage.value.trim()) {
        detectedChatType.value = aiChatService.detectChatType(inputMessage.value)
      } else {
        detectedChatType.value = 'general'
      }
    }

    // 发送消息
    const sendMessage = async () => {
      if (!inputMessage.value.trim() || isTyping.value) return

      const userMessage = {
        type: 'user',
        content: inputMessage.value.trim(),
        timestamp: new Date()
      }

      messages.value.push(userMessage)
      const userInput = inputMessage.value.trim()
      inputMessage.value = ''
      detectedChatType.value = 'general'
      
      // 重置textarea高度
      if (messageInput.value) {
        messageInput.value.style.height = 'auto'
      }

      await scrollToBottom()
      await getAIResponse(userInput)
    }

    // 快捷问题
    const askQuickQuestion = (question) => {
      inputMessage.value = question
      sendMessage()
    }

    // 获取AI回复
    const getAIResponse = async (userInput) => {
      isTyping.value = true

      try {
        // 模拟思考时间
        await new Promise(resolve => setTimeout(resolve, 800 + Math.random() * 1200))
        
        // 调用AI服务
        const result = await aiChatService.sendMessage(
          userInput, 
          detectedChatType.value,
          currentSessionId.value
        )
        
        if (result.success) {
          // 更新会话ID
          if (result.data.sessionId) {
            currentSessionId.value = result.data.sessionId
          }
          
          // 添加AI回复
          const aiMessage = {
            type: 'ai',
            content: result.data.message || '抱歉，没有收到有效回复',
            timestamp: new Date(),
            processingTime: result.data.processingTime,
            responseType: result.data.responseType
          }
          
          messages.value.push(aiMessage)
        } else {
          // 添加错误回复
          messages.value.push({
            type: 'ai',
            content: result.data.message || '服务器错误，请稍后重试',
            timestamp: new Date(),
            responseType: 'error'
          })
        }

      } catch (error) {
        console.error('AI回复失败:', error)
        messages.value.push({
          type: 'ai',
          content: '抱歉，我暂时无法回答您的问题。请稍后再试或联系人工客服。',
          timestamp: new Date(),
          responseType: 'error'
        })
      } finally {
        isTyping.value = false
        await scrollToBottom()
      }
    }

    // 加载对话历史
    const loadChatHistory = async () => {
      loadingHistory.value = true
      try {
        const result = await aiChatService.getChatHistory(1, 20)
        if (result.success) {
          chatHistory.value = result.data
        }
      } catch (error) {
        console.error('加载对话历史失败:', error)
      } finally {
        loadingHistory.value = false
      }
    }

    // 加载历史会话
    const loadHistorySession = async (sessionId) => {
      try {
        const result = await aiChatService.getChatHistoryBySession(sessionId)
        if (result.success && result.data.length > 0) {
          const historyMessages = []
          result.data.forEach(item => {
            historyMessages.push({
              type: 'user',
              content: item.userMessage || '用户消息为空',
              timestamp: new Date(item.createTime)
            })
            historyMessages.push({
              type: 'ai',
              content: item.aiResponse || 'AI回复为空',
              timestamp: new Date(item.createTime),
              processingTime: item.processingTime
            })
          })
          
          messages.value = historyMessages
          currentSessionId.value = sessionId
          showHistory.value = false
          await scrollToBottom()
        }
      } catch (error) {
        console.error('加载历史会话失败:', error)
      }
    }

    // 删除历史记录
    const deleteHistory = async (historyId) => {
      if (confirm('确定要删除这条对话记录吗？')) {
        try {
          const result = await aiChatService.deleteChatHistory(historyId)
          if (result.success) {
            chatHistory.value = chatHistory.value.filter(item => item.id !== historyId)
          }
        } catch (error) {
          console.error('删除历史记录失败:', error)
        }
      }
    }

    // 滚动到底部
    const scrollToBottom = async () => {
      await nextTick()
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    // 格式化消息内容
    const formatMessage = (content) => {
      // 检查content是否为null、undefined或空字符串
      if (!content || typeof content !== 'string') {
        console.warn('formatMessage收到无效content:', content)
        return '消息内容为空'
      }
      
      return content
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br>')
        .replace(/•/g, '•')
        .replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank">$1</a>')
    }

    // 格式化时间
    const formatTime = (timestamp) => {
      return aiChatService.formatTime(timestamp)
    }

    // 截断文本
    const truncateText = (text, maxLength) => {
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    }

    // 获取聊天类型文本
    const getChatTypeText = (type) => {
      const typeMap = {
        business: '商家咨询',
        food: '菜品推荐', 
        order: '订单相关',
        general: '通用对话'
      }
      return typeMap[type] || '通用对话'
    }

    // 组件挂载时初始化
    onMounted(async () => {
      await checkAiStatus()
      await nextTick()
      messageInput.value?.focus()
    })

    return {
      messages,
      chatHistory,
      inputMessage,
      isTyping,
      loadingHistory,
      showHistory,
      messagesContainer,
      messageInput,
      quickQuestions,
      aiStatus,
      detectedChatType,
      goBack,
      clearChat,
      handleKeyDown,
      onInputChange,
      sendMessage,
      askQuickQuestion,
      loadChatHistory,
      loadHistorySession,
      deleteHistory,
      formatMessage,
      formatTime,
      truncateText,
      getChatTypeText
    }
  }
}
</script>

<style scoped>
.ai-chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #667eea;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(-2px);
}

.header-title h1 {
  margin: 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.header-title p {
  margin: 0;
  font-size: 12px;
  color: #666;
}

.header-right .ai-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.ai-status.online {
  background: rgba(46, 213, 115, 0.1);
  color: #2ed573;
}

.ai-status.offline {
  background: rgba(255, 71, 87, 0.1);
  color: #ff4757;
}

.ai-status.checking {
  background: rgba(255, 165, 2, 0.1);
  color: #ffa502;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.ai-status.checking .status-dot {
  animation: pulse 1.5s infinite;
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  position: relative;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

/* 欢迎区域 */
.welcome-section {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.welcome-card {
  max-width: 800px;
  background: white;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.welcome-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.welcome-card h2 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.welcome-card > p {
  margin: 0 0 40px 0;
  color: #666;
  font-size: 16px;
}

.service-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.feature-card {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.3s;
}

.feature-card:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.feature-card i {
  font-size: 24px;
  color: #667eea;
  margin-bottom: 12px;
}

.feature-card:hover i {
  color: white;
}

.feature-card h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.feature-card p {
  margin: 0;
  font-size: 13px;
  opacity: 0.8;
  line-height: 1.4;
}

.quick-questions h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
}

.question-btn {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  color: #495057;
  padding: 12px 16px;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  text-align: left;
}

.question-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
  transform: translateY(-1px);
}

/* 消息样式 */
.message {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  animation: slideInUp 0.4s ease;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #667eea;
  color: white;
}

.message.ai .message-avatar {
  background: white;
  color: #667eea;
  border: 2px solid #e9ecef;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message.user .message-content {
  align-items: flex-end;
}

.message-bubble {
  padding: 16px 20px;
  border-radius: 20px;
  word-wrap: break-word;
  line-height: 1.5;
}

.message.user .message-bubble {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 8px;
}

.message.ai .message-bubble {
  background: white;
  color: #333;
  border: 1px solid #e9ecef;
  border-bottom-left-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-text {
  font-size: 15px;
}

.message-meta {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.message.user .message-time {
  text-align: right;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #ccc;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

.typing-text {
  margin-top: 8px;
  font-size: 13px;
  color: #666;
  font-style: italic;
}

/* 输入区域 */
.chat-input {
  background: white;
  border-top: 1px solid #e9ecef;
  padding: 20px;
}

.input-container {
  display: flex;
  gap: 15px;
  align-items: flex-end;
}

.input-wrapper {
  flex: 1;
}

.input-wrapper textarea {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 12px 16px;
  outline: none;
  font-size: 15px;
  font-family: inherit;
  resize: none;
  transition: border-color 0.3s;
  min-height: 44px;
  max-height: 120px;
}

.input-wrapper textarea:focus {
  border-color: #667eea;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 0 16px;
}

.input-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.char-count {
  font-size: 12px;
  color: #999;
}

.chat-type {
  font-size: 11px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

.input-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 6px;
  border-radius: 50%;
  transition: all 0.3s;
  font-size: 14px;
}

.action-btn:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.send-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.send-btn:hover:not(:disabled) {
  background: #5a67d8;
  transform: translateY(-1px);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

/* 历史记录侧边栏 */
.history-sidebar {
  position: fixed;
  top: 0;
  right: 0;
  width: 350px;
  height: 100vh;
  background: white;
  box-shadow: -5px 0 20px rgba(0, 0, 0, 0.1);
  z-index: 200;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.close-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 6px;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-btn:hover {
  color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.refresh-btn {
  width: 100%;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  color: #495057;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.refresh-btn:hover:not(:disabled) {
  background: #e9ecef;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.history-item:hover {
  background: #667eea;
  color: white;
  transform: translateY(-1px);
}

.history-content {
  flex: 1;
}

.history-message {
  font-size: 14px;
  margin-bottom: 6px;
  line-height: 1.4;
}

.history-time {
  font-size: 12px;
  opacity: 0.7;
}

.delete-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  opacity: 0;
  transition: all 0.3s;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  text-align: center;
  padding: 40px 20px;
}

.history-empty i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* 遮罩层 */
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 150;
}

/* 动画 */
.slide-left-enter-active, .slide-left-leave-active {
  transition: transform 0.3s ease;
}

.slide-left-enter-from {
  transform: translateX(100%);
}

.slide-left-leave-to {
  transform: translateX(100%);
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar,
.sidebar-content::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-track,
.sidebar-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-messages::-webkit-scrollbar-thumb,
.sidebar-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.chat-messages::-webkit-scrollbar-thumb:hover,
.sidebar-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-area {
    height: calc(100vh - 70px);
  }
  
  .service-features {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .question-grid {
    grid-template-columns: 1fr;
  }
  
  .history-sidebar {
    width: 100%;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .welcome-card {
    padding: 30px 20px;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 12px 15px;
  }
  
  .header-title h1 {
    font-size: 18px;
  }
  
  .service-features {
    grid-template-columns: 1fr;
  }
  
  .chat-messages {
    padding: 15px;
  }
  
  .chat-input {
    padding: 15px;
  }
  
  .welcome-card {
    padding: 20px 15px;
  }
}
</style>
