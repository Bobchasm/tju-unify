<template>
  <div class="page market-page">
    <!-- header部分 - 统一蓝色渐变风格 -->
    <header>
      <div class="header-content">
        <div class="back-btn" @click="goBack">
          <span>←</span>
        </div>
        <div class="page-title">
          <span class="title-icon">🛒</span>
          <span>二手市场</span>
        </div>
        <div class="placeholder"></div>
      </div>
      <!-- 搜索栏放在 header 内部 -->
      <div class="search-section">
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input
            type="text"
            class="search-input"
            v-model="searchKeyword"
            placeholder="搜索闲置物品..."
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
      </div>
    </header>

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
            <span class="post-time">📅 {{ formatTime(post.updateTime) }}</span>
          </div>
        </div>
        <div class="post-status" :class="'status-' + post.status">
          {{ getStatusText(post.status) }}
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="posts.length === 0 && !loading" class="empty-state">
      <span>📭</span>
      <p>{{ searchKeyword.trim() ? '未找到相关物品' : '暂无闲置物品' }}</p>
      <p class="empty-hint">
        {{ searchKeyword.trim() ? '换个关键词试试，或清空搜索框浏览全部' : '点击右下角按钮发布第一个商品吧' }}
      </p>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && posts.length > 0" class="load-more" @click="loadMore">
      <span>加载更多</span>
      <span>↓</span>
    </div>

    <!-- 发布按钮 -->
    <div class="fab-btn" @click="goToPublish">
      <span class="fab-icon">+</span>
    </div>

    <!-- 返回顶部悬浮按钮 -->
    <!-- <div class="back-top-fab" @click="scrollToTop" v-show="showBackTop">
      <span>↑</span>
    </div> -->
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import marketApi from '../api/market'
import auth from '../api/auth'

const router = useRouter()

const searchKeyword = ref('')
const posts = ref([])
const pageNo = ref(1)
const hasMore = ref(true)
const loading = ref(false)
const showBackTop = ref(false)
const isLoggedIn = computed(() => auth.isAuthenticated())

// 监听滚动显示返回顶部按钮
const handleScroll = () => {
  showBackTop.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const loadPosts = async () => {
  if (loading.value) return
  loading.value = true
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
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    pageNo.value = 1
    loadPosts()
    return
  }

  loading.value = true
  try {
    const response = await marketApi.searchPosts(searchKeyword.value)
    if (response && response.success) {
      posts.value = Array.isArray(response.data) ? response.data : []
      hasMore.value = false
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
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

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`
  
  return `${date.getMonth() + 1}/${date.getDate()}`
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
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/****************** 全局样式 ******************/
.market-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 0;
  padding-bottom: 14vw;
}

/****************** header部分 - 统一蓝色渐变 ******************/
.market-page header {
  width: 100%;
  margin: 0;
  border-radius: 0;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  padding: 5vw 4vw 4vw;
  box-sizing: border-box;
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.3);
}

.market-page header .header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.market-page header .back-btn {
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

.market-page header .back-btn span {
  font-size: 6vw;
  color: white;
  font-weight: bold;
}

.market-page header .back-btn:active {
  transform: scale(0.95);
  background: rgba(255, 255, 255, 0.3);
}

.market-page header .page-title {
  display: flex;
  align-items: center;
  gap: 2vw;
  flex: 1;
  justify-content: center;
}

.market-page header .page-title .title-icon {
  font-size: 6vw;
}

.market-page header .page-title span:last-child {
  font-size: 5vw;
  font-weight: bold;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.market-page header .placeholder {
  width: 10vw;
  flex-shrink: 0;
  visibility: hidden;
}

/****************** 搜索区域 ******************/
.market-page .search-section {
  margin-top: 4vw;
}

.market-page .search-box {
  display: flex;
  align-items: center;
  gap: 2vw;
  background: white;
  border-radius: 8vw;
  padding: 1.5vw 3vw;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.market-page .search-icon {
  font-size: 4.5vw;
  color: #999;
}

.market-page .search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 3.8vw;
  padding: 2vw 0;
  background: transparent;
}

.market-page .search-input::placeholder {
  color: #bbb;
}

.market-page .search-btn {
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
  border: none;
  border-radius: 6vw;
  padding: 2vw 5vw;
  font-size: 3.5vw;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.market-page .search-btn:active {
  transform: scale(0.95);
}

/****************** 帖子列表 ******************/
.market-page .post-list {
  padding: 4vw;
  display: flex;
  flex-direction: column;
  gap: 3vw;
}

.post-card {
  background: white;
  border-radius: 3vw;
  padding: 4vw;
  display: flex;
  gap: 3vw;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
}

.post-card:active {
  transform: scale(0.98);
}

.post-image {
  width: 25vw;
  height: 25vw;
  background: #f5f5f5;
  border-radius: 2vw;
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
  font-size: 12vw;
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2vw;
}

.post-title {
  font-size: 4vw;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.post-desc {
  font-size: 3.2vw;
  color: #999;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-price {
  font-size: 5vw;
  font-weight: bold;
  color: #ff6b6b;
}

.post-time {
  font-size: 2.8vw;
  color: #bbb;
  display: flex;
  align-items: center;
  gap: 1vw;
}

.post-status {
  position: absolute;
  top: 4vw;
  right: 4vw;
  padding: 1vw 2.5vw;
  border-radius: 3vw;
  font-size: 2.5vw;
  font-weight: 500;
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

/****************** 空状态样式 ******************/
.empty-state {
  text-align: center;
  padding: 20vw 4vw;
  color: #999;
}

.empty-state span {
  font-size: 20vw;
  opacity: 0.5;
}

.empty-state p {
  font-size: 4vw;
  margin: 3vw 0 0;
}

.empty-state .empty-hint {
  font-size: 3.2vw;
  color: #bbb;
  margin-top: 2vw;
}

/****************** 加载更多 ******************/
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2vw;
  padding: 4vw;
  color: #3a7bd5;
  font-size: 3.5vw;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.load-more:active {
  transform: scale(0.98);
  opacity: 0.7;
}

/****************** 发布按钮 ******************/
.fab-btn {
  position: fixed;
  right: 5vw;
  bottom: 20vw;
  width: 14vw;
  height: 14vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8vw;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(58, 123, 213, 0.5);
  z-index: 99;
  transition: all 0.2s ease;
}

.fab-btn:active {
  transform: scale(0.92);
}

.fab-icon {
  line-height: 1;
}

/****************** 返回顶部悬浮按钮 ******************/
.market-page .back-top-fab {
  position: fixed;
  right: 5vw;
  bottom: 38vw;
  width: 12vw;
  height: 12vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.4);
  cursor: pointer;
  z-index: 99;
  transition: all 0.2s ease;
}

.market-page .back-top-fab:active {
  transform: scale(0.92);
}

.market-page .back-top-fab span {
  color: white;
  font-size: 6vw;
  font-weight: bold;
}

/****************** 响应式适配 - 大屏幕 ******************/
@media (min-width: 768px) {
  .market-page {
    padding-bottom: 70px;
  }
  
  .market-page header .back-btn {
    width: 44px;
    height: 44px;
  }
  
  .market-page header .back-btn span {
    font-size: 24px;
  }
  
  .market-page header .page-title .title-icon {
    font-size: 28px;
  }
  
  .market-page header .page-title span:last-child {
    font-size: 22px;
  }
  
  .market-page header .placeholder {
    width: 44px;
  }
  
  .market-page .search-icon {
    font-size: 20px;
  }
  
  .market-page .search-input {
    font-size: 16px;
  }
  
  .market-page .search-btn {
    padding: 8px 20px;
    font-size: 14px;
  }
  
  .post-image {
    width: 100px;
    height: 100px;
  }
  
  .placeholder-icon {
    font-size: 48px;
  }
  
  .post-title {
    font-size: 16px;
  }
  
  .post-desc {
    font-size: 13px;
  }
  
  .post-price {
    font-size: 20px;
  }
  
  .post-time {
    font-size: 12px;
  }
  
  .post-status {
    font-size: 11px;
    padding: 4px 12px;
  }
  
  .fab-btn {
    width: 56px;
    height: 56px;
    font-size: 32px;
    bottom: 100px;
  }
  
  .market-page .back-top-fab {
    width: 50px;
    height: 50px;
    bottom: 170px;
  }
  
  .market-page .back-top-fab span {
    font-size: 24px;
  }
}
</style>