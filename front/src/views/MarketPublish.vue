<template>
  <div class="publish-page">
    <!-- header部分 - 统一蓝色渐变风格 -->
    <header>
      <div class="header-content">
        <div class="back-btn" @click="goBack">
          <i class="fa-solid fa-backward"></i>
        </div>
        <div class="page-title">
          <span>📝</span>
          <span>发布闲置</span>
        </div>
        <div class="placeholder"></div>
      </div>
    </header>

    <div class="form-container">
      <!-- 标题 -->
      <div class="form-group">
        <label class="form-label">
          标题 <span class="required">*</span>
        </label>
        <input
          type="text"
          class="form-input"
          v-model="form.title"
          placeholder="请输入物品名称"
          maxlength="50"
        />
        <div class="char-hint">{{ (form.title || '').length }}/50</div>
      </div>

      <!-- 价格 -->
      <div class="form-group">
        <label class="form-label">
          价格 <span class="required">*</span>
        </label>
        <div class="price-wrapper">
          <span class="price-sign">¥</span>
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
      <div class="form-group">
        <label class="form-label">详细描述</label>
        <textarea
          class="form-textarea"
          v-model="form.description"
          placeholder="请详细描述物品的成色、使用情况等..."
          maxlength="500"
          rows="5"
        ></textarea>
        <div class="char-hint">{{ (form.description || '').length }}/500</div>
      </div>

      <!-- 联系方式 -->
      <div class="form-group">
        <label class="form-label">
          联系方式 <span class="required">*</span>
        </label>
        <div class="contact-type">
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
          :placeholder="contactPlaceholder"
        />
        <input
          v-if="contact.type === '2'"
          type="text"
          class="form-input contact-other"
          v-model="contact.other"
          placeholder="请说明是什么联系方式"
        />
      </div>

      <!-- 图片上传 -->
      <div class="form-group">
        <label class="form-label">图片</label>
        <div class="upload-area">
          <div class="image-list">
            <div class="image-item" v-for="(url, index) in images" :key="index">
              <img :src="url" alt="图片" />
              <button class="delete-img" @click="deleteImage(index)">×</button>
            </div>
            <div v-if="images.length < 9" class="upload-box" @click="triggerFileUpload">
              <div class="upload-icon">📷</div>
              <p>上传图片</p>
            </div>
          </div>
          <p class="upload-hint">最多上传9张图片，支持jpg、png格式</p>
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

    <!-- 底部固定发布按钮 -->
    <div class="bottom-submit">
      <button class="submit-btn" @click="handleSubmit" :disabled="isSubmitting">
        {{ isSubmitting ? '发布中...' : '立即发布' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import marketApi from '../api/market'
import auth from '../api/auth'
import request from '../api/request'
import { toast } from '../utils/toast'

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
let existingContact = null

const contactPlaceholder = computed(() => {
  const map = { '0': '请输入QQ号', '1': '请输入微信号', '2': '请输入联系方式' }
  return map[contact.value.type] || '请输入联系方式'
})

const loadContact = async () => {
  try {
    const userInfo = auth.getUserInfo()
    if (userInfo && userInfo.id) {
      const res = await marketApi.getContactByUserId(userInfo.id)
      if (res && res.success && res.data && res.data.length > 0) {
        existingContact = res.data[0]
        contact.value.type = String(existingContact.type)
        contact.value.contact = existingContact.contact || ''
        contact.value.other = existingContact.other || ''
      }
    }
  } catch (e) {
    console.log('加载联系方式失败', e)
  }
}

const handleSubmit = async () => {
  if (!form.value.title?.trim()) {
    toast.warning('请输入标题')
    return
  }
  
  if (form.value.price === null || form.value.price === '' || form.value.price < 0) {
    toast.warning('请输入有效价格')
    return
  }

  if (!contact.value.contact?.trim()) {
    toast.warning('请输入联系方式')
    return
  }

  if (contact.value.type === '2' && !contact.value.other?.trim()) {
    toast.warning('请说明是什么联系方式')
    return
  }

  if (!auth.isAuthenticated()) {
    toast.warning('请先登录')
    router.push('/trade/login')
    return
  }

  isSubmitting.value = true
  
  try {
    // 保存联系方式
    const contactData = {
      contact: contact.value.contact,
      type: parseInt(contact.value.type),
      other: contact.value.type === '2' ? contact.value.other : null
    }

    if (existingContact) {
      contactData.userId = existingContact.userId
      await marketApi.updateContact(contactData)
    } else {
      await marketApi.addContact(contactData)
    }

    // 发布帖子
    const response = await marketApi.addPost({
      title: form.value.title,
      description: form.value.description,
      price: parseFloat(form.value.price),
      images: images.value.join(','),
      status: 0
    })

    if (response && response.success) {
      toast.warning('发布成功！')
      router.back()
    } else {
      toast.warning(response?.message || '发布失败，请重试')
    }
  } catch (error) {
    console.error('发布失败:', error)
    toast.warning('发布失败，请重试')
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
      toast.warning('请上传图片文件')
      continue
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.warning('文件大小不能超过5MB')
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
    toast.warning('图片上传失败，请重试')
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
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.publish-page {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 90px;
}

/* ========== Header ========== */
.publish-page header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  padding: 12px 16px;
  z-index: 100;
}

.publish-page .header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.publish-page .back-btn {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  font-size: 24px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.publish-page .back-btn:active {
  transform: scale(0.95);
}

.publish-page .page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: white;
}

.publish-page .placeholder {
  width: 40px;
}

/* ========== 表单容器 ========== */
.publish-page .form-container {
  margin-top: 64px;
  background: white;
  border-radius: 16px;
  margin: 70px 16px 20px;
  padding: 20px;
}

/* ========== 表单项 ========== */
.publish-page .form-group {
  margin-bottom: 24px;
}

.publish-page .form-group:last-child {
  margin-bottom: 0;
}

.publish-page .form-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.publish-page .required {
  color: #ff6b6b;
}

.publish-page .form-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.3s ease;
}

.publish-page .form-input:focus {
  outline: none;
  border-color: #3a7bd5;
  box-shadow: 0 0 0 2px rgba(58, 123, 213, 0.2);
}

.publish-page .char-hint {
  text-align: right;
  font-size: 12px;
  color: #bbb;
  margin-top: 6px;
}

/* 价格输入框 */
.publish-page .price-wrapper {
  display: flex;
  align-items: center;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
}

.publish-page .price-wrapper:focus-within {
  border-color: #3a7bd5;
  box-shadow: 0 0 0 2px rgba(58, 123, 213, 0.2);
}

.publish-page .price-sign {
  padding: 0 14px;
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
  background: #fafafa;
  line-height: 48px;
}

.publish-page .price-input {
  border: none !important;
  flex: 1;
}

.publish-page .price-input:focus {
  box-shadow: none;
}

/* 文本域 */
.publish-page .form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  font-size: 15px;
  resize: vertical;
  font-family: inherit;
  transition: all 0.3s ease;
}

.publish-page .form-textarea:focus {
  outline: none;
  border-color: #3a7bd5;
  box-shadow: 0 0 0 2px rgba(58, 123, 213, 0.2);
}

/* 联系方式 */
.publish-page .contact-type {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.publish-page .radio-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #555;
  cursor: pointer;
}

.publish-page .radio-item input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.publish-page .contact-other {
  margin-top: 12px;
}

/* 图片上传 */
.publish-page .upload-area {
  width: 100%;
}

.publish-page .image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.publish-page .image-item {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
}

.publish-page .image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.publish-page .delete-img {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  color: white;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.publish-page .delete-img:active {
  transform: scale(0.9);
}

.publish-page .upload-box {
  width: 100px;
  height: 100px;
  border: 2px dashed #ddd;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.publish-page .upload-box:hover {
  border-color: #3a7bd5;
  background: #f0f7ff;
}

.publish-page .upload-icon {
  font-size: 32px;
  margin-bottom: 4px;
}

.publish-page .upload-box p {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.publish-page .upload-hint {
  font-size: 12px;
  color: #bbb;
  margin-top: 12px;
}

/* ========== 底部固定发布按钮 ========== */
.publish-page .bottom-submit {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.publish-page .submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.publish-page .submit-btn:active {
  transform: scale(0.98);
}

.publish-page .submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* ========== 大屏幕适配 ========== */
@media (min-width: 768px) {
  .publish-page {
    max-width: 480px;
    margin: 0 auto;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  }
  
  .publish-page header {
    max-width: 480px;
    left: 50%;
    transform: translateX(-50%);
    right: auto;
  }
  
  .publish-page .bottom-submit {
    max-width: 480px;
    left: 50%;
    transform: translateX(-50%);
    right: auto;
  }
}
</style>