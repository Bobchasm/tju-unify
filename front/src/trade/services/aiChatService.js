/**
 * AI智能客服服务
 * 负责调用后端AI客服接口
 */

import request from '@/trade/utils/tradeRequest'

class AiChatService {
  
  /**
   * 发送消息给AI客服
   * @param {string} message - 用户消息内容
   * @param {string} chatType - 对话类型 (general, business, food, order)
   * @param {string} sessionId - 会话ID (可选)
   * @returns {Promise<Object>} AI回复响应
   */
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
      
      // 添加详细调试日志
      console.log('后端响应数据:', response)
      console.log('响应类型:', typeof response)
      console.log('响应结构:', Object.keys(response || {}))
      
      // 检查响应格式并处理
      if (response && (response.code === 200 || response.code === 'OK') && response.data) {
        // 标准格式：{code: 200/OK, message: "...", data: {...}}
        console.log('使用标准格式解析，data.message:', response.data.message)
        return {
          success: true,
          data: response.data
        }
      } else if (response && response.success && response.data) {
        // 处理 {success: true, data: {...}} 格式
        console.log('使用success格式解析，data.message:', response.data.message)
        return {
          success: true,
          data: response.data
        }
      } else if (response && response.message && response.sessionId) {
        // 直接返回AI回复对象的情况
        console.log('使用直接对象格式解析，message:', response.message)
        return {
          success: true,
          data: response
        }
      } else if (response && typeof response === 'object') {
        // 其他对象格式，尝试作为数据返回
        console.log('使用通用对象格式解析')
        return {
          success: true,
          data: response
        }
      } else {
        // 响应格式不符合预期
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
      
      // 检查是否是网络错误
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
      
      // 检查是否是后端服务错误
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
      
      // 其他错误
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

  /**
   * 获取用户对话历史
   * @param {number} page - 页码 (默认1)
   * @param {number} size - 每页大小 (默认20)
   * @returns {Promise<Object>} 对话历史列表
   */
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
        // 处理直接返回数组的情况
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

  /**
   * 根据会话ID获取对话历史
   * @param {string} sessionId - 会话ID
   * @returns {Promise<Object>} 会话对话历史
   */
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

  /**
   * 删除对话历史记录
   * @param {number} historyId - 历史记录ID
   * @returns {Promise<Object>} 删除结果
   */
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

  /**
   * 清理旧对话记录
   * @param {number} keepCount - 保留的记录数量 (默认50)
   * @returns {Promise<Object>} 清理结果
   */
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

  /**
   * AI客服健康检查
   * @returns {Promise<Object>} 健康状态
   */
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
        // 处理直接返回字符串的情况
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

  /**
   * 获取当前用户ID
   * @returns {number|null} 用户ID
   */
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
      
      // 如果没有用户信息，使用测试用户ID（用户33有多个订单）
      console.warn('未找到用户信息，使用测试用户ID: 33')
      return 33
    } catch (error) {
      console.warn('获取用户ID失败:', error)
      // 出错时也使用测试用户ID（用户33有多个订单）
      return 33
    }
  }

  /**
   * 生成会话ID
   * @returns {string} UUID格式的会话ID
   */
  generateSessionId() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0
      const v = c === 'x' ? r : (r & 0x3 | 0x8)
      return v.toString(16)
    })
  }

  /**
   * 格式化时间
   * @param {string|Date} timestamp - 时间戳
   * @returns {string} 格式化后的时间
   */
  formatTime(timestamp) {
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now - date
    
    // 今天
    if (diff < 24 * 60 * 60 * 1000 && now.getDate() === date.getDate()) {
      return date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }
    
    // 昨天
    if (diff < 48 * 60 * 60 * 1000) {
      return '昨天 ' + date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }
    
    // 更早
    return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }

  /**
   * 检测消息类型
   * @param {string} message - 用户消息
   * @returns {string} 推荐的聊天类型
   */
  detectChatType(message) {
    const lowerMessage = message.toLowerCase()
    
    // 商家相关关键词
    if (this.containsKeywords(lowerMessage, ['商家', '店铺', '餐厅', '外卖店', '商户', '饭店', '推荐'])) {
      return 'business'
    }
    
    // 菜品相关关键词
    if (this.containsKeywords(lowerMessage, ['菜', '菜品', '食物', '美食', '餐', '吃', '点餐', '菜单'])) {
      return 'food'
    }
    
    // 订单相关关键词
    if (this.containsKeywords(lowerMessage, ['订单', '下单', '支付', '配送', '外卖', '催单', '退款', '状态'])) {
      return 'order'
    }
    
    // 默认为通用对话
    return 'general'
  }

  /**
   * 检查消息是否包含关键词
   * @param {string} message - 消息内容
   * @param {Array} keywords - 关键词数组
   * @returns {boolean} 是否包含
   */
  containsKeywords(message, keywords) {
    return keywords.some(keyword => message.includes(keyword))
  }
}

// 导出单例
export default new AiChatService()

// 使用示例：
/*
import aiChatService from '@/services/aiChatService'

// 发送消息
const result = await aiChatService.sendMessage('推荐一些好吃的川菜', 'business')
if (result.success) {
  console.log('AI回复:', result.data.message)
}

// 获取对话历史
const history = await aiChatService.getChatHistory(1, 10)
if (history.success) {
  console.log('对话历史:', history.data)
}

// 健康检查
const health = await aiChatService.healthCheck()
console.log('AI服务状态:', health.status)
*/
