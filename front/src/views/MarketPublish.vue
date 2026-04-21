<template>
  <div class="page publish-page">
    <div class="header-bar">
      <button class="back-btn" @click="goBack">←</button>
      <h1 class="page-title">发布闲置</h1>
      <button class="submit-btn" @click="handleSubmit" :disabled="isSubmitting">
        {{ isSubmitting ? '发布中...' : '发布' }}
      </button>
    </div>

    <div class="form-container">
      <!-- 标题 -->
      <div class="form-item">
        <label class="form-label">标题 <span class="required">*</span></label>
        <input
          type="text"
          class="form-input"
          v-model="form.title"
          placeholder="请输入物品名称"
          maxlength="50"
        />
      </div>

      <!-- 价格 -->
      <div class="form-item">
        <label class="form-label">价格 <span class="required">*</span></label>
        <div class="price-input-wrapper">
          <span class="price-prefix">¥</span>
          <input
            type="number"
            class="form-input price-input"
            v-model="form.price"
            placeholder="0.00"
            step="0.01"
            min="0"
          />
        </div>
      </div>

      <!-- 描述 -->
      <div class="form-item">
        <label class="form-label">详细描述</label>
        <textarea
          class="form-textarea"
          v-model="form.description"
          placeholder="请详细描述物品的成色、使用情况等..."
          maxlength="500"
          rows="6"
        ></textarea>
        <div class="char-count">{{ (form.description || '').length }}/500</div>
      </div>

      <!-- 联系方式 -->
      <div class="form-item">
        <label class="form-label">联系方式 <span class="required">*</span></label>
        <div style="display: flex; flex-direction: column; gap: 12px;">
          <div style="display: flex; gap: 8px;">
            <label class="radio-item">
              <input type="radio" v-model="contact.type" value="0" /> QQ
            </label>
            <label class="radio-item">
              <input type="radio" v-model="contact.type" value="1" /> 微信
            </label>
            <label class="radio-item">
              <input type="radio" v-model="contact.type" value="2" /> 其他
            </label>
          </div>
          <input
            type="text"
            class="form-input"
            v-model="contact.contact"
            placeholder="请输入您的联系方式"
          />
          <input
            v-if="contact.type === '2'"
            type="text"
            class="form-input"
            v-model="contact.other"
            placeholder="请说明是什么联系方式"
          />
        </div>
      </div>

      <!-- 图片上传 -->
      <div class="form-item">
        <label class="form-label">图片</label>
        <div class="upload-area">
          <div class="image-preview-list">
            <div class="image-preview-item" v-for="(url, index) in images" :key="index">
              <img :src="url" alt="图片" />
              <button class="delete-btn" @click="deleteImage(index)">×</button>
            </div>
            <div v-if="images.length < 9" class="upload-btn" @click="triggerFileUpload">
              <div class="upload-icon">📷</div>
              <p class="upload-text">上传图片</p>
            </div>
          </div>
          <input
            type="file"
            ref="fileInput"
            @change="handleFileUpload"
            accept="image/*"
            multiple
            style="display: none"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import marketApi from '../api/market'
import auth from '../api/auth'
import request from '../api/request'

const router = useRouter()

const isSubmitting = ref(false)
const form = ref({
  title: '',
  price: null,
  description: ''
})
const contact = ref({
  type: '1',
  contact: '',
  other: ''
})
const images = ref([])
const fileInput = ref(null)
let existingContact = null // 存储用户已有的联系方式记录

const loadContact = async () => {
  try {
    const userInfo = auth.getUserInfo()
    if (userInfo && userInfo.id) {
      const res = await marketApi.getContactByUserId(userInfo.id)
      if (res && res.success && res.data && res.data.length > 0) {
        existingContact = res.data[0]
        contact.value.type = String(existingContact.type)
        contact.value.contact = existingContact.contact
        contact.value.other = existingContact.other || ''
      }
    }
  } catch (e) {
    console.log('加载联系方式失败', e)
  }
}

const handleSubmit = async () => {
  if (!form.value.title?.trim()) {
    alert('请输入标题')
    return
  }
  
  if (form.value.price === null || form.value.price === '' || form.value.price < 0) {
    alert('请输入有效价格')
    return
  }

  if (!contact.value.contact?.trim()) {
    alert('请输入联系方式')
    return
  }

  if (contact.value.type === '2' && !contact.value.other?.trim()) {
    alert('请说明是什么联系方式')
    return
  }

  if (!auth.isAuthenticated()) {
    alert('请先登录')
    router.push('/login')
    return
  }

  isSubmitting.value = true
  
  try {
    // 先保存联系方式
    const contactData = {
      contact: contact.value.contact,
      type: parseInt(contact.value.type),
      other: contact.value.type === '2' ? contact.value.other : null
    }

    // 如果有现有记录，就更新；否则新增
    let contactRes
    if (existingContact) {
      contactData.userId = existingContact.userId // 确保有用户ID
      contactRes = await marketApi.updateContact(contactData)
    } else {
      contactRes = await marketApi.addContact(contactData)
    }

    console.log('保存联系方式:', contactRes)

    // 然后发布帖子
    const response = await marketApi.addPost({
      title: form.value.title,
      description: form.value.description,
      price: parseFloat(form.value.price),
      images: images.value.join(','),
      status: 0
    })

    console.log('发布响应:', response)

    if (response && response.success) {
      alert('发布成功！')
      router.back()
    } else {
      alert(response?.message || '发布失败，请重试')
    }
  } catch (error) {
    console.error('发布失败:', error)
    console.error('错误详细:', {
      message: error.message,
      response: error.response,
      data: error.response?.data
    })
    alert('发布失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

const triggerFileUpload = () => {
  fileInput.value.click()
}

const handleFileUpload = async (event) => {
  const files = event.target.files
  if (!files || files.length === 0) return

  const remaining = 9 - images.value.length
  const filesToUpload = Array.from(files).slice(0, remaining)

  for (const file of filesToUpload) {
    if (!file.type.startsWith('image/')) {
      alert('请上传图片文件')
      continue
    }

    if (file.size > 5 * 1024 * 1024) {
      alert('文件大小不能超过5MB')
      continue
    }

    await uploadSingleFile(file)
  }

  event.target.value = ''
}

const uploadSingleFile = async (file) => {
  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await request.post('/api/upload', formData)
    if (response.success && response.data) {
      images.value.push(response.data)
    } else {
      throw new Error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    alert('图片上传失败，请重试')
  }
}

const deleteImage = (index) => {
  images.value.splice(index, 1)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadContact()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 60px 16px 32px;
  background: #f5f5f5;
}

.header-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.back-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 4px;
  color: #333;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-top: 20px;
}

.form-item {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.required {
  color: #e74c3c;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  box-sizing: border-box;
}

.price-input-wrapper {
  display: flex;
  align-items: center;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.price-prefix {
  padding: 0 12px;
  font-size: 16px;
  font-weight: 500;
  color: #e74c3c;
  background: #fafafa;
}

.price-input {
  border: none !important;
  flex: 1;
}

.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  resize: none;
  box-sizing: border-box;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.upload-area {
  width: 100%;
}

.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-preview-item {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.delete-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border-radius: 50%;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed #e0e0e0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #999;
}

.upload-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 4px;
}

.upload-text {
  font-size: 12px;
  margin: 0;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}

.radio-item input {
  cursor: pointer;
}
</style>
