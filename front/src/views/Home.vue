<template>
  <!-- 主内容区 -->
  <div class="page home-page">
    <!-- header部分 -->
    <header>
      <div class="header-bg-decoration"></div>
      <div class="header-content">
        <div class="logo-icon">
          <img src="/tju.png" alt="校徽" class="logo-img" />
          </div>
        <div class="logo-area">
          
          <div class="logo-text">
            <h1>天大校园助手</h1>
          </div>
        </div>
        <div class="user-area" @click="handleUserClick">
          <div class="user-avatar" v-if="isLoggedIn && userInfo?.photo && !avatarLoadFailed">
            <img 
              :src="userInfo.photo" 
              alt="头像"
              class="avatar-img"
              @error="avatarLoadFailed = true"
            />
          </div>
          <div class="user-avatar user-icon" v-else-if="isLoggedIn">
            <i class="fa-solid fa-circle-user"></i>
          </div>
          <div class="login-btn" v-else>
            <i class="fa-solid fa-right-to-bracket"></i>
            <span>登录</span>
          </div>
        </div>
      </div>
      <div class="header-wave">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 120">
          <path fill="#f8f9fa" fill-opacity="1" d="M0,64L48,69.3C96,75,192,85,288,80C384,75,480,53,576,48C672,43,768,53,864,64C960,75,1056,85,1152,80C1248,75,1344,53,1392,42.7L1440,32L1440,120L1392,120C1344,120,1248,120,1152,120C1056,120,960,120,864,120C768,120,672,120,576,120C480,120,384,120,288,120C192,120,96,120,48,120L0,120Z"></path>
        </svg>
      </div>
    </header>

    <!-- 工具集部分 -->
    <div class="section">
      <div class="section-header">
        <div class="section-line"></div>
        <h2 class="section-title">校园工具</h2>
        <div class="section-line"></div>
      </div>
      
      <!-- 工具网格 - 横排布局 -->
      <div class="tools-grid">
        <!-- 二手市场 -->
        <div class="tool-card" @click="handleToolClick('market')">
          <i class="fa-solid fa-cart-shopping" style="color: rgb(46, 116, 172);"></i>
          <div class="tool-name">二手市场</div>
        </div>
        <!-- 交易平台 -->
        <div class="tool-card" @click="handleToolClick('transaction')">
          <i class="fa-solid fa-bag-shopping" style="color: rgb(46, 116, 172);"></i>
          <div class="tool-name">交易平台</div>
        </div>
        <!-- 备忘录 -->
        <div class="tool-card" @click="handleToolClick('memo')">
          <i class="fa-solid fa-table" style="color: rgb(46, 116, 172);"></i>
          <div class="tool-name">备忘录</div>
        </div>
        <!-- 跑腿 -->
        <div class="tool-card" @click="handleToolClick('errand')">
          <i class="fa-solid fa-person-running" style="color: rgb(46, 116, 172);"></i>
          <div class="tool-name">跑腿</div>
        </div>
        <!-- 一卡通 -->
        <div class="tool-card tool-placeholder" @click="handleToolClick('comingSoon')">
          <i class="fa-regular fa-id-card" style="color: rgb(46, 116, 172);"></i>
          <div class="tool-name">一卡通*</div>
        </div>
        <!-- 更多按钮 -->
        <div class="tool-card tool-more" @click="openMoreModal">
          <i class="fa-solid fa-ellipsis" style="color: rgb(46, 116, 172);"></i>
          <div class="tool-name">更多</div>
        </div>
      </div>

      <!-- 更多应用弹窗 - 居中显示 -->
      <transition name="modal-fade">
        <div v-if="showMoreModal" class="more-modal" @click.self="closeMoreModal">
          <div class="more-modal-container">
            <div class="more-modal-header">
              <h3>
                <i class="fa-solid fa-grid-2"></i>
                全部应用
              </h3>
              <button class="more-modal-close" @click="closeMoreModal">
                <i class="fa-solid fa-xmark"></i>
              </button>
            </div>
            <div class="more-modal-body">
              <div class="more-apps-grid">
                <div class="more-app-item" @click="handleToolClick('lost')">
                  <div class="more-app-icon">🔍</div>
                  <div class="more-app-name">失物招领*</div>
                </div>
                <div class="more-app-item" @click="handleToolClick('activity')">
                  <div class="more-app-icon">🎉</div>
                  <div class="more-app-name">校园活动*</div>
                </div>
                <div class="more-app-item" @click="handleToolClick('dorm')">
                  <div class="more-app-icon">🏠</div>
                  <div class="more-app-name">宿舍报修*</div>
                </div>
                <div class="more-app-item" @click="handleToolClick('exam')">
                  <div class="more-app-icon">📊</div>
                  <div class="more-app-name">成绩查询*</div>
                </div>
                <div class="more-app-item" @click="handleToolClick('map')">
                  <div class="more-app-icon">🗺️</div>
                  <div class="more-app-name">校园地图*</div>
                </div>
                <div class="more-app-item" @click="handleToolClick('calendar')">
                  <div class="more-app-icon">📆</div>
                  <div class="more-app-name">校历*</div>
                </div>
                <div class="more-app-item" @click="handleToolClick('library')">
                  <div class="more-app-icon">📖</div>
                  <div class="more-app-name">图书馆*</div>
                </div>
                <div class="more-app-item" @click="handleToolClick('canteen')">
                  <div class="more-app-icon">🍜</div>
                  <div class="more-app-name">食堂信息*</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>
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
      <i class="fa-solid fa-arrow-up-from-bracket" style="color: rgb(251, 251, 252);"></i>
    </div>
  </div>

  <!-- 底部导航栏 -->
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
import request from '../api/request'
import { toast } from '../utils/toast';

const router = useRouter()

// 头像占位URL - 请替换为实际图片地址
const logoIconUrl = ref('/trade-assets/tju-logo.png')

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
const userInfo = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const showMoreModal = ref(false)
const avatarLoadFailed = ref(false)

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
      newsTotalPages.value = Math.max(1, Math.ceil((d.records.length || 0) / newsPageSize))
    }
    newsPage.value = !Number.isNaN(current) && current > 0 ? current : p
    return
  }
  newsList.value = []
  newsTotal.value = 0
  newsTotalPages.value = 1
}

// 打开更多弹窗
const openMoreModal = () => {
  showMoreModal.value = true
}

// 关闭更多弹窗
const closeMoreModal = () => {
  showMoreModal.value = false
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
  closeMoreModal()  // 点击后关闭弹窗
  if (tool === 'market') {
    goToMarket()
  } else if (tool === 'transaction') {
    router.push('/trade')
  } else if (tool === 'memo') {
    router.push('/memo')
  } else if (tool === 'errand') {
    router.push('/errand')
  } else if (tool === 'comingSoon') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'lost') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'activity') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'dorm') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'exam') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'elective') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'map') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'weather') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'calendar') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'library') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'bus') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'canteen') {
    toast.warning('功能开发中,敬请期待')
  } else if (tool === 'repair') {
    toast.warning('功能开发中,敬请期待')
  } else {
    toast.warning('功能开发中,敬请期待')
  }
}

const handleUserClick = () => {
  if (isLoggedIn.value) {
    router.push('/profile')
  } else {
    router.push('/login')
  }
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const loadUserData = async () => {
  // 未登录用户不请求个人信息
  if (!auth.isAuthenticated()) {
    console.log('用户未登录，跳过获取个人信息')
    return
  }
  
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await request.get('/api/person')
    if (response.success) {
      userInfo.value = response.data
      auth.setUserInfo(response.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 401 表示 token 无效，清除登录状态
    if (error.response?.status === 401) {
      auth.logout()
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadNews()
  loadUserData() 
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

/****************** header部分 - 美化版 ******************/
.home-page header {
  position: relative;
  width: 100%;
  margin: 0;
  border-radius: 0;
  background: linear-gradient(135deg, #1a5bbf, #00a8e8, #1a5bbf);
  background-size: 200% 200%;
  animation: gradientShift 8s ease infinite;
  padding: 5vw 4vw 8vw;
  box-sizing: border-box;
  overflow: hidden;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 背景装饰圆 */
.home-page header .header-bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.home-page header .header-bg-decoration::before {
  content: '';
  position: absolute;
  top: -30%;
  right: -20%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15), transparent);
  border-radius: 50%;
}

.home-page header .header-bg-decoration::after {
  content: '';
  position: absolute;
  bottom: -40%;
  left: -15%;
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1), transparent);
  border-radius: 50%;
}

/* 波浪装饰 */
.home-page header .header-wave {
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  line-height: 0;
}

.home-page header .header-wave svg {
  width: 100%;
  height: auto;
}

.home-page header .header-content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 2;
}

/* Logo区域 - 标题居中 */
.home-page header .logo-area {
  display: flex;
  align-items: center;
  gap: 3vw;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
}

.home-page header .logo-icon {
  width: 12vw;
  height: 12vw;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.home-page header .logo-icon .logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.home-page header .logo-text {
  text-align: left;
}

.home-page header .logo-text h1 {
  font-family: "PingFang SC", "Microsoft YaHei", "宋体", "KaiTi", serif;
  font-size: 5.5vw;
  font-weight: 700;
  color: white;
  margin: 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  letter-spacing: 2px;
}

.home-page header .logo-text p {
  font-size: 2.5vw;
  color: rgba(255, 255, 255, 0.7);
  margin: 0.5vw 0 0;
  letter-spacing: 1px;
}

/* 用户区域保持右侧 */
.home-page header .user-area {
  cursor: pointer;
  position: relative;
  z-index: 2;
  margin-left: auto;
}

.home-page header .login-btn {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  color: white;
  padding: 2vw 3.5vw;
  border-radius: 8vw;
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
  width: 12vw;
  height: 12vw;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.home-page header .user-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.home-page header .user-icon i {
  font-size: 6vw;
  color: white;
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
  gap: 3vw;
  background: #f8f9fa;
  border-radius: 3vw;
  padding: 4vw 3vw;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.home-page .tool-card {
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2vw;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 2.5vw 2vw;
  border-radius: 2vw;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.home-page .tool-card:active {
  transform: scale(0.96);
  background-color: #e8f0fe;
}

.home-page .tool-icon {
  font-size: 5vw;
  flex-shrink: 0;
}

.home-page .tool-name {
  font-size: 3.2vw;
  color: #555;
  font-weight: 500;
  white-space: nowrap;
}

.home-page .tool-placeholder .tool-icon,
.home-page .tool-more .tool-icon {
  opacity: 0.7;
}

/****************** 更多应用弹窗 - 居中 ******************/
.more-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.more-modal-container {
  width: 85%;
  max-width: 400px;
  max-height: 80vh;
  background: white;
  border-radius: 5vw;
  overflow: hidden;
  animation: modalSlideUp 0.3s ease;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.more-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4vw 5vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
}

.more-modal-header h3 {
  color: white;
  font-size: 4.5vw;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 2vw;
}

.more-modal-header h3 i {
  font-size: 5vw;
}

.more-modal-close {
  width: 8vw;
  height: 8vw;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  font-size: 5vw;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.more-modal-close:active {
  transform: scale(0.92);
  background: rgba(255, 255, 255, 0.3);
}

.more-modal-body {
  padding: 5vw;
  max-height: 60vh;
  overflow-y: auto;
}

/* 弹窗内的网格布局 - 与工具网格风格一致 */
.more-apps-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 3vw;
}

.more-app-item {
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.5vw;
  cursor: pointer;
  padding: 3vw 1vw;
  border-radius: 3vw;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.more-app-item:active {
  transform: scale(0.95);
  background-color: #f0f7ff;
}

.more-app-icon {
  font-size: 8vw;
}

.more-app-name {
  font-size: 3vw;
  color: #555;
  text-align: center;
  font-weight: 500;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .more-modal-container,
.modal-fade-leave-to .more-modal-container {
  transform: translateY(30px);
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

/****************** 新闻分页 ******************/
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
  flex-wrap: nowrap;
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
  white-space: nowrap;
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