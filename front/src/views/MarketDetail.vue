<template>
  <div class="detail-page">
    <!-- header部分 -->
    <header>
      <div class="header-content">
        <button class="back-btn" @click="goBack">←</button>
        <div class="page-title">
          <span>📦</span>
          <span>商品详情</span>
        </div>
        <button class="more-btn" @click="showMoreMenu">⋯</button>
      </div>
    </header>

    <div v-if="post" class="detail-content">
      <!-- 图片区域 -->
      <div class="image-section">
        <div v-if="imageList.length > 0" class="image-slider">
          <img :src="imageList[currentImageIndex]" alt="商品图片" />
          <div v-if="imageList.length > 1" class="image-dots">
            <span 
              v-for="(url, index) in imageList" 
              :key="index"
              :class="['dot', { active: index === currentImageIndex }]"
              @click="currentImageIndex = index"
            ></span>
          </div>
        </div>
        <div v-else class="image-placeholder">
          <span>📦</span>
        </div>
      </div>

      <!-- 价格和状态 -->
      <div class="price-row">
        <span class="price">¥{{ post.price }}</span>
        <span class="status" :class="'status-' + post.status">
          {{ getStatusText(post.status) }}
        </span>
      </div>

      <!-- 标题 -->
      <div class="title-row">
        <h2>{{ post.title }}</h2>
        <p class="update-time">更新于 {{ formatTime(post.updateTime) }}</p>
      </div>

      <!-- 商品描述 -->
      <div class="desc-section">
        <div class="section-title">
          <span></span>
          <h3>商品描述</h3>
          <span></span>
        </div>
        <p class="desc-text">{{ post.description || '暂无描述' }}</p>
      </div>

      <!-- 卖家信息 -->
      <div class="seller-section">
        <div class="section-title">
          <span></span>
          <h3>卖家信息</h3>
          <span></span>
        </div>
        <div class="seller-card">
          <div class="seller-avatar">👤</div>
          <div class="seller-info">
            <div class="seller-name">天大学生</div>
            <div class="seller-id">ID: {{ post.userId || '未知' }}</div>
          </div>
        </div>
      </div>

      <!-- 交易请求列表 -->
      <div v-if="requests.length > 0" class="request-section">
        <div class="section-title">
          <span></span>
          <h3>交易请求</h3>
          <span></span>
        </div>
        <div class="request-list">
          <div class="request-card" v-for="req in requests" :key="req.id">
            <div class="request-header">
              <span class="buyer-name">买家</span>
              <span class="request-status" :class="'req-status-' + req.status">
                {{ getRequestStatusText(req.status) }}
              </span>
            </div>
            <div class="request-time">{{ formatTime(req.createTime) }}</div>
            <div v-if="req.status === 0" class="request-buttons">
              <button 
                v-if="isSeller" 
                class="btn-agree" 
                @click="handleAgree(req.id)"
              >同意</button>
              <button 
                v-if="isSeller" 
                class="btn-reject" 
                @click="handleReject(req.id)"
              >拒绝</button>
              <button 
                v-if="isCurrentUser(req.buyerId)" 
                class="btn-cancel" 
                @click="handleCancel(req.id)"
              >取消</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 评论区域 -->
      <div class="comment-section">
        <div class="section-title">
          <span></span>
          <h3>评论</h3>
          <span></span>
        </div>

        <!-- 发表评论 -->
        <div class="comment-form">
          <textarea 
            v-model="newComment" 
            placeholder="写下你的评论..."
            rows="3"
          ></textarea>
          <button class="submit-btn" @click="handleSubmitComment">发布评论</button>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-for="cmt in comments" :key="cmt.id" class="comment-card">
            <div class="comment-header">
              <span class="comment-user">👤 {{ cmt.username || '用户' + cmt.userId }}</span>
              <span class="comment-time">{{ formatTime(cmt.createTime) }}</span>
            </div>
            <div class="comment-body">{{ cmt.content }}</div>
          </div>
          <div v-if="comments.length === 0" class="empty-comment">
            <span>💬</span>
            <p>暂无评论，快来抢沙发～</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="!post && !loading" class="empty-page">
      <span>📭</span>
      <p>商品不存在或已下架</p>
    </div>

    <!-- 底部按钮 -->
    <div class="bottom-bar">
      <button class="btn-comment" @click="scrollToComment">
        <span>💬</span>
        <span>评论</span>
      </button>
      <button class="btn-buy" @click="handleBuy" :disabled="cannotBuy">
        <span>❤️</span>
        <span>我想要</span>
      </button>
    </div>

    <!-- 返回顶部 -->
    <div v-show="showBackTop" class="back-top" @click="scrollToTop">
      <span>↑</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import marketApi from '../api/market'
import auth from '../api/auth'

const router = useRouter()
const route = useRoute()

const post = ref(null)
const requests = ref([])
const comments = ref([])
const newComment = ref('')
const loading = ref(false)
const showBackTop = ref(false)
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
  return requests.value.some(req => req.buyerId === userInfo.id && req.status === 0)
})

const cannotBuy = computed(() => {
  return post.value?.status !== 0 || hasPendingRequest.value || isSeller.value
})

const isCurrentUser = (userId) => {
  return userInfo && userInfo.id === userId
}

const handleScroll = () => {
  showBackTop.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const loadPost = async () => {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await marketApi.getPost(id)
    if (res && res.success && res.data) {
      post.value = res.data
      await loadRequests(id)
      await loadComments(id)
    }
  } catch (error) {
    console.error('获取详情失败:', error)
  } finally {
    loading.value = false
  }
}

const loadRequests = async (postId) => {
  try {
    const res = await marketApi.getRequests(postId)
    if (res && res.success && res.data) {
      requests.value = res.data
    }
  } catch (error) {
    console.error('获取请求失败:', error)
  }
}

const loadComments = async (postId) => {
  try {
    const res = await marketApi.getComments(postId)
    if (res && res.success && res.data) {
      comments.value = res.data
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const handleBuy = async () => {
  if (!isAuthenticated) {
    alert('请先登录')
    router.push('/trade/login')
    return
  }
  if (isSeller.value) {
    alert('不能购买自己的商品')
    return
  }
  try {
    const res = await marketApi.addRequest({
      postId: post.value.id,
      sellerId: post.value.userId,
      status: 0
    })
    if (res && res.success) {
      alert('请求已发送')
      await loadRequests(post.value.id)
    } else {
      alert('发送失败')
    }
  } catch (error) {
    console.error('发送失败:', error)
    alert('发送失败')
  }
}

const handleAgree = async (id) => {
  try {
    const res = await marketApi.updateRequestStatus(id, 1)
    if (res && res.success) {
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
    const res = await marketApi.updateRequestStatus(id, 2)
    if (res && res.success) {
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
    const res = await marketApi.updateRequestStatus(id, 3)
    if (res && res.success) {
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
    router.push('/trade/login')
    return
  }
  try {
    const res = await marketApi.addComment({
      postId: post.value.id,
      content: newComment.value
    })
    if (res && res.success) {
      newComment.value = ''
      alert('评论成功')
      await loadComments(post.value.id)
    } else {
      alert('评论失败')
    }
  } catch (error) {
    console.error('评论失败:', error)
    alert('评论失败')
  }
}

const scrollToComment = () => {
  const el = document.querySelector('.comment-section')
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const getStatusText = (status) => {
  const map = { 0: '进行中', 1: '已关闭', 2: '仅自己可见' }
  return map[status] || '未知'
}

const getRequestStatusText = (status) => {
  const map = { 0: '待处理', 1: '已同意', 2: '已拒绝', 3: '已取消' }
  return map[status] || '未知'
}

const showMoreMenu = () => {
  alert('举报')
}

const goBack = () => {
  router.back()
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

onMounted(() => {
  loadPost()
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.detail-page {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

/* ========== Header ========== */
.detail-page header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  padding: 12px 16px;
  z-index: 100;
}

.detail-page .header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-page .back-btn,
.detail-page .more-btn {
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

.detail-page .back-btn:active,
.detail-page .more-btn:active {
  transform: scale(0.95);
}

.detail-page .page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: white;
}

/* ========== 内容区域 ========== */
.detail-content {
  margin-top: 64px;
}

/* 图片区域 */
.image-section {
  width: 100%;
  height: 300px;
  background: #e0e0e0;
  position: relative;
  overflow: hidden;
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

.image-dots {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  background: rgba(0, 0, 0, 0.4);
  padding: 4px 12px;
  border-radius: 20px;
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

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
}

/* 价格行 */
.price-row {
  background: white;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.price-row .price {
  font-size: 28px;
  font-weight: bold;
  color: #ff6b6b;
}

.status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.status-0 { background: #e8f5e9; color: #43a047; }
.status-1 { background: #ffebee; color: #e53935; }
.status-2 { background: #fff3e0; color: #fb8c00; }

/* 标题行 */
.title-row {
  background: white;
  padding: 0 16px 16px;
  margin-bottom: 8px;
}

.title-row h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 8px;
}

.update-time {
  font-size: 12px;
  color: #999;
}

/* 通用 section 样式 */
.section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.section-title span {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #3a7bd5, #00d2ff);
}

.section-title h3 {
  font-size: 16px;
  color: #333;
  margin: 0 16px;
}

/* 商品描述 */
.desc-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.desc-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 卖家信息 */
.seller-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.seller-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seller-avatar {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #e0e0e0, #f0f0f0);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.seller-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.seller-id {
  font-size: 12px;
  color: #999;
}

/* 交易请求 */
.request-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.request-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.request-card {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 12px;
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.buyer-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.request-status {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
}

.req-status-0 { background: #fff3cd; color: #856404; }
.req-status-1 { background: #d4edda; color: #155724; }
.req-status-2 { background: #f8d7da; color: #721c24; }
.req-status-3 { background: #e2e3e5; color: #383d41; }

.request-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.request-buttons {
  display: flex;
  gap: 8px;
}

.request-buttons button {
  padding: 6px 16px;
  border: none;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
}

.btn-agree { background: #43a047; color: white; }
.btn-reject { background: #e53935; color: white; }
.btn-cancel { background: #757575; color: white; }

/* 评论区域 */
.comment-section {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.comment-form {
  margin-bottom: 20px;
}

.comment-form textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
  margin-bottom: 12px;
}

.comment-form textarea:focus {
  outline: none;
  border-color: #3a7bd5;
}

.submit-btn {
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
  border: none;
  border-radius: 24px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-card {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 12px;
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

.comment-body {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.empty-comment {
  text-align: center;
  padding: 30px;
  color: #999;
}

.empty-comment span {
  font-size: 40px;
  display: block;
  margin-bottom: 10px;
}

/* 空页面 */
.empty-page {
  text-align: center;
  padding: 100px 20px;
  color: #999;
}

.empty-page span {
  font-size: 60px;
  display: block;
  margin-bottom: 16px;
}

/* 底部按钮栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  display: flex;
  gap: 12px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.bottom-bar button {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn-comment {
  background: #f0f0f0;
  color: #666;
}

.btn-buy {
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
}

.btn-buy:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 返回顶部 */
.back-top {
  position: fixed;
  right: 16px;
  bottom: 100px;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.back-top span {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

/* 大屏幕适配 */
@media (min-width: 768px) {
  .detail-page {
    max-width: 480px;
    margin: 0 auto;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  }
  
  .detail-page header {
    max-width: 480px;
    left: 50%;
    transform: translateX(-50%);
    right: auto;
  }
  
  .bottom-bar {
    max-width: 480px;
    left: 50%;
    transform: translateX(-50%);
    right: auto;
  }
  
  .back-top {
    right: auto;
    left: 50%;
    transform: translateX(calc(240px - 22px));
  }
}
</style>