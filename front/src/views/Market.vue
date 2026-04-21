<template>
  <div class="page market-page">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <button class="back-btn" @click="goBack">←</button>
      <input
        type="text"
        class="search-input"
        v-model="searchKeyword"
        placeholder="搜索闲置物品..."
        @keyup.enter="handleSearch"
      />
      <button class="search-btn" @click="handleSearch">搜索</button>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list">
      <div
        v-for="post in posts"
        :key="post.id"
        class="post-card"
        @click="goToDetail(post)"
      >
        <div class="post-image">
          <img v-if="getFirstImage(post)" :src="getFirstImage(post)" alt="商品图片" />
          <span v-else class="placeholder-icon">📦</span>
        </div>
        <div class="post-info">
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-desc">{{ post.description }}</p>
          <div class="post-footer">
            <span class="post-price">¥{{ post.price }}</span>
            <span class="post-time">{{ formatTime(post.updateTime) }}</span>
          </div>
        </div>
        <div class="post-status" :class="'status-' + post.status">
          {{ getStatusText(post.status) }}
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more" @click="loadMore">
      加载更多
    </div>

    <!-- 发布按钮 -->
    <div class="fab-btn" @click="goToPublish">
      <span class="fab-icon">+</span>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goHome">
        <div class="nav-icon">🏠</div>
        <div>首页</div>
      </div>
      <div class="nav-item" @click="goToChat">
        <div class="nav-icon">💬</div>
        <div>小智</div>
      </div>
      <div class="nav-item" @click="goToUser">
        <div class="nav-icon">👤</div>
        <div>我的</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import marketApi from '../api/market'
import auth from '../api/auth'

const router = useRouter()

const searchKeyword = ref('')
const posts = ref([])
const pageNo = ref(1)
const hasMore = ref(true)
const isLoggedIn = computed(() => auth.isAuthenticated())

const loadPosts = async () => {
  try {
    const response = await marketApi.getPosts(pageNo.value)
    console.log('加载帖子响应:', response)
    if (response && response.success && response.data) {
      if (pageNo.value === 1) {
        posts.value = response.data
      } else {
        posts.value = [...posts.value, ...response.data]
      }
      hasMore.value = response.data.length >= 10
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    pageNo.value = 1
    loadPosts()
    return
  }
  
  try {
    const response = await marketApi.searchPosts(searchKeyword.value)
    console.log('搜索结果响应:', response)
    if (response && response.success && response.data) {
      posts.value = response.data
    }
  } catch (error) {
    console.error('搜索失败:', error)
  }
}

const loadMore = () => {
  pageNo.value++
  loadPosts()
}

const goToDetail = (post) => {
  router.push({ name: 'MarketDetail', params: { id: post.id } })
}

const goToPublish = () => {
  router.push({ name: 'MarketPublish' })
}

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push({ name: 'Home' })
}

const goToChat = () => {
  router.push({ name: 'Chat' })
}

const goToUser = () => {
  if (isLoggedIn.value) {
    router.push('/profile')
  } else {
    router.push('/login')
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString()
}

const getStatusText = (status) => {
  const map = {
    0: '进行中',
    1: '已关闭',
    2: '仅自己可见'
  }
  return map[status] || '未知'
}

const getFirstImage = (post) => {
  if (!post || !post.images) return null
  const images = post.images.split(',').filter(url => url.trim())
  return images[0] || null
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 80px 16px 100px;
  background: #f5f5f5;
}

.search-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 12px;
  padding: 16px;
  background: white;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  align-items: center;
}

.back-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 4px;
  color: #333;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  font-size: 14px;
}

.search-btn {
  padding: 0 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.post-image {
  width: 100px;
  height: 100px;
  background: #f0f0f0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.post-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-icon {
  font-size: 40px;
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.post-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.post-desc {
  font-size: 13px;
  color: #666;
  margin: 0;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-price {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-status {
  align-self: flex-start;
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
  color: #e53935;
}

.status-2 {
  background: #fff3e0;
  color: #fb8c00;
}

.load-more {
  text-align: center;
  padding: 16px;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
}

.fab-btn {
  position: fixed;
  right: 20px;
  bottom: 100px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  z-index: 99;
}

.fab-icon {
  line-height: 1;
}


</style>
