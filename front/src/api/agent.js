import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://localhost:8000',
  timeout: 60000
})

function getCampusBearerToken() {
  return localStorage.getItem('token') || sessionStorage.getItem('token') || null
}

export const sendMessage = async (messages, sessionId = null) => {
  const token = getCampusBearerToken()
  const body = {
    session_id: sessionId,
    messages: messages
  }
  if (token) {
    body.bearer_token = token
  }
  const response = await apiClient.post('/api/chat', body)
  return response.data
}

/**
 * 流式对话
 * @param onSessionId 收到首帧 meta 时回调（后端下发的 session_id，多轮必传）
 */
export const sendMessageStream = async (
  messages,
  sessionId = null,
  onChunk,
  onDone,
  onError,
  onSessionId = null
) => {
  try {
    const token = getCampusBearerToken()
    const body = {
      session_id: sessionId,
      messages: messages
    }
    if (token) {
      body.bearer_token = token
    }
    const response = await fetch('http://localhost:8000/api/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        if (onDone) onDone()
        break
      }

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const raw = line.slice(6)
          if (raw === '[DONE]') {
            if (onDone) onDone()
            return
          }
          let text = raw
          try {
            text = JSON.parse(raw)
          } catch {
            // 兼容旧版未编码的纯文本
          }
          if (text && typeof text === 'object' && text.event === 'session' && text.session_id) {
            if (onSessionId) onSessionId(String(text.session_id))
            continue
          }
          if (onChunk) {
            if (text && typeof text === 'object') {
              onChunk(JSON.stringify(text))
            } else {
              onChunk(text)
            }
          }
        }
      }
    }
  } catch (error) {
    if (onError) onError(error)
  }
}

/** 列出服务端已保存的会话摘要（本机 data/chat_history） */
export const fetchChatSessions = async () => {
  const response = await apiClient.get('/api/chat/sessions', { params: { limit: 50 } })
  return response.data
}

/** 从 tian-agent 拉取某 session 的完整对话（需先通过流式首帧拿到 session_id） */
export const fetchChatHistory = async (sessionId) => {
  if (!sessionId) return { session_id: '', messages: [] }
  const response = await apiClient.get('/api/chat/history', {
    params: { session_id: sessionId }
  })
  return response.data
}

/** 删除服务端该 session 的完整对话文件 */
export const deleteChatHistory = async (sessionId) => {
  if (!sessionId) return { ok: false }
  const response = await apiClient.delete('/api/chat/history', {
    params: { session_id: sessionId }
  })
  return response.data
}

export default {
  sendMessage,
  sendMessageStream,
  fetchChatSessions,
  fetchChatHistory,
  deleteChatHistory
}
