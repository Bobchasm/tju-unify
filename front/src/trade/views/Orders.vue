<template>
	<div class="wrapper">
		<!-- header部分 -->
		<header>
			<p>确认订单</p>
		</header>

		<div v-if="loading" class="loading">
			<p>加载中...</p>
		</div>

		<template v-else>
			<!-- 订单信息部分 -->
			<div class="order-info">
				<h5>订单配送至：</h5>
				<div class="order-info-address" @click="toUserAddress">
					<p>{{ deliveryaddress ? deliveryaddress.address : '请选择送货地址' }}</p>
					<i class="fa fa-angle-right"></i>
				</div>
				<p v-if="deliver.contactName">{{ deliver.contactName }} {{ deliver.contactTel }}</p>
			</div>

			<h3>{{ business.businessName }}</h3>

			<!-- 订单明细部分 -->
			<ul class="order-detailed">
				<li v-for="item in cartArr" :key="item.id">
					<div class="order-detailed-left">
						<img :src="item.food.foodImg">
						<p>{{ item.food.foodName }} x {{ item.quantity }}</p>
					</div>
					<p>&#165;{{ (item.food.foodPrice * item.quantity).toFixed(2) }}</p>
				</li>
			</ul>
			<div class="order-deliveryfee">
				<p>配送费</p>
				<p>&#165;{{ business.deliveryPrice }}</p>
			</div>




			<ul class="cart">
				<li v-for="item in cartItems" :key="item.foodId">
					<!-- 先检查 food[item.foodId] 是否存在 -->
					<template v-if="food[item.foodId]">
						<div class="cart-img">
							<img :src="food[item.foodId].foodImg">
							<div class="cart-img-quantity" v-show="item.quantity > 0">{{ item.quantity }}</div>
						</div>
						<div class="cart-info">
							<h3>{{ food[item.foodId].foodName }}</h3>
							<p>&#165;{{ food[item.foodId].foodPrice }} / 份</p>
						</div>
					</template>
					<!-- 可选：显示加载状态或默认内容 -->
					<template v-else>
						<div>加载中...</div>
					</template>
				</li>
			</ul>








			<!-- 合计部分 -->
			<div class="total">
				<div class="total-left">
					&#165;{{ totalPrice.toFixed(2) }}
				</div>
				<div class="total-right" @click="toPayment">
					去支付
				</div>
			</div>
		</template>
	</div>
</template>

<script>
import { ref, computed, onMounted, defineComponent } from 'vue';
import Footer from '../components/Footer.vue';
import axios from 'axios';
import { useRouter, useRoute } from 'vue-router';
import { toast } from '@/trade/utils/toast';

export default {
	name: 'Orders',
	setup() {
		const route = useRoute();
		const businessId = ref(route.query.businessId);
		const business = ref({});
		const user = ref(null);
		const cartArr = ref([]);
		const deliveryaddress = ref(null);
		const deliver = ref({});
		const router = useRouter();
		const loading = ref(true);

		onMounted(async () => {
			try {
				user.value = sessionStorage.getItem('user') ? JSON.parse(sessionStorage.getItem('user')) : null;

				if (!user.value) {
					toast.warning('用户未登录，请先登录！');
					router.push({ path: '/trade/login' });
					return;
				}

				const savedAddress = localStorage.getItem(user.value.userId);
				if (savedAddress) {
					deliveryaddress.value = JSON.parse(savedAddress);
					try {
						const response = await axios.post('DeliveryAddressController/getDeliveryAddressById', {
							daId: deliveryaddress.value.daId
						});
						if (response.data) {
							deliver.value = response.data;
						} else {
							deliveryaddress.value = null;
							localStorage.removeItem(user.value.userId);
						}
					} catch (error) {
						console.error('获取配送地址详情失败:', error);
						deliveryaddress.value = null;
						localStorage.removeItem(user.value.userId);
					}
				}

if (businessId.value) {
  try {
    const businessResponse = await axios.post('BusinessController/getBusinessById', 
      {
        businessId: businessId.value  // 参数放在请求体中
      }
    );
    
    if (businessResponse.data) {
      business.value = businessResponse.data;
    } else {
      throw new Error('商家信息获取失败');
    }
  } catch (error) {
    console.error('获取商家信息失败:', error);
    throw new Error('商家信息获取失败');
  }
} else {
  throw new Error('商家ID无效');
}

				const cartResponse = await axios.post('CartController/listCart', {
					userId: user.value.userId,
					businessId: businessId.value
				});
				if (cartResponse.data) {
					cartArr.value = cartResponse.data;
				}

			} catch (error) {
				console.error('初始化订单数据失败:', error);
				toast.error('加载订单数据失败，请重试！');
				router.push({ path: '/trade' });
			} finally {
				loading.value = false;
			}
		});

		const totalPrice = computed(() => {
			return cartArr.value.reduce((acc, item) => acc + item.food.foodPrice * item.quantity, 0) + (business.value?.deliveryPrice || 0);
		});

		const sexFilter = (value) => value === 1 ? '先生' : '女士';

		const toUserAddress = () => {
			router.push({ path: '/trade/userAddress', query: { businessId: businessId.value } });
		};

		const toPayment = async () => {
			try {
				if (!user.value) {
					toast.warning('请先登录！');
					router.push({ path: '/trade/login' });
					return;
				}

				if (!deliveryaddress.value || !deliveryaddress.value.daId) {
					toast.warning('请选择配送地址！');
					router.push({ path: '/trade/userAddress', query: { businessId: businessId.value } });
					return;
				}

				if (!businessId.value || !business.value) {
					toast.error('商家信息无效，请重试！');
					return;
				}

				if (!cartArr.value.length) {
					toast.warning('购物车为空，请先选择商品！');
					router.push({ path: '/trade/businessInfo', query: { businessId: businessId.value } });
					return;
				}

				const orderData = {
					userId: user.value.userId,
					businessId: parseInt(businessId.value),
					daId: parseInt(deliveryaddress.value.daId),
					orderTotal: parseFloat(totalPrice.value.toFixed(2))  // 确保金额为数字类型
				};

				console.log('创建订单数据:', orderData);

				const response = await axios.post('OrdersController/createOrders', orderData);

				console.log('订单创建响应:', response.data);

				if (response.data > 0) {
					const orderId = response.data;
					try {
						await axios.post('CartController/removeCart', {
							userId: user.value.userId,
							businessId: businessId.value
						});
					} catch (error) {
						console.error('清除购物车失败:', error);
					}

					toast.success('订单创建成功！');
					router.push({ path: '/trade/payment', query: { orderId } });
				} else {
					throw new Error('订单创建失败，返回值异常');
				}
			} catch (error) {
				console.error('创建订单失败:', error);
				if (error.response) {
					console.error('服务器响应:', error.response);
					if (error.response.status === 500) {
						toast.error('服务器处理订单时出错，请稍后重试！');
					} else {
						toast.error(`创建订单失败: ${error.response.data.message || '未知错误'}`);
					}
				} else if (error.request) {
					toast.error('网络连接失败，请检查网络后重试！');
				} else {
					toast.error('创建订单失败，请稍后重试！');
				}
			}
		};

		return {
			businessId,
			business,
			user,
			cartArr,
			deliveryaddress,
			totalPrice,
			sexFilter,
			toUserAddress,
			toPayment,
			deliver,
			loading
		};
	},
	components: {
		Footer
	}
};
</script>

<style scoped>
.wrapper {
	width: 100%;
	height: 100%;
}

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

.wrapper .order-info {
	width: 100%;
	margin-top: 12vw;
	background-color: #0097EF;
	box-sizing: border-box;
	padding: 2vw;
	color: #fff;
}

.wrapper .order-info h5 {
	font-size: 3vw;
	font-weight: 300;
}

.wrapper .order-info .order-info-address {
	width: 100%;
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-weight: 700;
	user-select: none;
	cursor: pointer;
	margin: 1vw 0;
}

.wrapper .order-info .order-info-address p {
	width: 90%;
	font-size: 5vw;
}

.wrapper .order-info .order-info-address i {
	font-size: 6vw;
}

.wrapper .order-info p {
	font-size: 3vw;
}

.wrapper h3 {
	box-sizing: border-box;
	padding: 3vw;
	font-size: 4vw;
	color: #666;
	border-bottom: solid 1px #DDD;
}

.wrapper .order-detailed {
	width: 100%;
}

.wrapper .order-detailed li {
	width: 100%;
	height: 16vw;
	box-sizing: border-box;
	padding: 3vw;
	color: #666;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.wrapper .order-detailed li .order-detailed-left {
	display: flex;
	align-items: center;
}

.wrapper .order-detailed li .order-detailed-left img {
	width: 10vw;
	height: 10vw;
}

.wrapper .order-detailed li .order-detailed-left p {
	font-size: 3.5vw;
	margin-left: 3vw;
}

.wrapper .order-detailed li p {
	font-size: 3.5vw;
}

.wrapper .order-deliveryfee {
	width: 100%;
	height: 16vw;
	box-sizing: border-box;
	padding: 3vw;
	color: #666;
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 3.5vw;
}

.wrapper .total {
	width: 100%;
	height: 14vw;
	position: fixed;
	left: 0;
	bottom: 0;
	display: flex;
}

.wrapper .total .total-left {
	flex: 2;
	background-color: #505051;
	color: #fff;
	font-size: 4.5vw;
	font-weight: 700;
	user-select: none;
	display: flex;
	justify-content: center;
	align-items: center;
}

.wrapper .total .total-right {
	flex: 1;
	background-color: #38CA73;
	color: #fff;
	font-size: 4.5vw;
	font-weight: 700;
	user-select: none;
	cursor: pointer;
	display: flex;
	justify-content: center;
	align-items: center;
}

.loading {
	width: 100%;
	height: 100vh;
	display: flex;
	justify-content: center;
	align-items: center;
	font-size: 4vw;
	color: #666;
}
</style>