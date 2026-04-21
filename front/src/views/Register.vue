<template>
  <div class="register-page">
    <div class="header">
      <h1>天大校园助手</h1>
    </div>
    <div class="form-container">
      <div class="form-box">
        <h2>注册</h2>
        <div class="form-item">
          <label>用户名 *</label>
          <input
            type="text"
            v-model="userData.username"
            placeholder="请输入用户名"
            maxlength="20"
          />
          <span class="word-count">{{ userData.username.length }}/20</span>
        </div>
        <div class="form-item">
          <label>姓 *</label>
          <input
            type="text"
            v-model="userData.firstName"
            placeholder="请输入姓"
          />
        </div>
        <div class="form-item">
          <label>名 *</label>
          <input
            type="text"
            v-model="userData.lastName"
            placeholder="请输入名"
          />
        </div>
        <div class="form-item">
          <label>邮箱 *</label>
          <input
            type="email"
            v-model="userData.email"
            placeholder="请输入邮箱"
          />
        </div>
        <div class="form-item">
          <label>手机号 *</label>
          <input
            type="tel"
            v-model="userData.phone"
            placeholder="请输入手机号"
          />
        </div>
        <div class="form-item">
          <label>性别</label>
          <div class="gender-group">
            <label>
              <input
                type="radio"
                v-model="userData.gender"
                value="0"
              />
              女
            </label>
            <label>
              <input
                type="radio"
                v-model="userData.gender"
                value="1"
              />
              男
            </label>
          </div>
        </div>
        <div class="form-item">
          <label>头像</label>
          <div class="avatar-upload" @click="triggerFileInput">
            <div v-if="!avatar" class="upload-placeholder">
              <span class="plus-icon">+</span>
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
        </div>
        <div class="form-item">
          <label>密码 *</label>
          <input
            type="password"
            v-model="userData.password"
            placeholder="请输入密码"
          />
          <span class="hint">密码至少8位，包含大小写字母和数字</span>
        </div>
        <div class="form-item">
          <label>确认密码 *</label>
          <input
            type="password"
            v-model="confirmPassword"
            placeholder="请再次输入密码"
          />
        </div>
        <button
          class="submit-btn"
          @click="register"
          :disabled="loading"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
        <div class="link">
          已有账号？<router-link to="/login">立即登录</router-link>
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
    alert('请输入用户名')
    return false
  }
  if (!userData.firstName.trim()) {
    alert('请输入姓')
    return false
  }
  if (!userData.lastName.trim()) {
    alert('请输入名')
    return false
  }
  if (!userData.email || !emailRegex.test(userData.email)) {
    alert('请输入有效的邮箱')
    return false
  }
  if (!userData.phone) {
    alert('请输入手机号')
    return false
  }
  if (!userData.password || !passwordRegex.test(userData.password)) {
    alert('密码至少8位，需包含大小写字母和数字')
    return false
  }
  if (userData.password !== confirmPassword.value) {
    alert('两次输入的密码不一致')
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
      alert('注册成功！')
      router.push('/login')
    } else {
      alert(response?.message || '注册失败，请重试')
    }
  } catch (error) {
    console.error('注册错误:', error)
    const errorMsg = error.response?.data?.message || '注册失败，请检查网络'
    alert(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
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
  max-width: 400px;
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
  position: relative;
}

.form-item label {
  display: block;
  color: #555;
  margin-bottom: 8px;
  font-size: 14px;
}

.form-item input[type="text"],
.form-item input[type="password"],
.form-item input[type="email"],
.form-item input[type="tel"] {
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

.word-count {
  position: absolute;
  right: 10px;
  bottom: -20px;
  font-size: 12px;
  color: #999;
}

.hint {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.gender-group {
  display: flex;
  gap: 20px;
}

.gender-group label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.gender-group input[type="radio"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.avatar-upload {
  width: 100px;
  height: 100px;
  border: 2px dashed #ccc;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: border-color 0.3s;
  overflow: hidden;
}

.avatar-upload:hover {
  border-color: #667eea;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #999;
  font-size: 12px;
}

.upload-placeholder .plus-icon {
  font-size: 28px;
  margin-bottom: 4px;
}

.avatar-upload img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  margin-top: 10px;
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
