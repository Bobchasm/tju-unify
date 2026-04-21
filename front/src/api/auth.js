import request from './request'

export const getToken = () => {
  return localStorage.getItem('token') || sessionStorage.getItem('token')
}

export const setToken = (token, rememberMe = true) => {
  const storage = rememberMe ? localStorage : sessionStorage
  storage.setItem('token', token)
}

export const removeToken = () => {
  localStorage.removeItem('token')
  sessionStorage.removeItem('token')
}

export const getUserInfo = () => {
  const info = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
  return info ? JSON.parse(info) : null
}

export const setUserInfo = (userInfo, rememberMe = true) => {
  const storage = rememberMe ? localStorage : sessionStorage
  storage.setItem('userInfo', JSON.stringify(userInfo))
}

export const removeUserInfo = () => {
  localStorage.removeItem('userInfo')
  sessionStorage.removeItem('userInfo')
}

export const isAuthenticated = () => {
  return !!getToken()
}

export const login = async (username, password, rememberMe = false) => {
  const response = await request.post('/api/auth', {
    username,
    password,
    rememberMe
  })

  if (response?.id_token) {
    setToken(response.id_token, rememberMe)

    try {
      const userRes = await request.get('/api/user')
      if (userRes) {
        setUserInfo(userRes, rememberMe)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  return response
}

export const register = async (userData) => {
  return await request.post('/api/register', userData)
}

export const logout = () => {
  removeToken()
  removeUserInfo()
}

export default {
  login,
  register,
  logout,
  getToken,
  setToken,
  removeToken,
  getUserInfo,
  setUserInfo,
  removeUserInfo,
  isAuthenticated
}
