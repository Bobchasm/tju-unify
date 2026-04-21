<template>
  <div class="back-btn-container">
        <BackButton style="margin-top: 2vw;"/>
    </div>
  <div class="notifications-container">

    <div class="fixed-top">
          <div class="top-background">
              <h1>消息与通知</h1>
              <div v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</div>
          </div>
        </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载消息中...</p>
    </div>

    <div v-if="error" class="error-state">
      <i class="fas fa-exclamation-circle"></i>
      <p>{{ error }}</p>
      <button 
        class="retry-btn" 
        @click="error.includes('连接') ? initWebSocket() : fetchHistoryMessages()"
      >
        {{ error.includes('连接') ? '重新连接' : '重新加载' }}
      </button>
    </div>

    <div v-else class="notification-list">
      <div 
        v-for="message in messages" 
        :key="message.id" 
        class="notification-item" 
        @click="markAsRead(message)"
      >
        <div 
          class="icon-wrapper" 
          :class="{ 
            'unread': message.unread,
            'expiry-warning': message.notificationType === 2 
          }"
        >
          <i v-if="message.notificationType === 2" class="icon fas fa-hourglass-half"></i> 
          <svg v-else class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
            <path d="M22 6.5C22 7.32843 21.3284 8 20.5 8H3.5C2.67157 8 2 7.32843 2 6.5V3.5C2 2.67157 2.67157 2 3.5 2H20.5C21.3284 2 22 2.67157 22 3.5V6.5ZM22 17.5C22 18.3284 21.3284 19 20.5 19H3.5C2.67157 19 2 18.3284 2 17.5V14.5C2 13.6716 2.67157 13 3.5 13H20.5C21.3284 13 22 13.6716 22 14.5V17.5ZM2 10.5C2 9.67157 2.67157 9 3.5 9H20.5C21.3284 9 22 9.67157 22 10.5V11.5C22 12.3284 21.3284 13 20.5 13H3.5C2.67157 13 2 12.3284 2 11.5V10.5Z"></path>
          </svg>
        </div>
        <div class="content">
          <div 
            class="message-text" 
            :class="{ 
              'bold': message.unread,
              'warning-text': message.notificationType === 2 
            }"
          >
            <span v-if="message.notificationType === 2" style="color: #faad14;">【积分预警】</span>
            {{ message.notificationContent }}
          </div>
          <div class="timestamp">{{ formatTime(message.createTime) }}</div>
        </div>
        <div v-if="message.unread" class="dot"></div>
      </div>

      <div v-if="messages.length === 0 && !loading && !error" class="empty-state">
        <i class="fas fa-envelope-open"></i>
        <p>暂无消息通知</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import request from '@/trade/utils/tradeRequest';
import { toast } from '@/trade/utils/toast';
//import BackButton from "@/trade/components/BackButton.vue";

// 状态管理
const messages = ref([]);
const loading = ref(true);
const error = ref('');
const webSocket = ref(null);
const isConnected = ref(false);

// 消息类型常量 (根据 API 文档)
const MESSAGE_TYPE = {
  MERCHANT_AUDIT: 0, // 商家申请审核
  STORE_AUDIT: 1,    // 开店申请审核
  SCORE_EXPIRY: 2    // 积分过期预警
};

// 获取当前用户信息
const userFromLocal = localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')) : null;
const userFromSession = sessionStorage.getItem('userInfo') ? JSON.parse(sessionStorage.getItem('userInfo')) : null;
const user = userFromLocal || userFromSession;
// 确保 currentUserId 在用户未登录时为 null
const currentUserId = user ? user.id : null; 

// 计算未读消息数量
const unreadCount = computed(() => {
  return messages.value.filter(msg => msg.unread).length;
});

// 初始化：加载历史消息并连接WebSocket
onMounted(() => {
  if (currentUserId) { // 只有在获取到用户ID时才执行初始化操作
    fetchHistoryMessages();
    initWebSocket();
  } else {
    loading.value = false;
    error.value = '请先登录以接收消息通知';
  }
});

// 组件卸载时关闭WebSocket连接
onUnmounted(() => {
  if (webSocket.value) {
    webSocket.value.close();
  }
});

/**
 * 加载历史消息
 */
const fetchHistoryMessages = async () => {
  loading.value = true;
  error.value = ''; // 清除之前的错误
  try {
    if (!currentUserId) {
        error.value = '用户未登录';
        loading.value = false;
        return;
    }

    const res = await request.get('/api/notifications', {
      params: {
        userId: currentUserId // 传递当前用户的 ID
      }
    });

    if (res.success) {
      messages.value = res.data.map(msg => ({
        ...msg,
        // 确保 notificationType 字段被正确接收
        notificationType: msg.notificationType, 
        // isRead=0 → 未读 → unread=true；isRead=1 → 已读 → unread=false
        unread: msg.isRead !== 1, 
      }));
    } else {
        throw new Error(res.message || '获取消息列表失败');
    }
  } catch (err) {
    console.error('加载历史消息失败:', err);
    toast.error('加载消息失败');
    error.value = `加载历史消息失败：${err.message || '请检查网络'}`;
  } finally {
    loading.value = false;
  }
};

/**
 * 初始化WebSocket连接
 */
const initWebSocket = () => {
  // 清除之前的连接
  if (webSocket.value) {
    webSocket.value.close();
  }

  error.value = '';
  // 不设置 loading=true，避免覆盖 fetchHistoryMessages 的状态

  try {
    if (!currentUserId) {
      error.value = '请先登录以接收消息通知';
      return;
    }
    
    // 创建WebSocket连接 - 使用userId作为连接标识
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${wsProtocol}//localhost:8086/ws/${currentUserId}`; 
    
    webSocket.value = new WebSocket(wsUrl);

    // 连接成功
    webSocket.value.onopen = () => {
      console.log('WebSocket连接成功');
      isConnected.value = true;
      error.value = '';
      // toast.success('已连接消息通知');
    };

    // 接收消息
    webSocket.value.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        handleNewMessage(message);
      } catch (err) {
        console.error('解析消息失败:', err);
      }
    };

    // 连接关闭
    webSocket.value.onclose = (event) => {
      console.log('WebSocket连接关闭，代码:', event.code);
      isConnected.value = false;
      
      // 不是手动关闭的连接，尝试重连
      if (event.code !== 1000) {
        error.value = '消息连接已断开，正在尝试重连...';
        // 3秒后重连
        setTimeout(initWebSocket, 3000);
      }
    };

    // 连接错误
    webSocket.value.onerror = (err) => {
      console.error('WebSocket错误:', err);
      isConnected.value = false;
      error.value = '消息连接出错，请检查网络';
    };
  } catch (err) {
    console.error('初始化WebSocket失败:', err);
    error.value = '无法建立消息连接';
  }
};

/**
 * 处理新接收的消息
 */
const handleNewMessage = (message) => {
  console.log('Notifications收到WebSocket消息:', message);
  
  // 处理订单相关消息(不显示toast,由订单页面处理)
  if (message.type === 'order_update' || message.type === 'new_order') {
    console.log('订单消息,不在通知页面显示toast');
    return; // 订单消息由OrderList/MerchantOrders页面处理
  }
  
  // 处理钱包消息(不显示toast,由钱包页面处理)
  if (message.type === 'wallet_opened') {
    console.log('钱包消息,不在通知页面显示toast');
    return; // 钱包消息由Wallet页面处理
  }
  
  // 1. 检查是否为当前用户的消息
  if (message.userId && message.userId !== currentUserId) {
    return;
  }

  // 2. 验证消息合法性
  const isValidMessage = message.notificationType !== undefined && message.notificationContent;
  if (!isValidMessage) {
    console.warn('收到无效的WebSocket消息:', message);
    return;
  }
  
  // 3. 显示即时提示(仅显示"您有新消息啦")
  toast.info('您有新消息啦');

  // 4. 延迟调用接口重新拉取消息列表（保证数据一致性）
  setTimeout(async () => {
    try {
      const res = await request.get('/api/notifications', {
         params: { userId: currentUserId }
      });
      if (res.success) {
        // 5. 重新映射消息状态，确保 notificationType 被保留
        messages.value = res.data.map(msg => ({
          ...msg,
          notificationType: msg.notificationType, 
          unread: msg.isRead !== 1,
          createTime: msg.createTime
        }));
      }
    } catch (err) {
      console.error('WebSocket消息触发重新拉取失败:', err);
      toast.error('新消息已收到，但加载失败，可下拉刷新重试');
    }
  }, 300);
};

/**
 * 标记消息为已读
 */
const markAsRead = async (message) => {
  if (!message.unread) return;

  try {
    // 调用接口标记为已读
    await request.put(`/api/notifications/${message.id}/read`);
    
    // 更新本地状态
    message.unread = false;
  } catch (err) {
    console.error('标记消息为已读失败:', err);
    toast.error('更新消息状态失败');
  }
};

/**
 * 格式化时间显示
 */
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css');

.notifications-container {
  max-width: 600px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #333;
  min-height: 100vh;
  background-color: #fff;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  max-width: 600px; /* 确保 header 不超出容器 */
  height: 50px;
  background-color: #0097ff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.title {
  font-size: 1.1rem;
  color: #ffffff;
  font-weight: 600;
  margin: 0;
}

.back-btn-container {
    position: fixed; /* 固定定位，不随滚动移动 */
    left: 0vw; /* 距离左侧的距离，可根据需求调整 */
    top: 0vw; /* 距离顶部的距离，与 header 高度（12vw）适配，确保垂直居中 */
    z-index: 1001; /* 比 header 的 z-index:1000 高，避免被遮挡 */
}

.unread-badge {
  position: absolute;
  top: 15px;
  right: 20px;
  background-color: #ff4d4f;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

/* 留出固定头部和返回按钮的空间 */
.notification-list {
  padding: 0;
  margin-top: calc(50px + 10vw); /* 50px 是 header 高度 */
  padding-bottom: 20px;
}

.notification-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
  cursor: pointer;
}

.notification-item:hover {
  background-color: #f5f8ff; /* 浅蓝色背景 */
}

.notification-item:last-child {
  border-bottom: none;
}

.icon-wrapper {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 15px;
  background-color: #e6f0ff; /* 默认浅蓝色 */
}

.icon-wrapper.unread {
  background-color: #c9e2ff; /* 针对未读消息的更深的蓝色 */
}

/* --- 积分过期预警样式 --- */

/* 针对积分预警的图标背景 */
.icon-wrapper.expiry-warning {
  background-color: #fffbe6; /* 浅黄色背景 */
}

.icon {
  width: 20px;
  height: 20px;
  color: #1a73e8; /* 默认图标蓝色 */
}

/* 针对积分预警图标本身的颜色 (Font Awesome) */
.icon.fa-hourglass-half {
  color: #faad14; /* 警告黄色 */
  font-size: 1.1rem; /* 调整 Font Awesome 图标大小 */
}

.content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.message-text {
  font-size: 16px;
  line-height: 1.5;
  color: #555;
}

.message-text.bold {
  font-weight: 600;
  color: #1a73e8; /* 未读消息文本蓝色 */
}

/* 针对积分预警文本的颜色，让它更醒目 */
.message-text.warning-text {
  color: #d46b08; /* 偏深的警告色 */
}

/* 如果是未读的积分预警，可以更突出 */
.message-text.bold.warning-text {
  font-weight: 700;
  color: #cf1322; /* 红色强调 */
}

.timestamp {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.dot {
  width: 8px;
  height: 8px;
  background-color: #ff4d4f; /* 未读小红点 */
  border-radius: 50%;
  margin-left: 10px;
  flex-shrink: 0;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: 15px;
  color: #ddd;
}

.empty-state p {
  font-size: 1.1rem;
}

/* 加载状态样式 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1a73e8;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误状态样式 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #f5222d;
  text-align: center;
}

.error-state i {
  font-size: 3rem;
  margin-bottom: 15px;
}

.error-state p {
  font-size: 1.1rem;
  margin-bottom: 20px;
}

.retry-btn {
  background-color: #1a73e8;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.retry-btn:hover {
  background-color: #0d66d0;
}

.top-background {
  width: 100%;
  height: 100px;
  background: linear-gradient(to right, #3a7bd5, #00d2ff);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 16px 16px 0 0;
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  overflow: hidden;
  margin-bottom: 50px;
  max-width: 600px;
}

.top-background::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 70%);
  transform: rotate(30deg);
  animation: shine 6s infinite linear;
}

.top-background h1 {
  color: white;
  font-size: 1.8rem;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: 1px;
  margin: 0;
  z-index: 1;
}
</style>