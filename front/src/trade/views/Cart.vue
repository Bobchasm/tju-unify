<template>
	<div class="back-btn-container">
    <BackButton />
  </div>
	<div class="wrapper">
		<!-- header部分 -->
		<div class="fixed-top">
          <div class="top-background">
              <h1>购物车</h1>
          </div>
        </div>

		<!-- 购物车为空提示 -->
		<div class="empty-cart" v-if="cartItems.length === 0">
			<img src="/trade-assets/empty-cart.png" alt="购物车为空">
			<p>您的购物车空空如也</p>
			<button @click="goBack">返回商家</button>
		</div>

		<!-- 购物车列表部分 -->
		<div v-else>
			<!-- 商家信息 -->
			<div class="business-info">
				<h3>{{ businessName }}</h3>
			</div>

			<ul class="cart">
				<li v-for="item in cartItems" :key="item.id">
					<div class="cart-img">
						<!-- 这里假设您有食物图片的URL，如果没有可以移除或使用默认图片 -->
						<img :src="item.foodImg" alt="食物图片">
						<div class="cart-img-quantity" v-show="item.quantity > 0">{{ item.quantity }}</div>
					</div>
					<div class="cart-info">
						<h3 style="font-size: 20px;">{{ item.foodName }}</h3>
						<p style="font-size: 18px;">&#165;{{ item.foodPrice }} / 份
							<span style="font-size: 18px;color: rgb(192, 23, 23);"> * {{ item.quantity }}</span>
						</p>
					</div>
					<div class="cart-item-price">
						<h3>￥ {{ item.foodPrice * item.quantity }}</h3>
					</div>
				</li>
			</ul>

			<!-- 底部结算栏 -->
			<div class="checkout-bar">
				<div class="total-price">
					<p style="color: black;">总计:  <span style="color:crimson;">&#165; {{ totalPrice }}</span></p>
				</div>
				<button class="checkout-btn" @click="checkout">去下单</button>
			</div>
		</div>

		<!-- 底部菜单部分 -->
		<!-- <Footer /> -->
	</div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import BackButton from '../components/BackButton.vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/trade/utils/tradeRequest';
import { toast } from '@/trade/utils/toast';

export default {
	name: 'Cart',
	components: { BackButton },
	setup() {
		const cartItems = ref([]);
		const userInfo = ref(null);
		const route = useRoute();
		const router = useRouter();
		const businessId = ref(null);
		// const businessId = ref(null);
		const businessName = ref('');

		onMounted(() => {
			businessId.value = parseInt(route.query.businessId);
			userInfo.value = sessionStorage.getItem('userInfo') ? JSON.parse(sessionStorage.getItem('userInfo')) : null;

			if (userInfo.value) {
				listCart();
			} else {
				toast.error("用户未登录，请先登录！");
				router.push({ path: '/trade/login' });
			}
		});

		const listCart = () => {
			request.get("/api/carts/list?businessId=" + businessId.value)
				.then(response => {
					cartItems.value = response.data;
				}).catch(error => {
					console.error('获取购物车失败:', error);
			});
		};

		// 计算总价
		const totalPrice = computed(() => {
			return cartItems.value.reduce((total, item) => {
				return total + (item.foodPrice * item.quantity);
			}, 0);
		});

		// 结算
		const checkout = () => {
			// 跳转到结算页面
			router.push({
				path: '/trade/userAddress',
				query: {
					businessId: businessId.value,
				}
			});
		};

		// 返回商家页面
		const goBack = () => {
			router.go(-1);
		};

		return {
			cartItems,
			businessName,
			totalPrice,
			checkout,
			goBack,
			businessId
		};
	}
}
</script>

<style scoped>
/****************** 总容器 ******************/
.wrapper {
	width: 100%;
	height: 100%;
	position: relative;
	top: -4vw;
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

/****************** 商家信息 ******************/
.business-info {
	padding: 3vw;
	background-color: #f8f8f8;
	border-bottom: 1px solid #eee;
	margin-top: 23vw;
}

.business-info h3 {
	font-size: 4vw;
	color: #333;
}

/****************** 购物车列表部分 ******************/
.wrapper .cart {
	width: 100%;
	margin-bottom: 60px;
}

.wrapper .cart li {
	width: 100%;
	box-sizing: border-box;
	padding: 2.5vw;
	border-bottom: solid 1px #DDD;
	user-select: none;
	cursor: pointer;
	display: flex;
	align-items: center;
}

.wrapper .cart li .cart-img {
	/*这里设置为相当定位，成为cart-img-quantity元素的父元素*/
	position: relative;
}

.wrapper .cart li .cart-img img {
	width: 20vw;
	height: 20vw;
}

.wrapper .cart li .cart-img .cart-img-quantity {
	width: 5vw;
	height: 5vw;
	background-color: red;
	color: #fff;
	font-size: 3.6vw;
	border-radius: 2.5vw;
	display: flex;
	justify-content: center;
	align-items: center;
	/*设置成绝对定位，不占文档流空间*/
	position: absolute;
	right: -1.5vw;
	top: -1.5vw;
}

.wrapper .cart li .cart-info {
	margin-left: 3vw;
	flex: 1;
}

.wrapper .cart li .cart-info h3 {
	font-size: 3.8vw;
	color: #555;
}

.wrapper .cart li .cart-info p {
	font-size: 3vw;
	color: #888;
	margin-top: 2vw;
}

.wrapper .cart li .cart-actions {
	display: flex;
	align-items: center;
	gap: 2vw;
}

.wrapper .cart li .cart-actions button {
	width: 6vw;
	height: 6vw;
	border: none;
	border-radius: 50%;
	background-color: #0097FF;
	color: white;
	font-size: 3.5vw;
	display: flex;
	justify-content: center;
	align-items: center;
	cursor: pointer;
}

.wrapper .cart li .cart-actions .delete-btn {
	width: auto;
	padding: 0 2vw;
	border-radius: 1vw;
	font-size: 2.8vw;
	background-color: #ff4d4f;
	margin-left: 2vw;
}

.wrapper .cart li .cart-actions span {
	font-size: 3.5vw;
	color: #555;
}

/****************** 结算栏 ******************/
.checkout-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 20vw;
	background-color: #fff;
	border-top: 1px solid #ddd;
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 0 4vw;
	box-sizing: border-box;
}

.checkout-bar .total-price {
	font-size: 4.5vw;
	font-weight: bold;
	color: #ff4d4f;
}

.checkout-bar .checkout-btn {
	background-color: #0097FF;
	color: white;
	border: none;
	padding: 2.5vw 5vw;
	border-radius: 2vw;
	font-size: 4vw;
	cursor: pointer;
}

/****************** 空购物车 ******************/
.empty-cart {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 30vw;
}

.empty-cart img {
	width: 40vw;
	height: 40vw;
	margin-bottom: 5vw;
}

.empty-cart p {
	font-size: 4vw;
	color: #999;
	margin-bottom: 5vw;
}

.empty-cart button {
	background-color: #0097FF;
	color: white;
	border: none;
	padding: 3vw 6vw;
	border-radius: 2vw;
	font-size: 4vw;
	cursor: pointer;
}
.back-btn-container {
  position: fixed; /* 固定定位，不随滚动移动 */
  left: 0vw; /* 距离左侧的距离，可根据需求调整 */
  top: 1vw; /* 距离顶部的距离，与 header 高度（12vw）适配，确保垂直居中 */
  z-index: 1001; /* 比 header 的 z-index:1000 高，避免被遮挡 */
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