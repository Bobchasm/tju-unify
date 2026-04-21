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

// 流式接口
export const sendMessageStream = async (messages, sessionId = null, onChunk, onDone, onError) => {
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
            // 与后端一致：每帧为 json.dumps 的单行字符串，可安全携带换行
            text = JSON.parse(raw)
          } catch {
            // 兼容旧版未编码的纯文本
          }
          if (onChunk) onChunk(text)
        }
      }
    }
  } catch (error) {
    if (onError) onError(error)
  }
}

export default {
  sendMessage,
  sendMessageStream
}
