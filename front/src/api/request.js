import axios from 'axios'

const request = axios.create({
  baseURL: '',  // 用空字符串表示相对路径，走 Vite 代理
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    const excludePaths = ['/api/auth', '/api/register', '/upload']
    if (!excludePaths.some(path => config.url.includes(path))) {
      const token = localStorage.getItem('token') || sessionStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    if (config.url.includes('/upload')) {
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
