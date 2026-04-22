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
        <div class="tool-card" @click="handleToolClick('memo')">
          <div class="tool-icon">📝</div>
          <div class="tool-name">备忘录</div>
        </div>
      </div>
    </div>

    <!-- 校园新闻部分 -->
    <div class="section">
      <div class="news-block-header">
        <div class="section-header">
          <div class="section-line"></div>
          <h2 class="section-title">校园新闻</h2>
          <div class="section-line"></div>
        </div>
        <div class="news-toolbar">
          <button
            type="button"
            class="news-refresh-btn"
            :disabled="crawlLoading"
            @click="onRefreshNews"
          >
            {{ crawlLoading ? '拉取中…' : '🔄 拉取最新' }}
          </button>
        </div>
      </div>
      <p v-if="crawlHint" class="news-crawl-hint">{{ crawlHint }}</p>
      <p v-if="newsLoading" class="news-loading-hint">加载中…</p>
      <div class="news-list">
        <div class="news-card" v-for="news in newsList" :key="news.id" @click="openNews(news)">
          <div class="news-title">{{ news.title }}</div>
          <div class="news-info">
            <span class="news-time">📅 {{ news.time }}</span>
            <span class="news-read">查看详情 ▶</span>
          </div>
        </div>
        <div v-if="newsList.length === 0 && !newsLoading" class="empty-state">
          <span>📰</span>
          <p>暂无新闻</p>
        </div>
      </div>
      <div v-if="newsList.length > 0" class="news-pagination">
        <button
          type="button"
          class="news-page-btn"
          :disabled="newsPage <= 1 || newsLoading"
          @click="goNewsPage(newsPage - 1)"
        >
          上一页
        </button>
        <span class="news-page-info"
          >第 {{ newsPage }} / {{ newsTotalPagesDisplay }} 页（共 {{ newsTotalDisplay }} 条）</span
        >
        <button
          type="button"
          class="news-page-btn"
          :disabled="!canGoNewsNext"
          @click="goNewsPage(newsPage + 1)"
        >
          下一页
        </button>
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
import { ref, onMounted, computed, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import newsApi from '../api/news'
import auth from '../api/auth'

const router = useRouter()

const newsList = ref([])
const newsPage = ref(1)
const newsPageSize = 8
const newsTotal = ref(0)
const newsTotalPages = ref(1)
const newsModeLegacy = ref(false)
const newsLoading = ref(false)
const showBackTop = ref(false)
const crawlLoading = ref(false)
const crawlHint = ref('')
const isLoggedIn = computed(() => auth.isAuthenticated())

/** 接口未带 total 时的兜底，保证分页区可见且按钮状态正确 */
const newsTotalDisplay = computed(() => {
  const t = newsTotal.value
  if (t > 0) return t
  if (newsList.value.length > 0 && newsModeLegacy.value) {
    return newsList.value.length
  }
  if (newsList.value.length > 0) {
    return (newsPage.value - 1) * newsPageSize + newsList.value.length
  }
  return 0
})

const newsTotalPagesDisplay = computed(() => {
  const p = newsTotalPages.value
  if (p > 0) return p
  const tot = newsTotalDisplay.value
  if (tot <= 0) return 1
  return Math.max(1, Math.ceil(tot / newsPageSize))
})

/** 是否还能点「下一页」：有 total 时按总页数；无 total 时本页条数=每页大小时允许再试（避免误拦截） */
const canGoNewsNext = computed(() => {
  if (newsLoading.value) return false
  if (newsModeLegacy.value) return false
  if (newsList.value.length < newsPageSize) return false
  if (newsTotal.value > 0) {
    return newsPage.value < newsTotalPagesDisplay.value
  }
  return true
})

// 监听滚动显示返回顶部按钮
const handleScroll = () => {
  showBackTop.value = window.scrollY > 300
}

const applyNewsPayload = (d, p) => {
  if (Array.isArray(d)) {
    newsModeLegacy.value = true
    newsList.value = d
    newsTotal.value = d.length
    newsTotalPages.value = 1
    newsPage.value = 1
    return
  }
  if (d && Array.isArray(d.records)) {
    newsModeLegacy.value = false
    newsList.value = d.records
    const current = Number(d.current)
    const rawTotal = Number(d.total)
    newsTotal.value = !Number.isNaN(rawTotal) && rawTotal >= 0 ? rawTotal : 0
    const rawPages = Number(d.pages)
    if (!Number.isNaN(rawPages) && rawPages > 0) {
      newsTotalPages.value = rawPages
    } else if (newsTotal.value > 0) {
      newsTotalPages.value = Math.max(1, Math.ceil(newsTotal.value / newsPageSize))
    } else {
      // total 未返回时由前端保守估计至少一页，不阻塞翻页
      newsTotalPages.value = Math.max(1, Math.ceil((d.records.length || 0) / newsPageSize))
    }
    newsPage.value = !Number.isNaN(current) && current > 0 ? current : p
    return
  }
  newsList.value = []
  newsTotal.value = 0
  newsTotalPages.value = 1
}

const loadNews = async (page = newsPage.value) => {
  newsLoading.value = true
  try {
    const p = page < 1 ? 1 : page
    const response = await newsApi.getNews(p, 0, newsPageSize)
    console.log('新闻API响应:', response)
    if (response && response.success && response.data != null) {
      applyNewsPayload(response.data, p)
    }
  } catch (error) {
    console.error('加载新闻失败:', error)
  } finally {
    newsLoading.value = false
  }
}

const goNewsPage = async (next) => {
  if (next < 1) return
  await loadNews(next)
  nextTick(() => {
    document.querySelector('.news-block-header')?.scrollIntoView({ block: 'start', behavior: 'smooth' })
  })
}

/** 触发自有爬虫拉取最新新闻入库，并延时重新拉列表（爬虫为异步） */
const onRefreshNews = async () => {
  if (crawlLoading.value) return
  crawlLoading.value = true
  crawlHint.value = ''
  try {
    const res = await newsApi.triggerNewsCrawler()
    if (res && res.success) {
      crawlHint.value = res.data || '已开始更新，请稍候…'
      setTimeout(() => loadNews(1), 2500)
      setTimeout(() => loadNews(1), 6000)
      setTimeout(() => {
        crawlHint.value = ''
      }, 8000)
    } else {
      crawlHint.value = res?.message || '更新失败，请稍后再试'
    }
  } catch (error) {
    console.error('触发新闻爬取失败:', error)
    crawlHint.value = '网络异常，请稍后再试'
  } finally {
    crawlLoading.value = false
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
  } else if (tool === 'memo') {
    router.push('/memo')
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

.home-page .news-block-header {
  margin-bottom: 2vw;
}

.home-page .news-block-header .section-header {
  margin-bottom: 3vw;
}

.home-page .news-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 3vw;
}

.home-page .news-refresh-btn {
  padding: 1.6vw 3.6vw;
  font-size: 3.2vw;
  color: #3a7bd5;
  background: #eef6ff;
  border: 1px solid rgba(58, 123, 213, 0.25);
  border-radius: 2vw;
  cursor: pointer;
  font-weight: 600;
}

.home-page .news-refresh-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.home-page .news-refresh-btn:active:not(:disabled) {
  background: #e0eeff;
}

.home-page .news-crawl-hint {
  font-size: 2.8vw;
  color: #5a7a9a;
  margin: 0 0 3vw;
  padding: 0 0.5vw;
}

.home-page .news-loading-hint {
  font-size: 3vw;
  color: #999;
  text-align: center;
  margin-bottom: 2vw;
}

.home-page .news-pagination {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 2vw 3vw;
  margin-top: 4vw;
  padding: 3.5vw 2vw;
  background: #fff;
  border-radius: 2vw;
  border: 1px solid rgba(58, 123, 213, 0.2);
  box-shadow: 0 2px 12px rgba(58, 123, 213, 0.08);
  margin-bottom: 80px;
}

.home-page .news-page-btn {
  padding: 1.8vw 4vw;
  font-size: 3.2vw;
  color: #3a7bd5;
  background: #eef6ff;
  border: 1px solid rgba(58, 123, 213, 0.25);
  border-radius: 2vw;
  cursor: pointer;
  font-weight: 600;
}

.home-page .news-page-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.home-page .news-page-info {
  font-size: 2.8vw;
  color: #666;
  text-align: center;
  flex: 1 1 100%;
}

@media (min-width: 480px) {
  .home-page .news-page-info {
    flex: 0 1 auto;
  }
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