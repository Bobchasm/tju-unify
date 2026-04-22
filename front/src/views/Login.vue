<template>
  <div class="login-page">
    <div class="header">
      <h1>天大校园助手</h1>
    </div>
    <div class="form-container">
      <div class="form-box">
        <h2>登录</h2>
        <div class="form-item">
          <label>用户名</label>
          <input
            type="text"
            v-model="username"
            placeholder="请输入用户名"
            @keyup.enter="login"
          />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input
            type="password"
            v-model="password"
            placeholder="请输入密码"
            @keyup.enter="login"
          />
        </div>
        <div class="form-item checkbox-item">
          <input
            type="checkbox"
            id="rememberMe"
            v-model="rememberMe"
          />
          <label for="rememberMe">记住我</label>
        </div>
        <button
          class="submit-btn"
          @click="login"
          :disabled="loading"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>
        <div class="link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import auth from '../api/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const rememberMe = ref(false)
const loading = ref(false)

const login = async () => {
  if (!username.value.trim()) {
    toast.warning('请输入用户名')
    return
  }
  if (!password.value.trim()) {
    toast.warning('请输入密码')
    return
  }

  loading.value = true
  try {
    console.log('开始登录，用户名:', username.value.trim())
    const response = await auth.login(
      username.value.trim(),
      password.value.trim(),
      rememberMe.value
    )
    console.log('登录响应:', response)
    
    if (response?.id_token) {
      console.log('登录成功，跳转首页')
      router.push('/')
    } else {
      toast.warning('登录失败，未获取到 token，请重试')
    }
  } catch (error) {
    console.error('登录错误:', error)
    console.error('错误详细信息:', {
      message: error.message,
      response: error.response,
      data: error.response?.data,
      status: error.response?.status
    })
    let errorMsg = '登录失败，请检查网络'
    if (error.response?.data?.message) {
      errorMsg = error.response.data.message
    } else if (error.response?.data?.id_token) {
      // 虽然报错但有 token，可能是后续获取用户信息出错了
      console.log('检测到 token，但有错误，尝试继续')
      router.push('/')
      return
    }
    toast.warning(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  padding: 40px 20px;
  text-align: center;
}

.header h1 {
  color: white;
  font-size: 28px;
  font-weight: bold;
}

.form-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
}

.form-box {
  background: white;
  border-radius: 16px;
  padding: 30px;
  width: 100%;
  max-width: 360px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.form-box h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  color: #555;
  margin-bottom: 8px;
  font-size: 14px;
}

.form-item input[type="text"],
.form-item input[type="password"] {
  width: 100%;
  padding: 14px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-item input:focus {
  outline: none;
  border-color: #667eea;
}

.form-item.checkbox-item {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.form-item.checkbox-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin-right: 8px;
  cursor: pointer;
}

.form-item.checkbox-item label {
  margin: 0;
  cursor: pointer;
}

.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.3s;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.link a:hover {
  text-decoration: underline;
}
</style>
