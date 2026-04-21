import axios from 'axios'

const request = axios.create({
  baseURL: '',  // 用空字符串表示相对路径，走 Vite 代理
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

function shouldSkipAuthHeader(url) {
  if (!url) return false
  if (url.includes('/api/auth') || url.includes('/api/register')) return true
  // 仅裸 /upload 跳过鉴权；/api/upload 需带 token（与 7070 网关实测一致）
  if (url === '/upload' || url.startsWith('/upload?')) return true
  return false
}

function isMultipartUpload(url) {
  if (!url) return false
  return url === '/api/upload' || url.startsWith('/api/upload?') || url === '/upload' || url.startsWith('/upload?')
}

request.interceptors.request.use(
  (config) => {
    if (!shouldSkipAuthHeader(config.url)) {
      const token = localStorage.getItem('token') || sessionStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    if (isMultipartUpload(config.url)) {
      delete config.headers['Content-Type']
    }
    // axios 会自动把对象序列化为 JSON，不需要手动 JSON.stringify
    // 如果手动 JSON.stringify 反而会导致 Content-Type 不一致和解析问题
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      sessionStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      sessionStorage.removeItem('userInfo')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
