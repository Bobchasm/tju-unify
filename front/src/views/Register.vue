<template>
  <div class="register-page">
    <!-- 呼吸光晕背景 -->
    <div class="particles-bg"></div>
    
    <!-- 背景装饰光晕 -->
    <div class="bg-decoration"></div>
    
    <div class="register-container">
      <!-- 头像上传区域 - 替换原校徽位置 -->
      <div class="avatar-section">
        <div class="avatar-upload" @click="triggerFileInput">
          <div v-if="!avatar" class="upload-placeholder">
            <i class="fa-solid fa-camera"></i>
            <span>上传头像</span>
          </div>
          <img v-else :src="avatar" alt="头像预览" />
          <input
            type="file"
            ref="fileInput"
            accept="image/*"
            @change="handleFileChange"
            style="display: none"
          />
        </div>
        <p class="avatar-hint">点击上传头像（可选）</p>
      </div>

      <!-- 表单滚动区域 -->
      <div class="form-scroll-area">
        <div class="form-section">
          <div class="form-card">
            <div class="welcome-text">
              <span class="greeting">📝 注册新账号</span>
            </div>
            
            <div class="two-columns">
              <div class="input-group">
                <div class="input-icon">
                  <i class="fa-solid fa-user"></i>
                </div>
                <input
                  type="text"
                  v-model="userData.username"
                  placeholder="用户名 *"
                  maxlength="20"
                  class="register-input"
                />
              </div>
              
              <div class="input-group">
                <div class="input-icon">
                  <i class="fa-solid fa-user-pen"></i>
                </div>
                <input
                  type="text"
                  v-model="userData.firstName"
                  placeholder="姓 *"
                  class="register-input"
                />
              </div>
            </div>

            <div class="two-columns">
              <div class="input-group">
                <div class="input-icon">
                  <i class="fa-solid fa-user-tag"></i>
                </div>
                <input
                  type="text"
                  v-model="userData.lastName"
                  placeholder="名 *"
                  class="register-input"
                />
              </div>
              
              <div class="input-group">
                <div class="input-icon">
                  <i class="fa-solid fa-envelope"></i>
                </div>
                <input
                  type="email"
                  v-model="userData.email"
                  placeholder="邮箱 *"
                  class="register-input"
                />
              </div>
            </div>

            <div class="two-columns">
              <div class="input-group">
                <div class="input-icon">
                  <i class="fa-solid fa-phone"></i>
                </div>
                <input
                  type="tel"
                  v-model="userData.phone"
                  placeholder="手机号 *"
                  class="register-input"
                />
              </div>
              
              <div class="input-group gender-group">
                <div class="input-icon">
                  <i class="fa-solid fa-venus-mars"></i>
                </div>
                <div class="gender-options">
                  <label class="gender-label">
                    <input type="radio" v-model="userData.gender" value="0" />
                    <span>女</span>
                  </label>
                  <label class="gender-label">
                    <input type="radio" v-model="userData.gender" value="1" />
                    <span>男</span>
                  </label>
                </div>
              </div>
            </div>

            <div class="two-columns">
              <div class="input-group">
                <div class="input-icon">
                  <i class="fa-solid fa-lock"></i>
                </div>
                <input
                  type="password"
                  v-model="userData.password"
                  placeholder="密码 *"
                  class="register-input"
                />
              </div>
              
              <div class="input-group">
                <div class="input-icon">
                  <i class="fa-solid fa-lock"></i>
                </div>
                <input
                  type="password"
                  v-model="confirmPassword"
                  placeholder="确认密码 *"
                  class="register-input"
                />
              </div>
            </div>

            <div class="password-hint">
              <i class="fa-solid fa-info-circle"></i>
              密码至少8位，包含大小写字母和数字
            </div>

            <button
              class="register-btn"
              @click="register"
              :disabled="loading"
            >
              <span v-if="!loading">注 册</span>
              <span v-else class="loading-spinner"></span>
            </button>

            <div class="login-link">
              已有账号？
              <router-link to="/login">立即登录</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import auth from '../api/auth'
import request from '../api/request'
import { toast } from '../utils/toast'

const router = useRouter()
const fileInput = ref(null)
const avatar = ref(null)
const uploadedFile = ref(null)
const loading = ref(false)
const confirmPassword = ref('')

const userData = reactive({
  username: '',
  password: '',
  email: '',
  firstName: '',
  lastName: '',
  phone: '',
  gender: '0',
  photo: ''
})

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    uploadedFile.value = file
    const reader = new FileReader()
    reader.onload = (e) => {
      avatar.value = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const validate = () => {
  if (!userData.username.trim()) {
    toast.warning('请输入用户名')
    return false
  }
  if (!userData.firstName.trim()) {
    toast.warning('请输入姓')
    return false
  }
  if (!userData.lastName.trim()) {
    toast.warning('请输入名')
    return false
  }
  if (!userData.email || !emailRegex.test(userData.email)) {
    toast.warning('请输入有效的邮箱')
    return false
  }
  if (!userData.phone) {
    toast.warning('请输入手机号')
    return false
  }
  if (!userData.password || !passwordRegex.test(userData.password)) {
    toast.warning('密码至少8位，需包含大小写字母和数字')
    return false
  }
  if (userData.password !== confirmPassword.value) {
    toast.warning('两次输入的密码不一致')
    return false
  }
  return true
}

const register = async () => {
  if (!validate()) {
    return
  }

  loading.value = true
  try {
    if (uploadedFile.value) {
      const formData = new FormData()
      formData.append('file', uploadedFile.value)
      const uploadRes = await request.post('/api/upload', formData)
      if (uploadRes?.success && uploadRes?.data) {
        userData.photo = uploadRes.data
      }
    }

    const response = await auth.register({
      username: userData.username.trim(),
      password: userData.password,
      email: userData.email,
      firstName: userData.firstName,
      lastName: userData.lastName,
      phone: userData.phone,
      gender: userData.gender,
      photo: userData.photo
    })

    if (response?.success) {
      toast.success('注册成功！')
      router.push('/login')
    } else {
      toast.warning(response?.message || '注册失败，请重试')
    }
  } catch (error) {
    console.error('注册错误:', error)
    const errorMsg = error.response?.data?.message || '注册失败，请检查网络'
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

.register-page {
  position: relative;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #1a5bbf, #00a8e8);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

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
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.12), transparent 70%);
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
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.06), transparent 80%);
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
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08), transparent);
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
  background: radial-gradient(circle, rgba(255, 255, 255, 0.06), transparent);
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
.register-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 500px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0 20px;
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

/* 头像上传区域 */
.avatar-section {
  text-align: center;
  padding: 50px 0 10px;
  flex-shrink: 0;
}

.avatar-upload {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.avatar-upload:hover {
  border-color: white;
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.02);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
  font-size: 12px;
}

.upload-placeholder i {
  font-size: 32px;
  margin-bottom: 6px;
}

.avatar-upload img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 10px;
}

/* 表单滚动区域 */
.form-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
  scrollbar-width: thin;
}

.form-scroll-area::-webkit-scrollbar {
  width: 4px;
}

.form-scroll-area::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.form-scroll-area::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

/* 表单卡片 */
.form-section {
  background: white;
  border-radius: 28px;
  padding: 24px 24px 28px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
  margin-bottom: 10px;
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
  margin-bottom: 24px;
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
  width: 25px;
  height: 2px;
  background: linear-gradient(90deg, #3a7bd5, #00d2ff);
}

.greeting::before {
  right: 100%;
  margin-right: 10px;
}

.greeting::after {
  left: 100%;
  margin-left: 10px;
}

/* 两列布局 - 统一间距 */
.two-columns {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}

.two-columns .input-group {
  flex: 1;
  margin-bottom: 0;
}

/* 输入框 - 统一间距 */
.input-group {
  display: flex;
  align-items: center;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: #fafbfc;
  margin-bottom: 14px;
}

.input-group:focus-within {
  border-color: #3a7bd5;
  box-shadow: 0 0 0 3px rgba(58, 123, 213, 0.1);
  background: white;
}

.input-icon {
  width: 46px;
  text-align: center;
  color: #aaa;
  font-size: 16px;
  transition: color 0.3s ease;
}

.input-group:focus-within .input-icon {
  color: #3a7bd5;
}

.register-input {
  flex: 1;
  padding: 13px 12px 13px 0;
  border: none;
  outline: none;
  font-size: 15px;
  background: transparent;
}

.register-input::placeholder {
  color: #bbb;
}

/* 性别选择 */
.gender-group {
  flex: 1;
  display: flex;
  align-items: center;
}

.gender-options {
  display: flex;
  gap: 24px;
  flex: 1;
}

.gender-label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #555;
}

.gender-label input {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #3a7bd5;
}

/* 密码提示 */
.password-hint {
  font-size: 12px;
  color: #999;
  margin-bottom: 20px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.password-hint i {
  font-size: 12px;
}

/* 注册按钮 */
.register-btn {
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

.register-btn::before {
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

.register-btn:hover::before {
  width: 300px;
  height: 300px;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(58, 123, 213, 0.4);
}

.register-btn:active {
  transform: translateY(0);
}

.register-btn:disabled {
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

/* 登录链接 */
.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #3a7bd5;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s;
}

.login-link a:hover {
  color: #00d2ff;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .register-container {
    padding: 0 16px;
  }
  
  .avatar-upload {
    width: 85px;
    height: 85px;
  }
  
  .upload-placeholder i {
    font-size: 28px;
  }
  
  .form-section {
    padding: 20px 18px 24px;
  }
  
  .two-columns {
    flex-direction: column;
    gap: 0;
  }
  
  .two-columns .input-group {
    margin-bottom: 14px;
  }
  
  .two-columns .input-group:last-child {
    margin-bottom: 0;
  }
  
  .greeting {
    font-size: 16px;
  }
  
  .greeting::before,
  .greeting::after {
    width: 15px;
  }
  
  .input-icon {
    width: 42px;
  }
  
  .register-input {
    padding: 12px 10px 12px 0;
    font-size: 14px;
  }
}
</style>