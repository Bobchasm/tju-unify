<template>
  <div class="ai-chatbot">
    <!-- AI客服触发按钮 -->
    <div class="chat-trigger" 
         @click="handleClick" 
         :class="{ active: showChat }">
      <i class="fa fa-robot"></i>
      <span v-if="!showChat && !hasUnreadMessages">AI</span>
      <span v-if="hasUnreadMessages" class="unread-indicator">•</span>
    </div>

    <!-- 聊天窗口 -->
    <transition name="slide-up">
      <div v-if="showChat" class="chat-container">
         <!-- 聊天头部 -->
         <div class="chat-header">
          <div class="header-left">
            <div class="ai-avatar">
              <i class="fa fa-robot"></i>
            </div>
            <div class="ai-info">
               <div class="ai-name">
                 小饿AI客服
               </div>
              <div class="ai-status" :class="aiStatus.type">
                <span class="status-dot"></span>
                {{ aiStatus.text }}
              </div>
            </div>
          </div>
          <div class="header-right">
            <button @click="clearChat" class="header-btn" title="清空对话">
              <i class="fa fa-trash"></i>
            </button>
            <button @click="showHistory = !showHistory" class="header-btn" title="历史记录">
              <i class="fa fa-history"></i>
            </button>
            <button @click="toggleChat" class="header-btn close-btn" title="关闭">
              <i class="fa fa-times"></i>
            </button>
          </div>
        </div>

        <!-- 历史记录面板 -->
        <div v-if="showHistory" class="history-panel">
          <div class="history-header">
            <h4>对话历史</h4>
            <button @click="loadChatHistory" class="refresh-btn">
              <i class="fa fa-refresh" :class="{ 'fa-spin': loadingHistory }"></i>
            </button>
          </div>
          <div class="history-list" v-if="chatHistory.length > 0">
            <div 
              v-for="history in chatHistory" 
              :key="history.id"
              class="history-item"
              @click="loadHistorySession(history.sessionId)"
            >
              <div class="history-content">
                <div class="history-message">{{ truncateText(history.userMessage, 30) }}</div>
                <div class="history-time">{{ formatTime(history.createTime) }}</div>
              </div>
              <button @click.stop="deleteHistory(history.id)" class="delete-btn">
                <i class="fa fa-trash"></i>
              </button>
            </div>
          </div>
          <div v-else class="history-empty">
            <i class="fa fa-comments"></i>
            <p>暂无对话历史</p>
          </div>
        </div>

        <!-- 聊天消息区域 -->
        <div v-if="!showHistory" class="chat-messages" ref="messagesContainer">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="welcome-icon">🍔</div>
            <h3>您好！我是小饿AI助手</h3>
            <p>我可以帮您：</p>
            <div class="feature-list">
              <div class="feature-item">
                <i class="fa fa-search"></i>
                <span>推荐美食和商家</span>
              </div>
              <div class="feature-item">
                <i class="fa fa-list"></i>
                <span>查询订单状态</span>
              </div>
              <div class="feature-item">
                <i class="fa fa-question-circle"></i>
                <span>解答使用问题</span>
              </div>
              <div class="feature-item">
                <i class="fa fa-gift"></i>
                <span>提供优惠信息</span>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
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
                  处理时间: {{ message.processingTime }}ms
                </div>
              </div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>

          <!-- AI正在输入指示器 -->
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
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
          <!-- 快捷问题（仅在无消息时显示） -->
          <div v-if="messages.length === 0 && !showHistory" class="quick-questions">
            <button
              v-for="question in quickQuestions"
              :key="question"
              @click="askQuickQuestion(question)"
              class="quick-btn"
            >
              {{ question }}
            </button>
          </div>
          
          <!-- 输入框 -->
          <div class="input-container">
            <div class="input-wrapper">
              <input
                v-model="inputMessage"
                @keyup.enter="sendMessage"
                @input="onInputChange"
                placeholder="输入您的问题..."
                :disabled="isTyping"
                ref="messageInput"
                maxlength="500"
              />
              <div class="input-footer">
                <div class="char-count">{{ inputMessage.length }}/500</div>
                <div class="chat-type-indicator" v-if="detectedChatType !== 'general'">
                  {{ getChatTypeText(detectedChatType) }}
                </div>
              </div>
            </div>
            <button
              @click="sendMessage"
              :disabled="!inputMessage.trim() || isTyping"
              class="send-btn"
            >
              <i class="fa fa-paper-plane"></i>
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, nextTick, watch, computed } from 'vue'
import aiChatService from '../services/aiChatService'

export default {
  name: 'AiChatbot',
  setup() {
    // 响应式数据
    const showChat = ref(false)
    const showHistory = ref(false)
    const messages = ref([])
    const chatHistory = ref([])
    const inputMessage = ref('')
    const isTyping = ref(false)
    const loadingHistory = ref(false)
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
      '今天有什么优惠',
      '如何查看订单状态',
      '配送时间多久'
    ])

    // 计算属性
    const hasUnreadMessages = computed(() => {
      // 这里可以添加未读消息的逻辑
      return false
    })

    // 初始化AI状态检查
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

    // 处理点击事件
    const handleClick = (event) => {
      console.log('handleClick called')
      toggleChat()
    }

    // 切换聊天窗口
    const toggleChat = async () => {
      console.log('toggleChat called, current showChat:', showChat.value)
      showChat.value = !showChat.value
      showHistory.value = false
      console.log('toggleChat after toggle, showChat:', showChat.value)
      
      if (showChat.value) {
        await nextTick()
        messageInput.value?.focus()
        
        // 首次打开时检查AI状态
        if (aiStatus.value.type === 'checking') {
          await checkAiStatus()
        }
      }
    }

    // 清空对话
    const clearChat = () => {
      if (confirm('确定要清空当前对话吗？')) {
        messages.value = []
        currentSessionId.value = null
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

      await scrollToBottom()
      await getAIResponse(userInput)
    }

    // 快捷问题
    const askQuickQuestion = (question) => {
      inputMessage.value = question
      sendMessage()
    }

    // 输入变化处理
    const onInputChange = () => {
      if (inputMessage.value.trim()) {
        detectedChatType.value = aiChatService.detectChatType(inputMessage.value)
      } else {
        detectedChatType.value = 'general'
      }
    }

    // 获取AI回复
    const getAIResponse = async (userInput) => {
      isTyping.value = true

      try {
        // 模拟打字延迟
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
          // 转换历史数据格式
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
    onMounted(() => {
      checkAiStatus()
    })

    return {
      showChat,
      showHistory,
      messages,
      chatHistory,
      inputMessage,
      isTyping,
      loadingHistory,
      messagesContainer,
      messageInput,
      quickQuestions,
      aiStatus,
      hasUnreadMessages,
      detectedChatType,
       handleClick,
       toggleChat,
      clearChat,
      sendMessage,
      askQuickQuestion,
      onInputChange,
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
.ai-chatbot {
  position: fixed;
  bottom: 100px; /* 距离底部40px */
  right: 20px; /* 距离右边20px */
  z-index: 9999;
}

/* 触发按钮 */
.chat-trigger {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 15px;
  padding: 10px 20px; /* 进一步减少内边距 */
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px; /* 进一步减少间距 */
  font-weight: 600;
  font-size: 20px; /* 进一步缩小字体 */
  position: relative;
  user-select: none; /* 防止选择文本 */
  max-width: 50vw; 
}

.chat-trigger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.6);
}

.chat-trigger.active {
  border-radius: 50%;
  padding: 10px; /* 进一步减少内边距 */
  width: 36px; /* 进一步缩小 */
  height: 36px;
  max-width: none; /* 激活状态不受最大宽度限制 */
}

.chat-trigger.active span {
  display: none;
}


.unread-indicator {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff4757;
  color: white;
  border-radius: 50%;
  width: 12px;
  height: 12px;
  font-size: 20px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}


/* 聊天容器 */
.chat-container {
  position: absolute;
  top: -520px; /* 在触发按钮上方，负值使其向上偏移 */
  right: 0; /* 与触发按钮右对齐 */
  width: 300px; /* 变窄100px */
  height: 500px; /* 稍微降低高度 */
  background: white;
  border-radius: 15px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 聊天头部 */
.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.2s ease;
}

.chat-header:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6d4190 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.ai-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ai-name {
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}


.ai-status {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  opacity: 0.9;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}

.ai-status.online .status-dot {
  background: #2ed573;
}

.ai-status.offline .status-dot {
  background: #ff4757;
}

.ai-status.checking .status-dot {
  background: #ffa502;
  animation: pulse 1.5s infinite;
}

.header-right {
  display: flex;
  gap: 8px;
}

.header-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.close-btn:hover {
  background: #ff4757 !important;
}

/* 历史记录面板 */
.history-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.history-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
}

.history-header h4 {
  margin: 0;
  color: #333;
  font-size: 14px;
}

.refresh-btn {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s;
}

.refresh-btn:hover {
  background: #f0f1ff;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.history-item {
  background: white;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
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
  margin-bottom: 4px;
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
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  text-align: center;
}

.history-empty i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
}

.welcome-message {
  text-align: center;
  color: #666;
  padding: 20px 0;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-message h3 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 18px;
}

.welcome-message p {
  margin: 0 0 20px 0;
  font-size: 14px;
}

.feature-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: white;
  border-radius: 8px;
  font-size: 13px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.feature-item i {
  color: #667eea;
  width: 16px;
}

/* 消息样式 */
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  animation: slideInUp 0.3s ease;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
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
  max-width: 75%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message.user .message-content {
  align-items: flex-end;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
  line-height: 1.4;
}

.message.user .message-bubble {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 6px;
}

.message.ai .message-bubble {
  background: white;
  color: #333;
  border: 1px solid #e9ecef;
  border-bottom-left-radius: 6px;
}

.message-text {
  font-size: 14px;
}

.message-meta {
  font-size: 11px;
  opacity: 0.7;
  margin-top: 6px;
}

.message-time {
  font-size: 11px;
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
  padding: 12px 16px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
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

/* 输入区域 */
.chat-input {
  padding: 20px;
  background: white;
  border-top: 1px solid #e9ecef;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.quick-btn {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  color: #495057;
  padding: 8px 12px;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
  transform: translateY(-1px);
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper {
  flex: 1;
}

.input-wrapper input {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 12px 16px;
  outline: none;
  font-size: 14px;
  transition: border-color 0.3s;
  resize: none;
}

.input-wrapper input:focus {
  border-color: #667eea;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
  padding: 0 16px;
}

.char-count {
  font-size: 11px;
  color: #999;
}

.chat-type-indicator {
  font-size: 11px;
  color: #667eea;
  background: #f0f1ff;
  padding: 2px 8px;
  border-radius: 10px;
}

.send-btn {
  background: #667eea;
  color: white;
  border: none;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.send-btn:hover:not(:disabled) {
  background: #5a67d8;
  transform: scale(1.05);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

/* 动画 */
.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(20px);
  opacity: 0;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(15px);
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
    transform: translateY(-8px);
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
.history-list::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-track,
.history-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-messages::-webkit-scrollbar-thumb,
.history-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.chat-messages::-webkit-scrollbar-thumb:hover,
.history-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-chatbot {
    bottom: 20px; /* 移动端底部边距稍小 */
    right: 15px; /* 移动端右边距稍小 */
  }
  
  .chat-trigger {
    max-width: 30vw; /* 移动端进一步限制宽度 */
    font-size: 11px; /* 移动端字体更小 */
    padding: 6px 10px; /* 移动端内边距更小 */
  }
  
  .chat-container {
    width: 90vw; /* 适应窄屏 */
    height: 70vh; /* 调整高度 */
    top: -72vh; /* 在触发器上方，根据高度调整 */
    right: -15px; /* 调整右对齐位置 */
  }
  
  .feature-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .ai-chatbot {
    bottom: 15px; /* 小屏设备底部边距更小 */
    right: 10px; /* 小屏设备右边距更小 */
  }
  
  .chat-trigger {
    max-width: 25vw; /* 小屏设备进一步限制宽度 */
    font-size: 10px; /* 小屏字体最小 */
    padding: 5px 8px; /* 小屏内边距最小 */
  }
  
  .chat-container {
    top: -77vh; /* 在触发器上方，根据高度调整 */
    right: -10px; /* 调整右对齐位置 */
    width: 95vw;
    height: 75vh;
    border-radius: 15px;
  }
}
</style>
