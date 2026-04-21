<template>
  <div class="wrapper">
    <!-- 顶部蓝色栏 -->
      <div class="fixed-top">
          <div class="top-background">
              <h1>订单</h1>
          </div>
      </div>

    <div class="fixed-header">

      <!-- 页面标题 -->
      <div class="page-title">订单中心</div>


      <!-- 筛选标签栏 -->
      <ul class="tabs">
        <li v-for="(t, idx) in tabs" :key="t" :class="{ active: activeTab === idx }" @click="changeTab(idx)">
          {{ t }} <span v-if="orderCounts[idx] > 0">({{ orderCounts[idx] }})</span>
        </li>
      </ul>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 加载提示 -->
      <div v-if="loading" class="loading">
        <p>加载中...</p>
      </div>

      <!-- 空状态提示 -->
      <div v-else-if="displayedOrders.length === 0" class="empty-state">
        <img src="/trade-assets/empty-order.png" alt="暂无订单">
        <p>暂无订单</p>
      </div>

      <!-- 订单列表 -->
      <ul v-else class="order-list">
        <li v-for="item in displayedOrders" :key="item.id" class="order-item" @click="goDetail(item.id)" title="查看详情">
          <div class="order-header">
            <span class="order-id">订单号: {{ item.id }}</span>
            <span class="status-badge" :class="getStatusClass(item.orderState)">{{ getStatusText(item.orderState)
            }}</span>
          </div>

          <div class="order-content">
            <img class="thumb" :src="item.businessImg || '/trade-assets/default-business.png'" alt="商家图片">
            <div class="meta">
              <p class="name">{{ item.businessName || '未知商家' }}</p>
              <p class="price">¥ {{ Number(item.orderTotal).toFixed(2) || 0}}</p><br>
              <p class="time">下单时间: {{ item.orderDate || ''}}</p>
            </div>
          </div>

          <div class="actions">
            <!-- 待支付：取消 + 付款 -->
            <template v-if="item.orderState === 0">
              <button class="cancel-btn" @click.stop="cancelOrder(item.id)">取消订单</button>
              <button class="pay-btn" @click.stop="payOrder(item.id)">立即支付</button>
            </template>

            <!-- 待接单：取消订单 -->
            <template v-else-if="item.orderState === 1">
              <button class="cancel-btn" @click.stop="cancelOrder(item.id)">取消订单</button>
            </template>

            <!-- 已接单：确认收货 -->
            <template v-else-if="item.orderState === 2">
              <button class="confirm-btn" @click.stop="confirmOrder(item.id)">确认收货</button>
            </template>

            <!-- 已完成/已取消：查看详情 -->
            <template v-else>
              <button class="detail-btn" @click.stop="goDetail(item.id)">查看详情</button>
            </template>
          </div>
        </li>
      </ul>
    </div>

    <!-- 底部导航 -->
    <Footer />
  </div>

  <!-- 确认收货弹窗 -->
  <div v-if="showConfirmFinishedModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>确认操作</h3>
        <span class="close-btn" @click="closeModal">&times;</span>
      </div>
      <div class="modal-body">
        <p>确定要确认收货吗？</p>
      </div>
      <div class="modal-footer">
        <button class="modal-btn confirm-btn" @click="confirmFinished">确认</button>
        <button class="modal-btn cancel-btn" @click="closeModal">取消</button>
      </div>
    </div>
  </div>

  <!-- 确认取消弹窗 -->
  <div v-if="showConfirmCanceledModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>确认操作</h3>
        <span class="close-btn" @click="closeModal">&times;</span>
      </div>
      <div class="modal-body">
        <p>确认要取消订单吗？</p>
      </div>
      <div class="modal-footer">
        <button class="modal-btn confirm-btn" @click="confirmCanceled">确认</button>
        <button class="modal-btn cancel-btn" @click="closeModal">取消</button>
      </div>
    </div>
  </div>
</template>
  
<script>
import { ref, onMounted, computed, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import request from "@/trade/utils/tradeRequest";
import Footer from '../components/Footer.vue';
import { toast } from '@/trade/utils/toast';

export default {
  name: "OrderList",
  components: {
    Footer
  },
  setup() {
    const orderArr = ref([]);
    const userInfo = ref({});
    const router = useRouter();
    const loading = ref(false);
    // --- WebSocket 相关 ---
    const socket = ref(null);
    const userId = ref(null); // 存储当前用户ID

    // 标签定义 - 与API状态对应
    const tabs = ["全部", "待支付", "待接单", "已接单", "已完成", "已取消"];
    const activeTab = ref(0);
    const showConfirmFinishedModal = ref(false);
    const showConfirmCanceledModal = ref(false);
    const selectId = ref(0);

    // 标签对应的API状态值
    const tabStatusMap = {
      0: null,     // 全部
      1: 0,        // 待支付
      2: 1,        // 待接单
      3: 2,        // 已接单
      4: 3,        // 已完成
      5: 4         // 已取消
    };

    // 获取订单列表
    const fetchOrders = async (status = null) => {
      loading.value = true;
      try {
        const params = {};
        if (status !== null) {
          params.orderState = status;
        }

        const response = await request.get("/api/orders/list/user"
          + (status === null ? "" : ("?orderState=" + status)));

        if (response.success) {
          orderArr.value = response.data || [];
          console.log("获取订单列表成功:", orderArr.value);
        } else {
          console.error('获取订单列表失败:', response.data.message);
          toast.error('获取订单列表失败: ' + response.data.message);
        }
      } catch (error) {
        console.error("请求订单列表失败:", error);
        toast.error("获取订单列表失败，请稍后重试！");
      } finally {
        loading.value = false;
      }
    };

    // 计算各状态订单数量 - 基于完整订单列表
    const orderCounts = computed(() => {
      // 初始化一个数组，长度与tabs一致，初始值为0
      const counts = new Array(tabs.length).fill(0);

      // 获取完整的订单列表（从后端API获取的所有订单）
      const allOrders = orderArr.value;

      // 遍历所有订单进行统计
      allOrders.forEach(order => {
        const state = order.orderState;

        // 全部订单数
        counts[0]++;

        // 根据订单状态，增加对应标签的计数
        if (state === 0) counts[1]++;       // 待支付 -> 索引1
        else if (state === 1) counts[2]++;  // 待接单 -> 索引2
        else if (state === 2) counts[3]++;  // 已接单 -> 索引3
        else if (state === 3) counts[4]++;  // 已完成 -> 索引4
        else if (state === 4) counts[5]++;  // 已取消 -> 索引5
      });

      return counts;
    });

    // 计算显示的订单 - 基于当前选中的标签
    const displayedOrders = computed(() => {
      if (activeTab.value === 0) return orderArr.value; // 全部

      const targetStatus = tabStatusMap[activeTab.value];
      return orderArr.value.filter(order => order.orderState === targetStatus);
    });

    // 切换标签 - 只需要改变activeTab，displayedOrders会自动更新
    const changeTab = (index) => {
      activeTab.value = index;
      // 不再需要在这里调用fetchOrders，因为displayedOrders是计算属性
    };

    // 获取状态文本
    const getStatusText = (state) => {
      const statusMap = {
        0: "待支付",
        1: "待接单",
        2: "已接单",
        3: "已完成",
        4: "已取消"
      };
      return statusMap[state] || "未知状态";
    };

    // 获取状态样式类
    const getStatusClass = (state) => {
      const classMap = {
        0: "unpaid",
        1: "pending",
        2: "accepted",
        3: "done",
        4: "canceled"
      };
      return classMap[state] || "";
    };

    // 格式化时间
    const formatTime = (timeString) => {
      if (!timeString) return "";
      const date = new Date(timeString);
      return date.toLocaleString();
    };

    // 取消订单
    const cancelOrder = (id) => {
      selectId.value = id;
      showConfirmCanceledModal.value = true;
    };

    // 确认取消订单
    const confirmCanceled = async () => {
      if (selectId.value === 0) return;

      try {
        const response = await request.put("/api/orders/status?orderState=4&orderId=" + selectId.value);

        if (response.success) {
          toast.success("订单取消成功");
          // 重新加载订单
          fetchOrders();
        } else {
          // 显示后端返回的具体错误消息
          const errorMessage = response.message || "取消失败,请重试";
          toast.error(errorMessage);
        }
      } catch (error) {
        // 从错误响应中提取错误消息
        const errorMessage = error.response?.data?.message || error.message || "取消失败,请重试";
        toast.error(errorMessage);
      } finally {
        closeModal();
      }
    };

    // 支付订单
    const payOrder = (orderId) => {
      router.push({ path: "/trade/payment", query: { orderId } });
    };

    // 确认收货
    const confirmOrder = (id) => {
      selectId.value = id;
      showConfirmFinishedModal.value = true;
    };

    // 确认完成订单
    const confirmFinished = async () => {
      if (selectId.value === 0) return;

      try {
        const response = await request.put("/api/orders/status?orderState=3&orderId=" + selectId.value);

        if (response.success) {
          toast.success("订单完成");
          // 重新加载订单
          fetchOrders();
        } else {
          toast.error("确认完成失败,请重试");
        }
      } catch (error) {
        toast.error("确认完成失败,请重试");
      } finally {
        closeModal();
      }
    };

    // 查看订单详情
    const goDetail = (id) => {
      router.push({
        path: '/trade/listDetail',
        query: { orderId: id }
      });
    };

    // 关闭弹窗
    const closeModal = () => {
      showConfirmFinishedModal.value = false;
      showConfirmCanceledModal.value = false;
      selectId.value = 0;
    };

    // 初始化WebSocket
    const initWebSocket = () => {
      userId.value = userInfo.value.id;
      if (!userId.value) return;

      socket.value = new WebSocket(`ws://localhost:7070/ws/${userId.value}`);

      socket.value.onopen = () => {
        console.log("WebSocket 连接成功");
      };

      socket.value.onmessage = (event) => {
        const message = JSON.parse(event.data);
        console.log("收到WebSocket消息:", message);
        
        // 处理订单状态更新消息
        if (message.type === 'order_update') {
          console.log("🔄 订单状态更新, 订单ID:", message.orderId, ", 新状态:", message.orderState);
          
          // 根据订单状态显示不同提示
          // 0-待支付,1-待接单,2-已接单,3-已完成,4-已取消
          const statusMessages = {
            0: '订单待支付',
            1: '订单已支付，等待商家接单',
            2: '商家已接单，正在配送',
            3: '订单已完成',
            4: '订单已取消'
          };
          
          const statusMsg = statusMessages[message.orderState] || '订单状态已更新';
          toast.info(statusMsg);
          
          // 刷新订单列表
          fetchOrders();
        }
        // 其他消息也刷新列表
        else {
          fetchOrders();
        }
      };

      socket.value.onclose = () => {
        console.log("WebSocket 连接关闭");
        // 断线重连（可选，视需求添加）
        setTimeout(initWebSocket, 2000);
      };

      socket.value.onerror = (err) => {
        console.error("WebSocket 错误:", err);
      };
    };

    // 关闭WebSocket（组件销毁时调用）
    const closeWebSocket = () => {
      if (socket.value && socket.value.readyState === WebSocket.OPEN) {
        socket.value.close();
      }
    };

    onMounted(() => {
      // 获取用户信息
      const userData = sessionStorage.getItem("userInfo") || localStorage.getItem("userInfo");
      userInfo.value = userData ? JSON.parse(userData) : null;

      if (!userInfo.value) {
        toast.error("用户未登录，请先登录！");
        router.push({ path: "/trade/login" });
        return;
      }
      initWebSocket();
      // 初始加载全部订单
      fetchOrders();
    });

    onUnmounted(() => {
      // 组件销毁时关闭WebSocket
      closeWebSocket();
    });

    return {
      orderArr,
      userInfo,
      tabs,
      activeTab,
      displayedOrders,
      loading,
      changeTab,
      getStatusText,
      getStatusClass,
      formatTime,
      cancelOrder,
      payOrder,
      confirmOrder,
      goDetail,
      orderCounts,
      confirmCanceled,
      confirmFinished,
      showConfirmFinishedModal,
      showConfirmCanceledModal,
      closeModal
    };
  }
};
</script>
  
<style scoped>
/****************** 容器与顶部 ******************/
.wrapper {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  min-height: 100vh;
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

@keyframes shine {
  0% {
    transform: rotate(30deg) translate(-10%, -10%);
  }
  100% {
    transform: rotate(30deg) translate(10%, 10%);
  }
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

/****************** 固定标题和筛选栏 ******************/
.fixed-header {
  position: fixed;
  top: 100px; /* 在顶部背景下方 */
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 600px;
  z-index: 999;
  background: white;
}

.page-title {
  padding: 4vw;
  font-size: 4.5vw;
  color: #333;
  font-weight: bold;
  background: white;
  text-align: center;
  margin-left: auto;
  margin-right: auto;
  /* 如果需要限制宽度，可以添加 width */
  width: fit-content; /* 宽度适应内容，可选 */
}

/****************** 标签栏 ******************/
.tabs {
  display: flex;
  align-items: center;
  padding: 0 4vw;
  background: white;
  border-bottom: 1px solid #f0f0f0;
  overflow-x: auto;
  white-space: nowrap;
}

.tabs li {
  margin-right: 6vw;
  padding: 3vw 0;
  font-size: 3.8vw;
  color: #666;
  position: relative;
  cursor: pointer;
}

.tabs li.active {
  color: #409eff;
  font-weight: 600;
}

.tabs li.active::after {
  content: "";
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 0.8vw;
  background: #409eff;
  border-radius: 0.4vw;
}

/****************** 内容区域 ******************/
.content-area {
  margin-top: calc(100px + 18vw); /* 顶部背景高度 + 固定标题和筛选栏高度 */
  padding: 0 4vw;
  margin-bottom: 15vw;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

/****************** 加载和空状态 ******************/
.loading,
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10vw;
  font-size: 4vw;
  color: #999;
}

.empty-state {
  flex-direction: column;
}

.empty-state img {
  width: 30vw;
  height: 30vw;
  margin-bottom: 4vw;
  opacity: 0.5;
}

/****************** 订单列表 ******************/
.order-list {
  padding: 4vw 0;
  /* 调整内边距 */
}

.order-item {
  background: #fff;
  border-radius: 2vw;
  box-shadow: 0 1vw 2vw rgba(0, 0, 0, .05);
  padding: 4vw;
  margin-bottom: 4vw;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 3vw;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 3vw;
}

.order-id {
  font-size: 3.6vw;
  color: #999;
}

.status-badge {
  padding: 1vw 2vw;
  border-radius: 1vw;
  font-size: 3.2vw;
  font-weight: 500;
  position: relative;
  z-index: 10;
  /* 确保在最上层 */
}

.status-badge.unpaid {
  background: #fff0f0;
  color: #ff4d4f;
}

.status-badge.pending {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.accepted {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.done {
  background: #fdf4de;
  color: #ffa700;
}

.status-badge.canceled {
  background: #f9f9f9;
  color: #999;
}

.order-content {
  display: flex;
  align-items: center;
  margin-bottom: 4vw;
}

.thumb {
  width: 20vw;
  height: 20vw;
  object-fit: cover;
  border-radius: 1.2vw;
  margin-right: 3vw;
}

.meta {
  flex: 1;
}

.name {
  font-size: 4.2vw;
  color: #333;
  font-weight: 500;
  margin-bottom: 1vw;
}

.items,
.time {
  font-size: 3.4vw;
  color: #999;
  margin-bottom: 1vw;
}

.price {
  font-size: 4.5vw;
  color: #ff6b00;
  font-weight: bold;
  margin-top: 2vw;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 2vw;
}

.actions button {
  padding: 2vw 4vw;
  border-radius: 1.6vw;
  font-size: 3.6vw;
  cursor: pointer;
  border: none;
}

.cancel-btn {
  background: #fff;
  color: #666;
  border: 1px solid #ddd !important;
}

.pay-btn {
  background: #409eff;
  color: #fff;
}

.confirm-btn {
  background: #52c41a;
  color: #fff;
}

.detail-btn {
  background: #f5f5f5;
  color: #666;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 20px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  gap: 15px;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95) translateY(20px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.close-btn {
  font-size: 1.5rem;
  color: #aaa;
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #666;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.modal-body p {
  color: #555;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.modal-btn {
  border: none;
  border-radius: 20px;
  padding: 10px 20px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-btn {
  background-color: #e0e0e0;
  color: #333;
}

.cancel-btn:hover {
  background-color: #c7c7c7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.confirm-btn {
  background-color: #1e80ff;
  color: white;
}

.confirm-btn:hover {
  background-color: #0085e0;
  box-shadow: 0 4px 12px rgba(30, 128, 255, 0.3);
}

@media (max-width: 480px) {
  .wrapper {
    max-width: 100vw;
    width: 100vw;
  }
  
  .top-background {
    height: 90px;
    border-radius: 0;
    max-width: 100vw;
  }
  
  .fixed-header {
    top: 90px;
    max-width: 100vw;
    transform: none;
    left: 0;
  }
  
  .content-area {
    margin-top: calc(130px + 18vw);
    max-width: 100vw;
    width: 100vw;
  }
}
</style>