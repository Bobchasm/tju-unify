<template>
	<div class="wrapper">
		<!-- 固定顶部栏 -->
		<div class="fixed-top">
          <div class="top-background">
              <h1>订单详情</h1>
          </div>
      	</div>
		
		<!-- 内容区域 -->
		<div class="content-area">
			<!-- 加载提示 -->
			<div v-if="loading" class="loading">
				<p>加载中...</p>
			</div>
			
			<!-- 错误提示 -->
			<div v-else-if="error" class="error">
				<p>{{ error || ''}}</p>
				<button @click="retry">重新加载</button>
			</div>
			
			<!-- 订单详情内容 -->
			<div v-else class="content">
				<!-- 订单状态和基本信息 -->
				<div class="order-status">
					<div class="status-icon" :class="getStatusClass(orderDetail.orderState)">
						<i class="fa" :class="getStatusIcon(orderDetail.orderState)"></i>
					</div>
					<div class="status-info">
						<h3>{{ getStatusText(orderDetail.orderState) }}</h3>
						<p>订单号: {{ orderDetail.id || '-' }}</p>
						<p>下单时间: {{ formatTime(orderDetail.orderDate) || ''}}</p>
					</div>
				</div>

				<!-- 收货人信息 -->
				<div class="info-section">
					<h3 class="section-title">收货信息</h3>
					<div class="info-content">
						<p><span>收货人:</span> {{ orderDetail.contactName || '-' }} {{ getGenderText(orderDetail.contactSex) }}</p>
						<p><span>联系电话:</span> {{ orderDetail.contactTel || '-' }}</p>
						<p><span>配送地址:</span> {{ orderDetail.address || '-' }}</p>
					</div>
				</div>

				<!-- 商家信息 -->
				<div class="info-section">
					<h3 class="section-title">商家信息</h3>
					<div class="info-content">
						<p><span>商家名称:</span> {{ orderDetail.businessName || '-' }}</p>
					</div>
				</div>

				<!-- 商品明细 -->
				<div class="info-section">
					<h3 class="section-title">商品明细</h3>
					<div class="items-list">
						<div v-for="(item, index) in orderDetail.foodList" :key="index" class="item-row">
							<div class="item-info">
								<span class="item-name">{{ item.foodName || '未知商品' }} &#165;{{ item.foodPrice }} &nbsp; × {{ item.quantity || 0 }}</span>
							</div>
							<div class="item-price">¥ {{ (item.foodPrice * item.quantity).toFixed(2) }}</div>
						</div>
					</div>
				</div>

				<!-- 费用汇总 -->
				<div class="info-section">
					<h3 class="section-title">费用明细</h3>
					<div class="price-details">
						<div class="price-row">
							<span>商品金额</span>
							<span>¥ {{ itemsTotal.toFixed(2) || '0'}}</span>
						</div>
						<div class="price-row">
							<span>配送费</span>
							<span>&#165;{{ orderDetail.deliveryPrice.toFixed(2) || '0.00' }}</span>
						</div>
						<div class="price-row total">
							<span>实付款</span>
							<span>¥ {{ orderDetail.orderTotal.toFixed(2) || '0.00'}}</span>
						</div>
					</div>
				</div>

				<!-- 操作按钮
				<div v-if="orderDetail.orderState === 0" class="action-buttons">
					<button class="btn cancel-btn" @click="cancelOrder">取消订单</button>
					<button class="btn pay-btn" @click="payOrder">立即支付</button>
				</div> -->
			</div>
		</div>
	</div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/trade/utils/tradeRequest';

import BackButton from '../components/BackButton.vue';
export default {
	name: 'ListDetail',
	components: { BackButton },
	setup() {
		const route = useRoute();
		const router = useRouter();
		const orderId = ref(null);
		const orderDetail = ref({});
		const loading = ref(true);
		const error = ref('');
		
		// 配送费（这里假设固定值，实际应该从API获取）
		const deliveryPrice = ref(5);

		// 获取订单详情
		const fetchOrderDetail = async () => {
			loading.value = true;
			error.value = '';
			
			try {
				const response = await request.get("/api/orders/detail", {
					params: { orderId: orderId.value }
				});
				
				if (response.success) {
					orderDetail.value = response.data || {};
					console.log("订单详情:", orderDetail.value);
				} else {
					error.value = '获取订单详情失败: ' + response.message;
				}
			} catch (err) {
				console.error('获取订单详情失败:', err);
				error.value = '网络错误，请稍后重试';
			} finally {
				loading.value = false;
			}
		};

		// 重新加载
		const retry = () => {
			fetchOrderDetail();
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
				0: "status-unpaid",
				1: "status-pending",
				2: "status-accepted", 
				3: "status-done",
				4: "status-canceled"
			};
			return classMap[state] || "status-unknown";
		};

		// 获取状态图标
		const getStatusIcon = (state) => {
			const iconMap = {
				0: "fa-clock-o",
				1: "fa-hourglass-half",
				2: "fa-check-circle",
				3: "fa-check-circle",
				4: "fa-times-circle"
			};
			return iconMap[state] || "fa-question-circle";
		};

		// 获取性别文本
		const getGenderText = (gender) => {
			return gender === 1 ? '先生' : gender === 2 ? '女士' : '';
		};

		// 格式化时间
		const formatTime = (timeString) => {
			if (!timeString) return "-";
			try {
				const date = new Date(timeString);
				return date.toLocaleString('zh-CN');
			} catch (e) {
				return timeString;
			}
		};

		// 计算商品总金额
		const itemsTotal = computed(() => {
			if (!orderDetail.value.foodList || !Array.isArray(orderDetail.value.foodList)) {
				return 0;
			}
			return orderDetail.value.foodList.reduce((total, item) => {
				return total + (item.foodPrice || 0) * (item.quantity || 0);
			}, 0);
		});

		// 取消订单
		const cancelOrder = async () => {
			if (!confirm("确定要取消此订单吗？")) return;
			
			try {
				const response = await request.post("/api/orders/cancel", { 
					orderId: orderId.value 
				});
				
				if (response.data && response.data.success) {
					alert("订单取消成功");
					// 重新加载订单详情
					fetchOrderDetail();
				} else {
					// 显示后端返回的具体错误消息
					const errorMessage = (response.data && response.data.message) || response.message || "取消失败,请重试";
					alert(errorMessage);
				}
			} catch (err) {
				console.error("取消订单失败:", err);
				// 从错误响应中提取错误消息
				const errorMessage = err.response?.data?.message || err.message || "取消订单失败，请稍后重试";
				alert(errorMessage);
			}
		};

		// 支付订单
		const payOrder = () => {
			router.push({ 
				path: "/trade/payment", 
				query: { orderId: orderId.value } 
			});
		};

		onMounted(() => {
			orderId.value = route.query.orderId;
			if (!orderId.value) {
				error.value = "订单ID不能为空";
				loading.value = false;
				return;
			}
			
			fetchOrderDetail();
		});

		return {
			orderId,
			orderDetail,
			loading,
			error,
			deliveryPrice,
			itemsTotal,
			fetchOrderDetail,
			retry,
			getStatusText,
			getStatusClass,
			getStatusIcon,
			getGenderText,
			formatTime,
			cancelOrder,
			payOrder
		};
	}
};
</script>

<style scoped>
.wrapper {
	width: 100%;
	min-height: 100vh;
	background: #f5f7fa;
}

/****************** 固定顶部栏 ******************/
.fixed-top {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	z-index: 1000;
	background: white;
}

.header {
	width: 100%;
  height: 12vw;
  background-color: #0097ff;
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

.title {
	margin: 0;
	font-size: 24px;
	font-weight: 600;
	color: white;
}

/****************** 内容区域 ******************/
.content-area {
	margin-top: 100px; /* 固定顶部栏的高度 */
	padding-bottom: 20vw;
}

.loading, .error {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	padding: 20vw 4vw;
	font-size: 4vw;
	color: #666;
}

.error button {
	margin-top: 4vw;
	padding: 2vw 6vw;
	background: #409eff;
	color: white;
	border: none;
	border-radius: 1vw;
	cursor: pointer;
}

/* 订单状态区域 */
.order-status {
	display: flex;
	align-items: center;
	padding: 6vw 4vw;
	background: white;
	margin-bottom: 3vw;
}

.status-icon {
	width: 16vw;
	height: 16vw;
	border-radius: 50%;
	display: flex;
	justify-content: center;
	align-items: center;
	margin-right: 4vw;
	font-size: 8vw;
}

.status-unpaid {
	background: #fff0f0;
	color: #ff4d4f;
}

.status-pending {
	background: #e6f7ff;
	color: #1890ff;
}

.status-accepted {
	background: #f6ffed;
	color: #52c41a;
}

.status-done {
	background: #f9f9f9;
	color: #999;
}

.status-canceled {
	background: #f9f9f9;
	color: #999;
}

.status-unknown {
	background: #f9f9f9;
	color: #666;
}

.status-info h3 {
	font-size: 4.5vw;
	color: #333;
	margin-bottom: 1vw;
	font-weight: bold;
}

.status-info p {
	font-size: 3.6vw;
	color: #666;
	margin: 0.5vw 0;
}

/* 信息区块 */
.info-section {
	background: white;
	margin-bottom: 3vw;
	padding: 4vw;
}

.section-title {
	font-size: 4.2vw;
	color: #333;
	margin-bottom: 3vw;
	font-weight: bold;
	border-bottom: 1px solid #f0f0f0;
	padding-bottom: 2vw;
}

.info-content p {
	font-size: 3.8vw;
	color: #333;
	margin: 2vw 0;
	display: flex;
}

.info-content span {
	color: #666;
	margin-right: 2vw;
	min-width: 20vw;
}

/* 商品列表 */
.items-list {
	border-top: 1px solid #f0f0f0;
}

.item-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 3vw 0;
	border-bottom: 1px solid #f0f0f0;
}

.item-info {
	flex: 1;
}

.item-name {
	font-size: 3.8vw;
	color: #333;
}

.item-quantity {
	font-size: 3.4vw;
	color: #666;
	margin-left: 2vw;
}

.item-price {
	font-size: 3.8vw;
	color: #333;
	font-weight: 500;
}

/* 价格明细 */
.price-details {
	border-top: 1px solid #f0f0f0;
	padding-top: 3vw;
}

.price-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 2vw 0;
	font-size: 3.8vw;
	color: #333;
}

.price-row.total {
	border-top: 1px solid #f0f0f0;
	margin-top: 2vw;
	padding-top: 3vw;
	font-weight: bold;
	font-size: 4.2vw;
	color: #ff6b00;
}

/* 操作按钮 */
.action-buttons {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: white;
	padding: 3vw 4vw;
	display: flex;
	justify-content: flex-end;
	gap: 3vw;
	border-top: 1px solid #f0f0f0;
}

.btn {
	padding: 3vw 6vw;
	border-radius: 1.6vw;
	font-size: 3.8vw;
	cursor: pointer;
	border: none;
}

.cancel-btn {
	background: #fff;
	color: #666;
	border: 1px solid #ddd;
}

.pay-btn {
	background: #409eff;
	color: #fff;
}

.top-background {
  width: 100%;
  height: 90px;
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