<template>
  <div class="page detail-page">
    <div class="header-bar">
      <button class="back-btn" @click="goBack">←</button>
      <h1 class="page-title">详情</h1>
      <button class="more-btn" @click="showMoreMenu">...</button>
    </div>

    <div v-if="post" class="detail-content">
      <!-- 图片区域 -->
      <div class="detail-image">
        <div v-if="imageList.length > 0" class="image-slider">
          <img :src="imageList[currentImageIndex]" alt="商品图片" />
          <div v-if="imageList.length > 1" class="slider-dots">
            <span 
              v-for="(url, index) in imageList" 
              :key="index"
              :class="['dot', { active: index === currentImageIndex }]"
              @click="currentImageIndex = index"
            ></span>
          </div>
        </div>
        <span v-else class="placeholder-icon">📦</span>
      </div>

      <!-- 商品信息 -->
      <div class="detail-info">
        <div class="price-section">
          <span class="price-symbol">¥</span>
          <span class="price-value">{{ post.price }}</span>
          <span class="post-status" :class="'status-' + post.status">
            {{ getStatusText(post.status) }}
          </span>
        </div>
        <h2 class="detail-title">{{ post.title }}</h2>
        <p class="detail-time">更新于 {{ formatTime(post.updateTime) }}</p>
      </div>

      <!-- 商品描述 -->
      <div class="detail-desc">
        <h3 class="section-title">商品描述</h3>
        <p class="desc-text">{{ post.description || '暂无描述' }}</p>
      </div>

      <!-- 卖家信息 -->
      <div class="seller-info">
        <div class="seller-avatar">👤</div>
        <div class="seller-name">天大学生</div>
      </div>

      <!-- 交易请求区域 -->
      <div class="section-box" v-if="requests.length > 0">
        <h3 class="section-title">交易请求</h3>
        <div class="request-list">
          <div class="request-item" v-for="req in requests" :key="req.id">
            <div class="request-info">
              <span class="request-user">买家</span>
              <span class="request-status" :class="'status-' + req.status">
                {{ getRequestStatusText(req.status) }}
              </span>
            </div>
            <div class="request-time">{{ formatTime(req.createTime) }}</div>
            <div class="request-actions" v-if="req.status === 0">
              <button 
                v-if="isSeller" 
                class="request-btn agree-btn" 
                @click="handleAgree(req.id)"
              >同意</button>
              <button 
                v-if="isSeller" 
                class="request-btn reject-btn" 
                @click="handleReject(req.id)"
              >拒绝</button>
              <button 
                v-if="isCurrentUser(req.buyerId)" 
                class="request-btn cancel-btn" 
                @click="handleCancel(req.id)"
              >取消</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 评论区域 -->
      <div class="section-box comment-section">
        <h3 class="section-title">评论</h3>
        <div class="comment-input-box" v-if="showCommentInput">
          <textarea 
            class="comment-input" 
            v-model="newComment" 
            placeholder="写下你的评论..."
            maxlength="200"
          ></textarea>
          <button class="submit-comment-btn" @click="handleSubmitComment">发布</button>
        </div>
        <div class="comment-list">
          <div class="comment-item" v-for="cmt in comments" :key="cmt.id">
            <div class="comment-header">
              <span class="comment-user">{{ cmt.username || '用户' + cmt.userId }}</span>
              <span class="comment-time">{{ formatTime(cmt.createTime) }}</span>
            </div>
            <div class="comment-content">{{ cmt.content }}</div>
          </div>
          <div v-if="comments.length === 0" class="empty-tip">暂无评论</div>
        </div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-action">
      <button class="action-btn comment-btn" @click="scrollToComments">
        <span>评论</span>
      </button>
      <button class="action-btn buy-btn" @click="handleBuy" :disabled="cannotBuy">
        <span>我想要</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import marketApi from '../api/market'
import auth from '../api/auth'

const router = useRouter()
const route = useRoute()

const post = ref(null)
const requests = ref([])
const comments = ref([])
const newComment = ref('')
const showCommentInput = ref(false)
const currentImageIndex = ref(0)
const imageList = computed(() => {
  if (!post.value || !post.value.images) return []
  return post.value.images.split(',').filter(url => url.trim())
})

const userInfo = auth.getUserInfo()
const isAuthenticated = auth.isAuthenticated()

const isSeller = computed(() => {
  return post.value && userInfo && post.value.userId === userInfo.id
})

const hasPendingRequest = computed(() => {
  if (!userInfo) return false
  return requests.value.some(req => 
    req.buyerId === userInfo.id && req.status === 0
  )
})

const cannotBuy = computed(() => {
  return post.value?.status !== 0 || hasPendingRequest.value || isSeller.value
})

const isCurrentUser = (userId) => {
  return userInfo && userInfo.id === userId
}

const loadPost = async () => {
  const id = route.params.id
  if (id) {
    try {
      const response = await marketApi.getPost(id)
      if (response && response.success && response.data) {
        post.value = response.data
        await loadRequests(id)
        await loadComments(id)
      }
    } catch (error) {
      console.error('获取帖子详情失败:', error)
    }
  }
}

const loadRequests = async (postId) => {
  try {
    const response = await marketApi.getRequests(postId)
    if (response && response.success && response.data) {
      requests.value = response.data
    }
  } catch (error) {
    console.error('获取交易请求失败:', error)
  }
}

const loadComments = async (postId) => {
  try {
    const response = await marketApi.getComments(postId)
    if (response && response.success && response.data) {
      comments.value = response.data
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const handleBuy = async () => {
  if (!isAuthenticated) {
    alert('请先登录')
    router.push('/login')
    return
  }
  
  if (isSeller) {
    alert('您不能购买自己的商品')
    return
  }
  
  try {
    const response = await marketApi.addRequest({
      postId: post.value.id,
      sellerId: post.value.userId,
      status: 0
    })
    
    if (response && response.success) {
      alert('请求已发送')
      await loadRequests(post.value.id)
    } else {
      alert('发送失败，请重试')
    }
  } catch (error) {
    console.error('发送请求失败:', error)
    alert('发送失败，请重试')
  }
}

const handleAgree = async (id) => {
  try {
    const response = await marketApi.updateRequestStatus(id, 1)
    if (response && response.success) {
      alert('已同意')
      await loadRequests(post.value.id)
    }
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败')
  }
}

const handleReject = async (id) => {
  try {
    const response = await marketApi.updateRequestStatus(id, 2)
    if (response && response.success) {
      alert('已拒绝')
      await loadRequests(post.value.id)
    }
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败')
  }
}

const handleCancel = async (id) => {
  try {
    const response = await marketApi.updateRequestStatus(id, 3)
    if (response && response.success) {
      alert('已取消')
      await loadRequests(post.value.id)
    }
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败')
  }
}

const handleSubmitComment = async () => {
  if (!newComment.value.trim()) {
    alert('请输入评论内容')
    return
  }
  
  if (!isAuthenticated) {
    alert('请先登录')
    router.push('/login')
    return
  }
  
  try {
    const response = await marketApi.addComment({
      postId: post.value.id,
      content: newComment.value
    })
    
    if (response && response.success) {
      newComment.value = ''
      alert('评论成功')
      await loadComments(post.value.id)
    } else {
      alert('评论失败，请重试')
    }
  } catch (error) {
    console.error('评论失败:', error)
    alert('评论失败，请重试')
  }
}

const scrollToComments = () => {
  showCommentInput.value = true
  // 滚动到评论区
  setTimeout(() => {
    const commentSection = document.querySelector('.comment-section')
    if (commentSection) {
      commentSection.scrollIntoView({ behavior: 'smooth' })
    }
  }, 100)
}

const getStatusText = (status) => {
  const map = {
    0: '进行中',
    1: '已关闭',
    2: '仅自己可见'
  }
  return map[status] || '未知'
}

const getRequestStatusText = (status) => {
  const map = {
    0: '待处理',
    1: '已同意',
    2: '已拒绝',
    3: '已取消'
  }
  return map[status] || '未知'
}

const showMoreMenu = () => {
  alert('更多操作菜单（待实现）')
}

const goBack = () => {
  router.back()
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadPost()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 60px 0 120px;
  background: #f5f5f5;
  box-sizing: border-box;
  overflow-x: hidden;
}

.header-bar {
  position: fixed;
  top: 0;
  left: 50%;
  right: auto;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  height: 60px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  z-index: 100;
  box-sizing: border-box;
}

.back-btn,
.more-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  padding: 8px;
  color: #333;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.detail-content {
  padding: 0;
  max-width: 100%;
  overflow-x: hidden;
}

.detail-image {
  width: 100%;
  height: 300px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.image-slider {
  width: 100%;
  height: 100%;
}

.image-slider img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.slider-dots {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
}

.dot.active {
  background: white;
}

.placeholder-icon {
  font-size: 80px;
}

.detail-info {
  background: white;
  padding: 20px 16px;
  margin-bottom: 8px;
  box-sizing: border-box;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.price-symbol {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}

.price-value {
  font-size: 32px;
  font-weight: bold;
  color: #e74c3c;
}

.post-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.status-0 {
  background: #e8f5e9;
  color: #43a047;
}

.status-1 {
  background: #ffebee;
  color: #c62828;
}

.status-2 {
  background: #fff3e0;
  color: #fb8c00;
}

.detail-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}

.detail-time {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.detail-desc,
.seller-info {
  background: white;
  padding: 20px 16px;
  margin-bottom: 8px;
  box-sizing: border-box;
}

.section-box {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
  box-sizing: border-box;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

.desc-text {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seller-avatar {
  font-size: 40px;
}

.seller-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.request-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.request-item {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
}

.request-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.request-user {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.request-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 10px;
}

.request-status.status-0 {
  background: #fff3cd;
  color: #856404;
}

.request-status.status-1 {
  background: #d4edda;
  color: #155724;
}

.request-status.status-2 {
  background: #f8d7da;
  color: #721c24;
}

.request-status.status-3 {
  background: #e2e3e5;
  color: #383d41;
}

.request-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.request-actions {
  display: flex;
  gap: 8px;
}

.request-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
}

.agree-btn {
  background: #43a047;
  color: white;
}

.reject-btn {
  background: #e53935;
  color: white;
}

.cancel-btn {
  background: #757575;
  color: white;
}

.comment-input-box {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: flex-end;
}

.comment-input {
  flex: 1;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  resize: none;
  min-height: 80px;
}

.submit-comment-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-user {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.empty-tip {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 20px;
}

.bottom-action {
  position: fixed;
  bottom: 0;
  left: 50%;
  right: auto;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  background: white;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  display: flex;
  gap: 12px;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.06);
  z-index: 100;
  box-sizing: border-box;
}

.action-btn {
  flex: 1;
  min-width: 0;
  padding: 14px 8px;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.comment-btn {
  background: #f5f5f5;
  color: #333;
}

.buy-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.buy-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
