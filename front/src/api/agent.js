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

export const fetchChatSessions = async () => {
  const response = await apiClient.get('/api/chat/sessions', { params: { limit: 50 } })
  return response.data
}

export const fetchChatHistory = async (sessionId) => {
  if (!sessionId) return { session_id: '', messages: [] }
  const response = await apiClient.get('/api/chat/history', {
    params: { session_id: sessionId }
  })
  return response.data
}

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
