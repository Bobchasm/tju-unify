<template>
  <div class="page profile-page">
    <div class="header-bar">
      <button class="back-btn" @click="goBack">←</button>
      <h1 class="page-title">个人信息</h1>
    </div>

    <div v-if="loading" class="loading">
      加载中...
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="userInfo && !loading" class="profile-content">
      <div class="user-card">
        <div class="avatar" @click="triggerFileUpload" style="cursor: pointer;">
          <img 
            v-if="userInfo.photo && !imageLoadFailed"
            :src="userInfo.photo" 
            alt="头像"
            @error="imageLoadFailed = true"
          />
          <span v-else style="font-size: 40px;">👤</span>
          <div class="avatar-overlay">
            <span>点击更换</span>
          </div>
        </div>
        <input
          type="file"
          ref="fileInput"
          @change="handleFileUpload"
          accept="image/*"
          style="display: none"
        />
        <div class="user-details">
          <div class="user-name">
            {{ userInfo.username || '未设置昵称' }}
          </div>
          <div class="user-full-name">
            {{ userInfo.firstName || '' }} {{ userInfo.lastName || '' }}
          </div>
        </div>
      </div>

      <div class="info-section">
        <div class="info-item">
          <span class="info-label">手机号</span>
          <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">邮箱</span>
          <span class="info-value">{{ userInfo.email || '未设置' }}</span>
        </div>
      </div>

      <button class="edit-btn" @click="openEditModal">
        编辑个人信息
      </button>

      <button class="logout-btn" @click="logout">
        退出登录
      </button>
    </div>

    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-content">
        <h3>编辑个人信息</h3>
        <div class="modal-item">
          <label>姓氏</label>
          <input v-model="editForm.firstName" placeholder="输入姓氏" />
        </div>
        <div class="modal-item">
          <label>名字</label>
          <input v-model="editForm.lastName" placeholder="输入名字" />
        </div>
        <div class="modal-item">
          <label>手机号</label>
          <input v-model="editForm.phone" placeholder="输入手机号" />
        </div>
        <div class="modal-item">
          <label>邮箱</label>
          <input v-model="editForm.email" placeholder="输入邮箱" type="email" />
        </div>
        <div class="modal-buttons">
          <button class="submit-btn" @click="submitEdits">提交</button>
          <button class="cancel-btn" @click="closeEditModal">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import auth from '../api/auth'
import request from '../api/request'

const router = useRouter()
const userInfo = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const showEditModal = ref(false)
const fileInput = ref(null)
const imageLoadFailed = ref(false)
const editForm = ref({
  firstName: '',
  lastName: '',
  phone: '',
  email: ''
})

const goBack = () => {
  router.back()
}

const triggerFileUpload = () => {
  fileInput.value.click()
}

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    alert('请上传图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    alert('文件大小不能超过5MB')
    return
  }

  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await request.post('/api/upload', formData)
    if (response.success && response.data) {
      userInfo.value.photo = response.data
      await updateUserProfile()
      alert('头像上传成功')
    } else {
      throw new Error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    alert('上传失败，请重试')
  } finally {
    event.target.value = ''
  }
}

const updateUserProfile = async () => {
  try {
    const response = await request.put('/api/person/info', {
      id: userInfo.value.id,
      firstName: userInfo.value.firstName,
      lastName: userInfo.value.lastName,
      email: userInfo.value.email,
      phone: userInfo.value.phone,
      photo: userInfo.value.photo
    })
    if (response.success) {
      auth.setUserInfo(userInfo.value)
    }
  } catch (error) {
    console.error('更新失败:', error)
  }
}

const loadUserData = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await request.get('/api/person')
    if (response.success) {
      userInfo.value = response.data
      auth.setUserInfo(response.data)
    } else {
      errorMessage.value = '获取用户信息失败'
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    errorMessage.value = '获取用户信息失败，请重试'
  } finally {
    loading.value = false
  }
}

const openEditModal = () => {
  if (userInfo.value) {
    editForm.value.firstName = userInfo.value.firstName || ''
    editForm.value.lastName = userInfo.value.lastName || ''
    editForm.value.phone = userInfo.value.phone || ''
    editForm.value.email = userInfo.value.email || ''
  }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
}

const submitEdits = async () => {
  try {
    const response = await request.put('/api/person/info', {
      id: userInfo.value.id,
      firstName: editForm.value.firstName,
      lastName: editForm.value.lastName,
      email: editForm.value.email,
      phone: editForm.value.phone,
      photo: userInfo.value.photo
    })
    if (response.success) {
      userInfo.value.firstName = editForm.value.firstName
      userInfo.value.lastName = editForm.value.lastName
      userInfo.value.email = editForm.value.email
      userInfo.value.phone = editForm.value.phone
      auth.setUserInfo(userInfo.value)
      alert('修改成功')
      closeEditModal()
    } else {
      alert('修改失败，请重试')
    }
  } catch (error) {
    console.error('修改失败:', error)
    alert('修改失败，请重试')
  }
}

const logout = () => {
  auth.logout()
  router.push('/')
}

onMounted(() => {
  if (!auth.isAuthenticated()) {
    router.push('/login')
    return
  }
  loadUserData()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 60px 16px 80px;
  background: #f5f5f5;
}

.header-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  display: flex;
  align-items: center;
  padding: 0 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.back-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  padding: 8px;
  color: #333;
}

.page-title {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.error-message {
  text-align: center;
  padding: 20px;
  background: #ffebee;
  color: #e74c3c;
  border-radius: 8px;
  margin: 20px;
}

.profile-content {
  max-width: 500px;
  margin: 0 auto;
}

.user-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay span {
  color: white;
  font-size: 12px;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.user-full-name {
  font-size: 14px;
  color: #666;
}

.info-section {
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #666;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.edit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 12px;
}

.logout-btn {
  width: 100%;
  padding: 14px;
  background: #fff;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 12px;
  max-width: 400px;
  width: 85%;
  box-sizing: border-box;
}

.modal-content h3 {
  margin-top: 0;
  color: #333;
  margin-bottom: 20px;
}

.modal-item {
  margin-bottom: 16px;
}

.modal-item label {
  display: block;
  font-weight: 500;
  color: #555;
  margin-bottom: 6px;
  font-size: 14px;
}

.modal-content input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
}

.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
}

.modal-buttons button {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.submit-btn {
  background: #667eea;
  color: white;
}

.cancel-btn {
  background: #e0e0e0;
  color: #333;
}
</style>
