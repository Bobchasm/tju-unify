<template>
  <div class="page home-page">
    <div class="header">
      <div class="header-top">
        <div class="header-text">
          <h1>天大校园助手</h1>
          <p>让校园生活更美好</p>
        </div>
        <div class="user-area" @click="handleUserClick">
          <div class="user-avatar" v-if="isLoggedIn">👤</div>
          <div class="login-btn" v-else>登录</div>
        </div>
      </div>
    </div>

    <div class="section">
      <h2 class="section-title">常用工具</h2>
      <div class="tools-grid">
        <!-- <div class="tool-card" @click="goToChat">
          <div class="tool-icon">🤖</div>
          <div class="tool-name">小智助手</div>
        </div> -->
        <div class="tool-card" @click="handleToolClick('market')">
          <div class="tool-icon">🛒</div>
          <div class="tool-name">二手市场</div>
        </div>
        <div class="tool-card" @click="handleToolClick('transaction')">
          <div class="tool-icon">💱</div>
          <div class="tool-name">交易平台</div>
        </div>
        <div class="tool-card" @click="handleToolClick('more')">
          <div class="tool-icon">📋</div>
          <div class="tool-name">更多功能</div>
        </div>
      </div>
    </div>

    <div class="section">
      <h2 class="section-title">校园新闻</h2>
      <div class="news-list">
        <div class="news-card" v-for="news in newsList" :key="news.id" @click="openNews(news)">
          <div class="news-title">{{ news.title }}</div>
          <div class="news-time">{{ news.time }}</div>
        </div>
      </div>
    </div>

    <div class="bottom-nav">
      <div class="nav-item active">
        <div class="nav-icon">🏠</div>
        <div>首页</div>
      </div>
      <div class="nav-item" @click="goToChat">
        <div class="nav-icon">💬</div>
        <div>小智</div>
      </div>
      <div class="nav-item" @click="handleUserClick">
        <div class="nav-icon">👤</div>
        <div>我的</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import newsApi from '../api/news'
import auth from '../api/auth'

const router = useRouter()

const newsList = ref([])
const isLoggedIn = computed(() => auth.isAuthenticated())

const loadNews = async () => {
  try {
    const response = await newsApi.getNews()
    console.log('新闻API响应:', response)
    if (response && response.success && response.data) {
      newsList.value = response.data || []
    }
  } catch (error) {
    console.error('加载新闻失败:', error)
  }
}

const openNews = (news) => {
  if (news && news.url) {
    window.open(news.url, '_blank')
  } else {
    console.warn('新闻链接不存在:', news)
  }
}

const goToChat = () => {
  router.push('/chat')
}

const goToMarket = () => {
  router.push('/market')
}

const handleToolClick = (tool) => {
  if (tool === 'market') {
    goToMarket()
  } else if (tool === 'transaction') {
    router.push('/trade')
  } else {
    alert(`${tool} 功能即将上线，敬请期待！`)
  }
}

const handleUserClick = () => {
  if (isLoggedIn.value) {
    router.push('/profile')
  } else {
    router.push('/login')
  }
}

onMounted(() => {
  loadNews()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding-bottom: 80px;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px 40px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-text h1 {
  color: white;
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 6px;
}

.header-text p {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin: 0;
}

.user-area {
  cursor: pointer;
}

.login-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.section {
  padding: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.tool-card {
  background: white;
  border-radius: 12px;
  padding: 16px 8px;
  text-align: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;
}

.tool-card:active {
  transform: scale(0.95);
}

.tool-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.tool-name {
  font-size: 14px;
  color: #333;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.news-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.2s ease;
}

.news-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.news-card:active {
  transform: translateY(0);
}

.news-title {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
}

.news-time {
  font-size: 12px;
  color: #999;
}


</style>
