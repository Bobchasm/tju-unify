<template>
  <div class="page profile-page">
    <!-- header部分 - 统一蓝色渐变风格 -->
    <header>
      <div class="header-content">
        <div class="back-btn" @click="goBack">
          <i class="fa-solid fa-backward"></i>
        </div>
        <div class="page-title">
          <span>个人信息</span>
        </div>
        <div class="placeholder"></div>
      </div>
    </header>

    <div v-if="loading" class="loading">
      <span>⏳</span>
      <p>加载中...</p>
    </div>

    <div v-if="errorMessage" class="error-message">
      <span>⚠️</span>
      <p>{{ errorMessage }}</p>
    </div>

    <div v-if="userInfo && !loading" class="profile-content">
      <!-- 用户卡片 -->
      <div class="user-card">
        <div class="avatar" @click="triggerFileUpload">
          <img 
            v-if="userInfo.photo && !imageLoadFailed"
            :src="userInfo.photo" 
            alt="头像"
            @error="imageLoadFailed = true"
          />
          <span v-else class="avatar-placeholder">👤</span>
          <div class="avatar-overlay">
            <span>📷 更换头像</span>
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

      <!-- 信息列表 -->
      <div class="info-section">
        <div class="section-header">
          <div class="section-line"></div>
          <h3>基本信息</h3>
          <div class="section-line"></div>
        </div>
        <div class="info-item">
          <span class="info-label">📱 手机号</span>
          <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">📧 邮箱</span>
          <span class="info-value">{{ userInfo.email || '未设置' }}</span>
        </div>
      </div>

      <!-- 按钮区域 -->
      <button class="edit-btn" @click="openEditModal">
        ✏️ 编辑个人信息
      </button>

      <button class="logout-btn" @click="logout">
        🚪 退出登录
      </button>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>✏️ 编辑个人信息</h3>
          <button class="modal-close" @click="closeEditModal">×</button>
        </div>
        <div class="modal-body">
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
            <input v-model="editForm.phone" placeholder="输入手机号" type="tel" />
          </div>
          <div class="modal-item">
            <label>邮箱</label>
            <input v-model="editForm.email" placeholder="输入邮箱" type="email" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeEditModal">取消</button>
          <button class="submit-btn" @click="submitEdits">确认修改</button>
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
import { toast } from '../utils/toast'

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
    toast.warning('请上传图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    toast.warning('文件大小不能超过5MB')
    return
  }

  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await request.post('/api/upload', formData)
    if (response.success && response.data) {
      userInfo.value.photo = response.data
      await updateUserProfile()
      toast.warning('头像上传成功')
    } else {
      throw new Error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    toast.warning('上传失败，请重试')
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
      toast.warning('修改成功')
      closeEditModal()
    } else {
      toast.warning('修改失败，请重试')
    }
  } catch (error) {
    console.error('修改失败:', error)
    toast.warning('修改失败，请重试')
  }
}

const logout = () => {
  auth.logout()
  router.push('/')
}

onMounted(() => {
  if (!auth.isAuthenticated()) {
    router.push('/trade/login')
    return
  }
  loadUserData()
})
</script>

<style scoped>
/****************** 全局样式 ******************/
.profile-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 0;
  padding-bottom: 14vw;
}

/****************** header部分 - 统一蓝色渐变 ******************/
.profile-page header {
  width: 100%;
  margin: 0;
  border-radius: 0;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  padding: 5vw 4vw;
  box-sizing: border-box;
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.3);
}

.profile-page header .header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;  /* 改回 space-between */
}

.profile-page header .back-btn {
  width: 10vw;
  height: 10vw;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.profile-page header .back-btn span {
  font-size: 6vw;
  color: white;
  font-weight: bold;
}

.profile-page header .page-title {
  display: flex;
  align-items: center;
  gap: 2vw;
  /* 关键：让标题在剩余空间中居中 */
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
}

.profile-page header .placeholder {
  width: 10vw;
  flex-shrink: 0;
  visibility: hidden;
}

/****************** 加载和错误状态 ******************/
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20vw 0;
  color: #999;
}

.loading span {
  font-size: 12vw;
  animation: spin 1s linear infinite;
}

.loading p {
  font-size: 4vw;
  margin-top: 4vw;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3vw;
  margin: 10vw 4vw;
  padding: 4vw;
  background: #ffebee;
  color: #e74c3c;
  border-radius: 3vw;
  font-size: 3.8vw;
}

.error-message span {
  font-size: 6vw;
}

/****************** 内容区域 ******************/
.profile-content {
  padding: 4vw;
}

/****************** 用户卡片 ******************/
.user-card {
  background: white;
  border-radius: 4vw;
  padding: 6vw 4vw;
  margin-bottom: 4vw;
  display: flex;
  align-items: center;
  gap: 5vw;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.avatar {
  width: 25vw;
  height: 25vw;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
  background: linear-gradient(135deg, #e0e0e0, #f0f0f0);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 15vw;
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
  border-radius: 50%;
}

.avatar:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay span {
  color: white;
  font-size: 3vw;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 5vw;
  font-weight: 700;
  color: #333;
  margin-bottom: 2vw;
}

.user-full-name {
  font-size: 3.5vw;
  color: #999;
}

/****************** 信息区域 ******************/
.info-section {
  background: white;
  border-radius: 4vw;
  margin-bottom: 5vw;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4vw 4vw 2vw;
}

.section-header .section-line {
  width: 8vw;
  height: 0.3vw;
  background: linear-gradient(90deg, #3a7bd5, #00d2ff);
  border-radius: 0.3vw;
}

.section-header h3 {
  font-size: 4vw;
  font-weight: 600;
  color: #333;
  margin: 0 4vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4vw 5vw;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #666;
  font-size: 3.8vw;
  display: flex;
  align-items: center;
  gap: 2vw;
}

.info-value {
  color: #333;
  font-size: 3.8vw;
  font-weight: 500;
}

/****************** 按钮样式 ******************/
.edit-btn {
  width: 100%;
  padding: 4vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
  border: none;
  border-radius: 4vw;
  font-size: 4vw;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 3vw;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2vw;
}

.edit-btn:active {
  transform: scale(0.98);
}

.logout-btn {
  width: 100%;
  padding: 4vw;
  background: white;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  border-radius: 4vw;
  font-size: 4vw;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2vw;
}

.logout-btn:active {
  transform: scale(0.98);
  background: #fff5f5;
}

/****************** 弹窗样式 ******************/
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
  padding: 4vw;
}

.modal-content {
  background: white;
  border-radius: 4vw;
  width: 100%;
  max-width: 400px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4vw 5vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
}

.modal-header h3 {
  color: white;
  font-size: 4.5vw;
  font-weight: 600;
  margin: 0;
}

.modal-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 8vw;
  height: 8vw;
  border-radius: 50%;
  font-size: 6vw;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:active {
  background: rgba(255, 255, 255, 0.3);
}

.modal-body {
  padding: 5vw;
}

.modal-item {
  margin-bottom: 4vw;
}

.modal-item label {
  display: block;
  font-weight: 500;
  color: #555;
  margin-bottom: 2vw;
  font-size: 3.5vw;
}

.modal-item input {
  width: 100%;
  padding: 3vw 4vw;
  border: 1px solid #e0e0e0;
  border-radius: 2vw;
  font-size: 3.8vw;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.modal-item input:focus {
  outline: none;
  border-color: #3a7bd5;
  box-shadow: 0 0 0 2px rgba(58, 123, 213, 0.2);
}

.modal-footer {
  display: flex;
  gap: 3vw;
  padding: 4vw 5vw 5vw;
}

.modal-footer button {
  flex: 1;
  padding: 3vw;
  border: none;
  border-radius: 3vw;
  font-size: 3.8vw;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  /* 关键：固定高度，不管文字多少 */
  height: 12vw;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.submit-btn {
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
}

.submit-btn:active {
  transform: scale(0.98);
}

.cancel-btn {
  background: #f0f0f0;
  color: #666;
}

.cancel-btn:active {
  transform: scale(0.98);
}

/****************** 响应式适配 - 大屏幕 ******************/
@media (min-width: 768px) {
  .profile-page {
    padding-bottom: 70px;
  }
  
  .profile-page header .back-btn {
    width: 44px;
    height: 44px;
  }
  
  .profile-page header .back-btn span {
    font-size: 24px;
  }
  
  .profile-page header .page-title .title-icon {
    font-size: 28px;
  }
  
  .profile-page header .page-title span:last-child {
    font-size: 22px;
  }
  
  .profile-page header .placeholder {
    width: 44px;
  }
  
  .avatar {
    width: 100px;
    height: 100px;
  }
  
  .avatar-placeholder {
    font-size: 60px;
  }
  
  .avatar-overlay span {
    font-size: 12px;
  }
  
  .user-name {
    font-size: 20px;
  }
  
  .user-full-name {
    font-size: 14px;
  }
  
  .info-label, .info-value {
    font-size: 16px;
  }
  
  .edit-btn, .logout-btn {
    padding: 14px;
    font-size: 16px;
  }
  
  .modal-header {
    padding: 16px 20px;
  }
  
  .modal-header h3 {
    font-size: 18px;
  }
  
  .modal-close {
    width: 32px;
    height: 32px;
    font-size: 24px;
  }
  
  .modal-body {
    padding: 20px;
  }
  
  .modal-item {
    margin-bottom: 16px;
  }
  
  .modal-item label {
    font-size: 14px;
    margin-bottom: 8px;
  }
  
  .modal-item input {
    padding: 12px 16px;
    font-size: 14px;
  }
  
  .modal-footer {
    padding: 16px 20px 20px;
  }
  
  .modal-footer button {
    padding: 12px;
    font-size: 14px;
    height: 48px;
  }
}
</style>