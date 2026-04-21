import axios from 'axios'

/**
 * 交易平台 API：与校园助手主站一致走 7070 网关（开发环境经 Vite `/api` 代理）。
 * 见 docs/unify-backend.md：以 `/api/`、`/ws/` 开头的请求由 unify 网关按规则转发，饿了吧（电商）侧由对应网关处理；用户与鉴权与主站共用。
 * 与 src/api/request.js 行为对齐，仅 401 时跳转到交易平台登录页。
 */
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
