<template>
  <div class="back-btn-container">
    <BackButton style="margin-top: 2vw;"/>
  </div>
  <div class="wrapper">
    <header>
      <p>支付成功</p>
    </header>
    
    <div class="content">
      <div class="card">
        <div class="header-section">
          <div class="icon-section">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="check-icon">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-8.82"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
          </div>
          <h2 class="title">支付成功</h2>
        </div>

      <div class="details">
        <!-- 商家名称 -->
        <div class="detail-item">
          <span class="label">商家名称</span>
          <span class="value">{{ paymentDetails.business?.businessName || '未知商家' }}</span>
        </div>

        <!-- 订单总额 -->
        <div class="detail-item total-section">
          <span class="label">订单总额</span>
          <span class="value">¥{{ paymentDetails.orderTotal }}</span>
        </div>

        <!-- 积分抵扣 - 仅在有抵扣金额时显示 -->
        <div class="detail-item deduction-section" v-if="hasPointsDeduction">
          <span class="label deduction-label">积分抵扣</span>
          <span class="value deduction-value">- ¥{{ pointsDeductionAmount }}</span>
        </div>

        <!-- 实付金额 -->
        <div class="detail-item actual-paid-section">
          <span class="label actual-paid-label">实付金额</span>
          <span class="value actual-paid-amount">¥{{ actualPaidAmount }}</span>
        </div>

        <!-- 本单可获积分 -->
        <div class="detail-item" v-if="pointsAmount > 0">
          <span class="label">本单可获积分</span>
          <span class="value points-value">
            <i class="fas fa-star"></i>
            {{ pointsAmount }}
          </span>
        </div>

        <!-- 支付时间 -->
        <div class="detail-item">
          <span class="label">支付时间</span>
          <span class="value">{{ paymentDetails.orderDate }}</span>
        </div>
      </div>

        <div class="actions">
          <button @click="goBack" class="btn-back">去查看订单</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// 假设 '@/utils/request' 路径正确
import request from '@/trade/utils/tradeRequest';
import BackButton from '../components/BackButton.vue'; 

// 定义常量
const POINT_DEDUCTION_KEY = 'usePointsDeduction'; 

const route = useRoute();
const router = useRouter();
const paymentDetails = ref({
    business: null,
    orderTotal: '0.00',
    pointsDeduction: '0.00', // 存储积分抵扣金额 (来自后端)
    orderDate: ''
});
const orderId = ref(route.query.orderId);
const pointsAmount = ref(0); // 本单可获积分

// 辅助判断：用于控制积分抵扣行是否显示
const hasPointsDeduction = computed(() => {
    const deduction = parseFloat(paymentDetails.value.pointsDeduction);
    // 只有当积分抵扣金额大于 0.00 时才显示该行
    return deduction > 0.00;
});

// 计算实付金额
const actualPaidAmount = computed(() => {
    const total = parseFloat(paymentDetails.value.orderTotal) || 0;
    const deduction = parseFloat(paymentDetails.value.pointsDeduction) || 0; 
    // 确保实付金额不为负
    return Math.max(0, total - deduction).toFixed(2);
});

// 格式化积分抵扣金额
const pointsDeductionAmount = computed(() =>
    (parseFloat(paymentDetails.value.pointsDeduction) || 0).toFixed(2)
);

onMounted(async () => {
    console.log('[LIFECYCLE] SuccessfulPayment 页面挂载。');
    console.log(`[INFO] 订单ID: ${orderId.value}`);
    
    if (!orderId.value) {
        console.error('[ERROR] 缺少订单ID，无法查询详情');
        return;
    }

    try {
        console.log('[ACTION] 正在请求订单详情（获取最终数据，包括抵扣金额）。');
        
        // --- 接口修改：使用新的 Path 参数结构 GET /api/orders/{id} ---
        const apiUrl = `/api/orders/${orderId.value}`; 
        const orderResponse = await request.get(apiUrl);
        // --- 接口修改结束 ---
        
        if (!orderResponse.success || !orderResponse.data) {
            console.error('[ERROR] 订单 API 返回异常或 data 为空', orderResponse.message);
            // 可以在此处添加友好的错误提示
            return;
        }

        const orderData = orderResponse.data;
        const totalAmount = parseFloat(orderData.orderTotal) || 0;
        // 积分抵扣的现金金额字段为 pointsDiscountAmount
        const deductionAmount = parseFloat(orderData.pointsDiscountAmount) || 0;
        // 本单可获积分字段为 pointsAmount
        pointsAmount.value = orderData.pointsAmount || 0;

        paymentDetails.value = {
            business: orderData.business,
            orderTotal: totalAmount.toFixed(2),
            pointsDeduction: deductionAmount.toFixed(2), 
            orderDate: orderData.orderDate
        };
        
        console.log(`[INFO] 从后端获取的订单总额: ¥${totalAmount.toFixed(2)}`);
        console.log(`[INFO] 从后端获取的积分抵扣金额: ¥${deductionAmount.toFixed(2)}`);
        console.log(`[INFO] 从后端获取的本单可获积分: ${pointsAmount.value}`);
        
        // 清除 sessionStorage 中的临时数据
        if (sessionStorage.getItem(POINT_DEDUCTION_KEY) !== null) {
            sessionStorage.removeItem(POINT_DEDUCTION_KEY);
            console.log('[INFO] SessionStorage 积分抵扣临时状态已清除。');
        }

    } catch (err) {
        console.error('[ERROR] Error fetching order details:', err);
    }
});

const goBack = () => {
    console.log('[ACTION] 跳转至订单列表。');
    // 假设订单列表的路由是 '/orderList'
    router.push('/trade/orderList');
};

// 变量和函数自动暴露给模板
</script>

<style scoped>
/* 保持原有的样式，确保页面的美观和响应式 */
:root {
  --primary-color: #2e7d32;
  --secondary-color: #f5f7fa;
  --text-color-primary: #1a202c;
  --text-color-secondary: #4a5568;
  --card-bg-color: #ffffff;
  --button-color: #2563eb;
  --button-hover-color: #1e40af;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "PingFang SC", "Microsoft YaHei", sans-serif;
  background-color: var(--secondary-color);
}

.wrapper {
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  background-color: #f5f7fa;
}

/****************** header部分 ******************/
.wrapper header {
  width: 100%;
  height: 12vw;
  background-color: #0097FF;
  color: #fff;
  font-size: 4.8vw;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.content {
  padding-top: 14vw;
  padding-bottom: 4vw;
  width: 100%;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* 返回按钮容器 */
.back-btn-container {
  position: fixed;
  left: 0vw;
  top: 0vw;
  z-index: 1001;
}

/* 本单可获积分样式 */
.points-value {
  display: flex;
  align-items: center;
  gap: 1vw;
  color: #ff6b00;
  font-weight: bold;
}

.points-value i {
  color: #ffa500;
  font-size: 1em;
}

.content .card {
  background-color: var(--card-bg-color);
  border-radius: 3vw;
  box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.05);
  padding: 4vw;
  width: calc(100% - 6vw);
  max-width: 420px;
  margin: 0 auto;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 2rem;
  box-sizing: border-box;
}

.header-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
}

.icon-section {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background-color: #e6f6e8;
  animation: scale-in 0.5s ease both;
}

.check-icon {
  width: 55px;
  height: 55px;
  color: #2e7d32;
  animation: fade-in 0.8s ease 0.2s both;
}

.title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-color-primary);
  margin: 0;
  white-space: nowrap;
  animation: slide-up 0.6s ease 0.3s both;
}

.details {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  text-align: left;
  border-top: 1px solid #e2e8f0;
  padding-top: 1.5rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1rem;
}

.label {
  color: var(--text-color-secondary);
  font-weight: 500;
}

.value {
  color: var(--text-color-primary);
  font-weight: 600;
}

.deduction-value {
  color: #f56565;
}

.actual-paid-section {
  border-top: 2px solid #e2e8f0;
  padding-top: 1rem;
  margin-top: 0.5rem;
}

.actual-paid-label {
  font-size: 1.1rem;
  font-weight: 700;
}

.actual-paid-amount {
  font-size: 2.8rem;
  color: var(--primary-color);
  font-weight: 900;
  line-height: 1;
}

.actions {
  margin-top: 0.5rem;
}

.btn-back {
  width: 100%;
  padding: 1rem;
  background-color: #0493f2da;
  color: #e2e8f0;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.btn-back:hover {
  background-color: var(--button-hover-color);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.3);
}

@keyframes scale-in {
  from { transform: scale(0.8); opacity: 0; }
  to   { transform: scale(1); opacity: 1; }
}

@keyframes fade-in {
  from { opacity: 0; }
  to   { opacity: 1; }
}

@keyframes slide-up {
  from { transform: translateY(20px); opacity: 0; }
  to   { transform: translateY(0); opacity: 1; }
}

@media (min-width: 600px) {
  .card { padding: 3rem; }
  .title { font-size: 2.5rem; }
}
</style>