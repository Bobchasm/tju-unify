import axios from 'axios'

const tradeRequest = axios.create({
  baseURL: '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

function shouldSkipAuthHeader(url) {
  if (!url) return false
  if (url.includes('/api/auth') || url.includes('/api/register')) return true
  if (url === '/upload' || url.startsWith('/upload?')) return true
  return false
}

function isMultipartUpload(url) {
  if (!url) return false
  return url === '/api/upload' || url.startsWith('/api/upload?') || url === '/upload' || url.startsWith('/upload?')
}

tradeRequest.interceptors.request.use(
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
    return config
  },
  (error) => Promise.reject(error)
)

tradeRequest.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      sessionStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      sessionStorage.removeItem('userInfo')
      const hash = window.location.hash || ''
      if (!hash.includes('/trade/login')) {
        window.location.href = '/#/trade/login'
      }
    }
    return Promise.reject(error)
  }
)

export default tradeRequest
