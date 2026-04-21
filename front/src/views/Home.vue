<template>
  <!-- 主内容区 -->
  <div class="page home-page">
    <!-- header部分 -->
    <header>
      <div class="header-content">
        <div class="logo-area">
          <span class="logo-icon">🎓</span>
          <h1>天大校园助手</h1>
        </div>
        <div class="user-area" @click="handleUserClick">
          <div class="user-avatar" v-if="isLoggedIn">
            <span>👤</span>
          </div>
          <div class="login-btn" v-else>
            <span>🔑</span>
            <span>登录</span>
          </div>
        </div>
      </div>
      <p class="subtitle">让校园生活更美好</p>
    </header>

    <!-- 工具集部分 -->
    <div class="section">
      <div class="section-header">
        <div class="section-line"></div>
        <h2 class="section-title">常用工具</h2>
        <div class="section-line"></div>
      </div>
      <div class="tools-grid">
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

    <!-- 校园新闻部分 -->
    <div class="section">
      <div class="section-header">
        <div class="section-line"></div>
        <h2 class="section-title">校园新闻</h2>
        <div class="section-line"></div>
      </div>
      <div class="news-list">
        <div class="news-card" v-for="news in newsList" :key="news.id" @click="openNews(news)">
          <div class="news-title">{{ news.title }}</div>
          <div class="news-info">
            <span class="news-time">📅 {{ news.time }}</span>
            <span class="news-read">查看详情 ▶</span>
          </div>
        </div>
        <div v-if="newsList.length === 0" class="empty-state">
          <span>📰</span>
          <p>暂无新闻</p>
        </div>
      </div>
    </div>

    <!-- 返回顶部悬浮按钮 -->
    <div class="back-top-fab" @click="scrollToTop" v-show="showBackTop">
      <span>↑</span>
    </div>
  </div>

  <!-- 底部导航栏 - 移到外面，不受父容器 transform 影响 -->
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
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import newsApi from '../api/news'
import auth from '../api/auth'

const router = useRouter()

const newsList = ref([])
const showBackTop = ref(false)
const isLoggedIn = computed(() => auth.isAuthenticated())

// 监听滚动显示返回顶部按钮
const handleScroll = () => {
  showBackTop.value = window.scrollY > 300
}

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
    router.push('/trade/login')
  }
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  loadNews()
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/****************** 全局样式 ******************/
.home-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 0;
}

/****************** header部分 ******************/
.home-page header {
  width: 100%;
  margin: 0;
  border-radius: 0;
  margin-top: 0;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  padding: 5vw 4vw 4vw;
  box-sizing: border-box;
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.3);
}

.home-page header .header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.home-page header .logo-area {
  display: flex;
  align-items: center;
  gap: 2vw;
}

.home-page header .logo-area .logo-icon {
  font-size: 7vw;
}

.home-page header .logo-area h1 {
  font-size: 5vw;
  font-weight: bold;
  color: #fff;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.home-page header .subtitle {
  font-size: 3.2vw;
  color: rgba(255, 255, 255, 0.85);
  margin: 3vw 0 0 0;
  text-align: center;
}

.home-page header .user-area {
  cursor: pointer;
}

.home-page header .login-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 2vw 3.5vw;
  border-radius: 5vw;
  font-size: 3.2vw;
  display: flex;
  align-items: center;
  gap: 1.5vw;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.home-page header .login-btn:active {
  transform: scale(0.95);
  background: rgba(255, 255, 255, 0.3);
}

.home-page header .user-avatar {
  width: 10vw;
  height: 10vw;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 5vw;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.home-page header .user-avatar:active {
  transform: scale(0.95);
}

/****************** 通用section样式 ******************/
.home-page .section {
  padding: 4vw;
}

.home-page .section-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 5vw;
}

.home-page .section-line {
  width: 8vw;
  height: 0.3vw;
  background: linear-gradient(90deg, #3a7bd5, #00d2ff);
  border-radius: 0.3vw;
}

.home-page .section-title {
  font-size: 4.5vw;
  font-weight: bold;
  color: #333;
  margin: 0 4vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

/****************** 工具网格 ******************/
.home-page .tools-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4vw;
  background: white;
  border-radius: 3vw;
  padding: 5vw 3vw;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.home-page .tool-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 2vw;
  border-radius: 2vw;
}

.home-page .tool-card:active {
  transform: scale(0.95);
  background-color: #f0f7ff;
}

.home-page .tool-icon {
  font-size: 12vw;
  margin-bottom: 2vw;
}

.home-page .tool-name {
  font-size: 3.2vw;
  color: #555;
  font-weight: 500;
}

/****************** 新闻列表 ******************/
.home-page .news-list {
  display: flex;
  flex-direction: column;
  gap: 3vw;
}

.home-page .news-card {
  background: white;
  border-radius: 2vw;
  padding: 4vw;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.home-page .news-card:active {
  transform: scale(0.98);
  background-color: #fafafa;
}

.home-page .news-title {
  font-size: 3.8vw;
  color: #333;
  font-weight: 600;
  margin-bottom: 2.5vw;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.home-page .news-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.home-page .news-time {
  font-size: 2.8vw;
  color: #999;
  display: flex;
  align-items: center;
  gap: 1vw;
}

.home-page .news-read {
  font-size: 2.8vw;
  color: #3a7bd5;
  display: flex;
  align-items: center;
  gap: 1vw;
}

/****************** 空状态样式 ******************/
.home-page .empty-state {
  text-align: center;
  padding: 12vw 0;
  color: #999;
}

.home-page .empty-state span {
  font-size: 12vw;
  margin-bottom: 3vw;
  opacity: 0.5;
  display: inline-block;
}

.home-page .empty-state p {
  font-size: 3.5vw;
  margin: 0;
}

/****************** 返回顶部悬浮按钮 ******************/
.home-page .back-top-fab {
  position: fixed;
  right: 5vw;
  bottom: 18vw;
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

.home-page .back-top-fab:active {
  transform: scale(0.92);
}

.home-page .back-top-fab span {
  color: white;
  font-size: 6vw;
  font-weight: bold;
}

/****************** 响应式适配 ******************/
@media (min-width: 768px) {
  .home-page header .logo-area .logo-icon {
    font-size: 3rem;
  }
  
  .home-page header .logo-area h1 {
    font-size: 2rem;
  }
  
  .home-page .tool-icon {
    font-size: 4rem;
  }
  
  .home-page .back-top-fab {
    width: 50px;
    height: 50px;
  }
  
  .home-page .back-top-fab span {
    font-size: 24px;
  }
}
</style>

<style>
/****************** 底部导航栏 - 全局样式（不加 scoped） ******************/
.home-page .bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 14vw;
  background: white;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.home-page .nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1vw;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 2vw 4vw;
  border-radius: 2vw;
  color: #999;
}

.home-page .nav-item.active {
  color: #3a7bd5;
}

.home-page .nav-item:active {
  transform: scale(0.95);
  background-color: #f0f7ff;
}

.home-page .nav-icon {
  font-size: 5vw;
}

.home-page .nav-item div:last-child {
  font-size: 2.5vw;
  font-weight: 500;
}

@media (min-width: 768px) {
  .bottom-nav {
    height: 70px;
  }
  
  .nav-icon {
    font-size: 24px;
  }
  
  .nav-item div:last-child {
    font-size: 14px;
  }
  
  .nav-item {
    padding: 10px 20px;
  }
}
</style>