<template>
	<div class="wrapper">
		<BackButton :show-back-button="true" style="margin-top: -10vw;" />
		<!-- header部分 -->
		<div class="header">
			<p>商家列表</p>
		</div>

		<!-- 商家列表部分 -->
		<div class="business-list">
			<div class="business-item" v-for="business in businessArr" :key="business.id"
				@click="toBusinessInfo(business.id)">
				<div class="business-info">
					<img :src="business.businessImg || 'https://sunnybigevent.oss-cn-beijing.aliyuncs.com/bbd37656-0eae-41be-995e-e2be0b96aca2.png'"
						:alt="business.businessName" />
					<div class="business-details">
						<div class="business-header">
							<h3>{{ business.businessName || '未知商铺' }}</h3>
						</div>
						<p class="description">{{ business.businessExplain || '暂无描述' }}</p>
						<p class="description">{{ business.businessAddress || '暂无地址信息' }}</p>
						<div class="business-info-bottom">
							<div class="price-info">
								<span>起送 ¥{{ business.startPrice.toFixed(2) || 0 }}元</span>
								<span>配送 ¥{{ business.deliveryPrice.toFixed(2) || 0 }}元</span>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>
  
<script>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import request from "@/trade/utils/tradeRequest";
import BackButton from "@/trade/components/BackButton.vue";
export default {
	name: "BusinessList",
	components: { BackButton },
	setup() {
		const businessArr = ref([]);
		const route = useRoute();
		const router = useRouter();

		onMounted(async () => {
			const orderTypeId = route.query.orderTypeId || 1; // 默认为类型1

			try {
				// 获取商家列表
				const response = await request.get(
					"http://localhost:8086/api/businesses/type",
					{
						params: {
							type: orderTypeId
						}
					}
				);

				if (response.success) {
					businessArr.value = response.data;
				} else {
					console.error("获取商家列表失败:", response.message);
				}
			} catch (error) {
				console.error("请求商家列表出错:", error);
			}
		});

		const toBusinessInfo = (businessId) => {
			router.push({
				path: '/trade/businessInfo',
				query: { businessId }
			});
		};

		return {
			businessArr,
			toBusinessInfo,
		};
	},
};
</script>
  
<style scoped>
/* 保持原有的样式不变 */
.wrapper {
	width: 100%;
	height: 100%;
	position: relative;
	padding-top: 0;
	background-color: #f5f5f5;
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

.wrapper title {
	margin: 0;
	font-size: 24px;
	font-weight: 600;
	color: white;
}

.wrapper .business-list {
	padding: 2vw;
	margin-top: 12vw;
	margin-bottom: 15vw;
}

.wrapper .business-item {
	background: #fff;
	border-radius: 3vw;
	margin-bottom: 3vw;
	padding: 3vw;
	box-shadow: 0 0.5vw 2vw rgba(0, 0, 0, 0.05);
	transition: all 0.3s ease;
}

.wrapper .business-item:active {
	transform: scale(0.98);
}

.wrapper .business-info {
	display: flex;
	gap: 3vw;
	margin-bottom: 2vw;
	cursor: pointer;
}

.wrapper .business-info img {
	width: 20vw;
	height: 20vw;
	border-radius: 2vw;
	object-fit: cover;
	background-color: #eee;
	/* 图片加载前的背景色 */
}

.wrapper .business-details {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 2vw;
}

.wrapper .business-header {
	display: flex;
	flex-direction: column;
	gap: 1vw;
}

.wrapper .business-details h3 {
	font-size: 4.2vw;
	margin: 0;
	color: #333;
	font-weight: 600;
}

.wrapper .description {
	font-size: 3.2vw;
	color: #666;
	line-height: 1.4;
	margin: 0;
}

.wrapper .business-info-bottom {
	margin-top: auto;
}

.wrapper .price-info {
	display: flex;
	flex-wrap: wrap;
	gap: 2vw 3vw;
	font-size: 3vw;
	color: #666;
}

.wrapper .business-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 2vw;
	margin-top: 2vw;
}

.wrapper .tag {
	font-size: 2.8vw;
	padding: 0.5vw 2vw;
	border-radius: 3vw;
	background: #f0f7ff;
	color: #0097ff;
}
</style>