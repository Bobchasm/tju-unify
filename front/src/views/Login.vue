<template>
  <div class="login-page">
    <!-- 动态粒子背景 -->
    <div class="particles-bg"></div>
    
    <!-- 背景装饰光晕 -->
    <div class="bg-decoration"></div>
    
    <div class="login-container">
      <!-- Logo区域 -->
      <div class="logo-section">
        <div class="logo-wrapper">
          <div class="logo-white-bg"></div>
          <img src="/tju.png" alt="校徽" class="logo-img" />
        </div>
        <h1>天大校园助手</h1>
        <p>让校园生活更美好</p>
      </div>

      <!-- 登录表单 -->
      <div class="form-section">
        <div class="form-card">
          <div class="welcome-text">
            <span class="greeting">👋 欢迎回来</span>
          </div>
          
          <div class="input-group">
            <div class="input-icon">
              <i class="fa-solid fa-user"></i>
            </div>
            <input
              type="text"
              v-model="username"
              placeholder="用户名"
              @keyup.enter="login"
              class="login-input"
            />
          </div>

          <div class="input-group">
            <div class="input-icon">
              <i class="fa-solid fa-lock"></i>
            </div>
            <input
              type="password"
              v-model="password"
              placeholder="密码"
              @keyup.enter="login"
              class="login-input"
            />
          </div>

          <div class="form-options">
            <label class="checkbox-label">
              <input type="checkbox" v-model="rememberMe" />
              <span>记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>

          <button
            class="login-btn"
            @click="login"
            :disabled="loading"
          >
            <span v-if="!loading">登 录</span>
            <span v-else class="loading-spinner"></span>
          </button>

          <div class="register-link">
            还没有账号？
            <router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import auth from '../api/auth'
import { toast } from '../utils/toast'

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
    const response = await auth.login(
      username.value.trim(),
      password.value.trim(),
      rememberMe.value
    )
    
    if (response?.id_token) {
      toast.success('登录成功')
      router.push({ path: '/' })
    } else {
      toast.warning('登录失败，请重试')
    }
  } catch (error) {
    console.error('登录错误:', error)
    let errorMsg = '登录失败，请检查网络'
    if (error.response?.data?.message) {
      errorMsg = error.response.data.message
    } else if (error.response?.data?.id_token) {
      router.push({ path: '/' })
      return
    }
    toast.warning(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a5bbf, #00a8e8);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 波浪流动背景 */
/* 呼吸光晕背景 */
.particles-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

.particles-bg::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  transform: translate(-50%, -50%);
  animation: breathe 4s ease-in-out infinite;
}

.particles-bg::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 80%;
  height: 80%;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.08) 0%, transparent 80%);
  transform: translate(-50%, -50%);
  animation: breathe 5s ease-in-out infinite reverse;
}

@keyframes breathe {
  0%, 100% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 0.4;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.8;
  }
}

/* 背景装饰光晕 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 1;
}

.bg-decoration::before {
  content: '';
  position: absolute;
  top: 15%;
  right: -10%;
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1), transparent);
  border-radius: 50%;
  animation: floatLarge 12s ease-in-out infinite;
}

.bg-decoration::after {
  content: '';
  position: absolute;
  bottom: 15%;
  left: -10%;
  width: 40%;
  height: 40%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08), transparent);
  border-radius: 50%;
  animation: floatLarge 10s ease-in-out infinite reverse;
}

@keyframes floatLarge {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(25px, 20px);
  }
}

/* 主容器 */
.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  padding: 0 20px;
  margin-top: -80px;
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Logo区域 */
.logo-section {
  text-align: center;
  margin-bottom: 28px;
}

.logo-wrapper {
  position: relative;
  width: 130px;
  height: 130px;
  margin: 0 auto 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 白色圆盘背景 */
.logo-white-bg {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 130px;
  height: 130px;
  background: white;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  z-index: 1;
}

.logo-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 50%;
  position: relative;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo-section h1 {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin-bottom: 6px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  letter-spacing: 1px;
}

.logo-section p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

/* 表单卡片 */
.form-section {
  background: white;
  border-radius: 28px;
  padding: 30px 28px 34px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
}

.form-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3a7bd5, #00d2ff);
}

.welcome-text {
  text-align: center;
  margin-bottom: 26px;
}

.greeting {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  position: relative;
  display: inline-block;
}

.greeting::before,
.greeting::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 30px;
  height: 2px;
  background: linear-gradient(90deg, #3a7bd5, #00d2ff);
}

.greeting::before {
  right: 100%;
  margin-right: 12px;
}

.greeting::after {
  left: 100%;
  margin-left: 12px;
}

/* 输入框 */
.input-group {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
  border: 1px solid #e8e8e8;
  border-radius: 14px;
  transition: all 0.3s ease;
  background: #fafbfc;
}

.input-group:focus-within {
  border-color: #3a7bd5;
  box-shadow: 0 0 0 3px rgba(58, 123, 213, 0.1);
  background: white;
}

.input-icon {
  width: 52px;
  text-align: center;
  color: #aaa;
  font-size: 18px;
  transition: color 0.3s ease;
}

.input-group:focus-within .input-icon {
  color: #3a7bd5;
}

.login-input {
  flex: 1;
  padding: 14px 15px 14px 0;
  border: none;
  outline: none;
  font-size: 15px;
  background: transparent;
}

.login-input::placeholder {
  color: #bbb;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 26px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.checkbox-label input {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #3a7bd5;
}

.forgot-link {
  font-size: 14px;
  color: #3a7bd5;
  text-decoration: none;
  transition: color 0.3s;
}

.forgot-link:hover {
  color: #00d2ff;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  transform: translate(-50%, -50%);
  transition: width 0.5s, height 0.5s;
}

.login-btn:hover::before {
  width: 300px;
  height: 300px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(58, 123, 213, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 加载动画 */
.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid white;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 注册链接 */
.register-link {
  text-align: center;
  margin-top: 22px;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #3a7bd5;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s;
}

.register-link a:hover {
  color: #00d2ff;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .login-container {
    max-width: 92%;
    margin-top: -60px;
  }
  
  .logo-wrapper {
    width: 105px;
    height: 105px;
  }
  
  .logo-white-bg {
    width: 105px;
    height: 105px;
  }
  
  .logo-img {
    width: 68px;
    height: 68px;
  }
  
  .logo-section h1 {
    font-size: 24px;
  }
  
  .form-section {
    padding: 24px 20px 28px;
  }
  
  .greeting {
    font-size: 16px;
  }
  
  .greeting::before,
  .greeting::after {
    width: 20px;
  }
}
</style>