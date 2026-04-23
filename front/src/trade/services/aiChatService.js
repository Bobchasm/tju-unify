
import request from '@/trade/utils/tradeRequest'

class AiChatService {
  
  async sendMessage(message, chatType = 'general', sessionId = null) {
    try {
      const requestData = {
        message: message.trim(),
        chatType: chatType,
        userId: this.getCurrentUserId(), // 从本地存储获取用户ID
        sessionId: sessionId
      }
      
      console.log('发送AI请求:', requestData)
      
      const response = await request.post('/api/ai/chat', requestData)
      
      console.log('后端响应数据:', response)
      console.log('响应类型:', typeof response)
      console.log('响应结构:', Object.keys(response || {}))
      
      if (response && (response.code === 200 || response.code === 'OK') && response.data) {
        console.log('使用标准格式解析，data.message:', response.data.message)
        return {
          success: true,
          data: response.data
        }
      } else if (response && response.success && response.data) {
        console.log('使用success格式解析，data.message:', response.data.message)
        return {
          success: true,
          data: response.data
        }
      } else if (response && response.message && response.sessionId) {
        console.log('使用直接对象格式解析，message:', response.message)
        return {
          success: true,
          data: response
        }
      } else if (response && typeof response === 'object') {
        console.log('使用通用对象格式解析')
        return {
          success: true,
          data: response
        }
      } else {
        console.error('响应格式异常:', response)
        throw new Error(`后端响应格式异常: ${JSON.stringify(response)}`)
      }
    } catch (error) {
      console.error('AI客服调用失败:', error)
      console.error('错误详情:', {
        message: error.message,
        stack: error.stack,
        response: error.response
      })
      
      if (error.message?.includes('Network Error') || error.code === 'NETWORK_ERROR') {
        return {
          success: false,
          error: '网络连接失败，请检查网络连接',
          data: {
            message: '抱歉，网络连接失败，请检查您的网络连接后重试。',
            sessionId: sessionId || this.generateSessionId(),
            responseType: 'error',
            responseTime: new Date().toISOString(),
            processingTime: 0
          }
        }
      }
      
      if (error.message?.includes('500') || error.message?.includes('502') || error.message?.includes('503')) {
        return {
          success: false,
          error: '后端服务暂时不可用',
          data: {
            message: '抱歉，AI客服服务暂时不可用，请稍后再试或联系人工客服。',
            sessionId: sessionId || this.generateSessionId(),
            responseType: 'error',
            responseTime: new Date().toISOString(),
            processingTime: 0
          }
        }
      }
      
      return {
        success: false,
        error: error.message,
        data: {
          message: '抱歉，我现在遇到了一些技术问题，请稍后再试或联系人工客服。😔',
          sessionId: sessionId || this.generateSessionId(),
          responseType: 'error',
          responseTime: new Date().toISOString(),
          processingTime: 0
        }
      }
    }
  }

  async getChatHistory(page = 1, size = 20) {
    try {
      const response = await request.get('/api/ai/chat/history', {
        params: {
          page: page,
          size: size
        }
      })
      
      console.log('对话历史响应:', response)
      
      if (response && (response.code === 200 || response.code === 'OK') && response.data) {
        return {
          success: true,
          data: response.data || []
        }
      } else if (response && response.success && response.data) {
        return {
          success: true,
          data: response.data || []
        }
      } else if (response && Array.isArray(response)) {
        return {
          success: true,
          data: response
        }
      } else if (response && response.data) {
        return {
          success: true,
          data: response.data || []
        }
      } else {
        throw new Error(response?.message || '获取对话历史失败')
      }
    } catch (error) {
      console.error('获取对话历史失败:', error)
      return {
        success: false,
        error: error.message,
        data: []
      }
    }
  }

  async getChatHistoryBySession(sessionId) {
    try {
      const response = await request.get(`/api/ai/chat/history/session/${sessionId}`)
      
      console.log('会话历史响应:', response)
      
      if (response && (response.code === 200 || response.code === 'OK') && response.data) {
        return {
          success: true,
          data: response.data || []
        }
      } else if (response && response.success && response.data) {
        return {
          success: true,
          data: response.data || []
        }
      } else if (response && Array.isArray(response)) {
        return {
          success: true,
          data: response
        }
      } else {
        throw new Error(response?.message || '获取会话历史失败')
      }
    } catch (error) {
      console.error('获取会话历史失败:', error)
      return {
        success: false,
        error: error.message,
        data: []
      }
    }
  }

  async deleteChatHistory(historyId) {
    try {
      const response = await request.delete(`/api/ai/chat/history/${historyId}`)
      
      console.log('删除历史响应:', response)
      
      if (response && (response.code === 200 || response.code === 'OK')) {
        return {
          success: true,
          data: response.data || true
        }
      } else if (response && response.success) {
        return {
          success: true,
          data: response.data || true
        }
      } else {
        throw new Error(response?.message || '删除对话历史失败')
      }
    } catch (error) {
      console.error('删除对话历史失败:', error)
      return {
        success: false,
        error: error.message
      }
    }
  }

  async cleanOldChatHistory(keepCount = 50) {
    try {
      const response = await request.post('/api/ai/chat/history/clean', null, {
        params: {
          keepCount: keepCount
        }
      })
      
      console.log('清理历史响应:', response)
      
      if (response && (response.code === 200 || response.code === 'OK')) {
        return {
          success: true,
          data: response.data || true
        }
      } else if (response && response.success) {
        return {
          success: true,
          data: response.data || true
        }
      } else {
        throw new Error(response?.message || '清理对话历史失败')
      }
    } catch (error) {
      console.error('清理对话历史失败:', error)
      return {
        success: false,
        error: error.message
      }
    }
  }

  async healthCheck() {
    try {
      const response = await request.get('/api/ai/chat/health')
      
      console.log('健康检查响应:', response)
      
      if (response && (response.code === 200 || response.code === 'OK') && response.data) {
        return {
          success: true,
          status: 'healthy',
          message: response.data
        }
      } else if (response && response.success && response.data) {
        return {
          success: true,
          status: 'healthy',
          message: response.data
        }
      } else if (response && typeof response === 'string') {
        return {
          success: true,
          status: 'healthy',
          message: response
        }
      } else if (response && response.data) {
        return {
          success: true,
          status: 'healthy',
          message: response.data
        }
      } else {
        throw new Error(response?.message || '健康检查失败')
      }
    } catch (error) {
      console.error('AI客服健康检查失败:', error)
      return {
        success: false,
        status: 'unhealthy',
        error: error.message
      }
    }
  }

  getCurrentUserId() {
    try {
      const userInfo = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
      console.log('获取到的用户信息:', userInfo)
      
      if (userInfo) {
        const user = JSON.parse(userInfo)
        console.log('解析后的用户对象:', user)
        const userId = user.id || user.userId || user.ID || user.user_id
        console.log('提取的用户ID:', userId)
        return userId || null
      }
      
      console.warn('未找到用户信息，使用测试用户ID: 33')
      return 33
    } catch (error) {
      console.warn('获取用户ID失败:', error)
      return 33
    }
  }

  generateSessionId() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0
      const v = c === 'x' ? r : (r & 0x3 | 0x8)
      return v.toString(16)
    })
  }

  formatTime(timestamp) {
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now - date
    
    if (diff < 24 * 60 * 60 * 1000 && now.getDate() === date.getDate()) {
      return date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }
    
    if (diff < 48 * 60 * 60 * 1000) {
      return '昨天 ' + date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }
    
    return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }

  detectChatType(message) {
    const lowerMessage = message.toLowerCase()
    
    if (this.containsKeywords(lowerMessage, ['商家', '店铺', '餐厅', '外卖店', '商户', '饭店', '推荐'])) {
      return 'business'
    }
    
    if (this.containsKeywords(lowerMessage, ['菜', '菜品', '食物', '美食', '餐', '吃', '点餐', '菜单'])) {
      return 'food'
    }
    
    if (this.containsKeywords(lowerMessage, ['订单', '下单', '支付', '配送', '外卖', '催单', '退款', '状态'])) {
      return 'order'
    }
    
    return 'general'
  }

  containsKeywords(message, keywords) {
    return keywords.some(keyword => message.includes(keyword))
  }
}

export default new AiChatService()

