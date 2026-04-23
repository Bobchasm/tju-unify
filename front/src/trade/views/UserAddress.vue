<template>
	<div class="wrapper">
		<!-- header部分 -->
		<BackButton style="margin-top: -12vw;"/>
    <div class="header">

      <p>订单配送地址</p>
    </div>
		<!-- 地址列表部分 -->
		<ul class="addresslist">
			<li v-for="item in deliveryAddressArr" :key="item.id">
				<div class="addresslist-left" @click="setDeliveryAddress(item)">
					<h3 style="color: black;">{{ item.contactName }}{{ sexFilter(item.contactSex) }} {{ item.contactTel }}</h3>
					<p>{{ item.address }}</p>
				</div>
				<div class="addresslist-right">
					<i class="fa fa-edit" @click="editUserAddress(item.id)"></i>
					<i class="fa fa-remove" @click="removeUserAddress(item.id)"></i>
					<button class="select-btn" :class="{ 'selected': addressSelectedId === item.id }"
					@click="toggleUserAddress(item.id)">
						{{ addressSelectedId === item.id ? '已选' : '使用' }}
					</button>
				</div>
			</li>
		</ul>

		<!-- 新增地址部分 -->
		<div class="addbtn" @click="toAddUserAddress">
			<i class="fa fa-plus-circle"></i>
			<p>新增收货地址</p>
		</div>
		<!-- 确认对话框 - 完全保留原始样式 -->
		<div v-if="showConfirmModal" class="modal-overlay" @click.self="showConfirmModal = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3>确认操作</h3>
            <span class="close-btn" @click="showConfirmModal = false">&times;</span>
          </div>
          <div class="modal-body">
            <p>确定要删除地址吗？</p>
          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel-btn" @click="showConfirmModal = false">取消</button>
            <button class="modal-btn confirm-btn" @click="confirmRemove">确认</button>
          </div>
        </div>
      </div>
		<!-- 底部结算栏 -->
		<div class="order-bar">
			<button class="checkout-order-btn" @click="submitOrder">确认下单</button>
		</div>

		<!-- 确认删除弹窗 -->
		<div v-if="showConfirmModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-content">
          <div class="modal-header">
            <h3>确认操作</h3>
            <span class="close-btn" @click="closeModal">&times;</span>
          </div>
          <div class="modal-body">
            <p>确认要删除此送货地址吗？</p>
          </div>
          <div class="modal-footer">
			<button class="modal-btn confirm-btn" @click="confirmDelete">确认</button>
            <button class="modal-btn cancel-btn" @click="closeModal">取消</button>
          </div>
        </div>
      </div>

	</div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import Footer from '../components/Footer.vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/trade/utils/tradeRequest';
import BackButton from '@/trade/components/BackButton.vue';
import { toast } from '@/trade/utils/toast';
export default {
	name: 'UserAddress',
	components: {
		BackButton
		},
	setup() {

		const user = reactive({});
		const deliveryAddressArr = ref([]);
		const route = useRoute();
		const router = useRouter();
		const businessId = ref(route.query.businessId);
		const orderId = ref();
		const addressSelectedId = ref(0);
		const showConfirmModal = ref(false);
		const addressDeleteSelectId = ref(0);

		const goBack = () => {
      router.back();
    };
		onMounted(() => {
			const userFromLocal = localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')) : null;
			const userFromSession = sessionStorage.getItem('userInfo') ? JSON.parse(sessionStorage.getItem('userInfo')) : null;
			user.value = userFromLocal || userFromSession;
			businessId.value = route.query.businessId;
			listDeliveryAddressByUserId();
		});

		const listDeliveryAddressByUserId = () => {
			request.get('/api/addresses/listDeliveryAddressByUserId', {
				params: { userId: user.value.id }
			}).then(response => {
				deliveryAddressArr.value = response.data;
			}).catch(error => {
				console.error('获取送货地址列表失败:', error);
			});
		};

		const setDeliveryAddress = (deliveryAddress) => {
			localStorage.setItem(user.value.id, JSON.stringify(deliveryAddress));
			router.push({ path: '/trade/orders', query: { businessId: businessId.value } });
		};

		const toAddUserAddress = () => {
			router.push({ path: '/trade/addUserAddress', query: { businessId: businessId.value } });
		};

		const toggleUserAddress = (id) => {
			if (addressSelectedId.value === id) {
				addressSelectedId.value = 0;
			} else {
				addressSelectedId.value = id;
			}
		};

		const submitOrder = () => {
			if (addressSelectedId.value === 0) {
				toast.error("请选择配送地址");
				return;
			}
			else {
				console.log('📦 开始提交订单...');
				console.log('- businessId:', businessId.value);
				console.log('- addressId:', addressSelectedId.value);
				
				request.get("/api/orders/submit?businessId=" + businessId.value + "&addressId=" + addressSelectedId.value)
				.then(response => {
					console.log('📦 接收到订单响应:', response);
					console.log('- response类型:', typeof response);
					console.log('- response.success:', response.success);
					console.log('- response.data:', response.data);
					console.log('- response.data类型:', typeof response.data);
					
					if (response.success) {
						orderId.value = response.data;
						console.log('✅ 订单提交成功, orderId:', orderId.value);
						console.log('🛫 准备跳转到支付页面...');
						router.push({ path: '/trade/payment', query: { businessId: businessId.value, orderId: response.data } });
					} else {
						console.error('❌ 下单失败:', response.message || response);
						toast.error("下单失败，请重试");
						router.push({path: '/trade/orderList'})
					}
				}).catch(error => {
					console.error('❌ 下单请求失败:', error);
					console.error('- error.response:', error.response);
					console.error('- error.message:', error.message);
					toast.error('下单失败: ' + (error.response?.data?.message || error.message));
			});
			}
		};

		const editUserAddress = (id) => {
			router.push({ path: '/trade/editUserAddress', query: { businessId: businessId.value, id } });
		};

		const removeUserAddress = (id) => {
			addressDeleteSelectId.value = id;
			showConfirmModal.value = true;
		};

		const closeModal = () => {
			showConfirmModal.value = false;
			addressDeleteSelectId.value = 0;
		};

		const confirmDelete = () => {
			if (addressDeleteSelectId.value === 0) return;

			request.put('/api/addresses/removeDeliveryAddress', {
				id: addressDeleteSelectId.value
			}).then(response => {
				console.log(response.data);
				if (response.success) {
					let deliveryAddress = JSON.parse(localStorage.getItem(user.value.id.toString()));
					if (deliveryAddress && deliveryAddress.id === addressDeleteSelectId.value) {
						localStorage.removeItem(user.value.id.toString());
					}
					toast.success("删除地址成功");
					listDeliveryAddressByUserId();
				} else {
					toast.error("删除地址失败！");
				}
			}).catch(error => {
				console.error(error);
				toast.error("删除地址失败！");
			}).finally(() => {
				closeModal(); // 无论成功失败都关闭弹窗
			});
		};

		const sexFilter = (value) => value === 1 ? '先生' : '女士';

		return {
			businessId,
			user,
			deliveryAddressArr,
			listDeliveryAddressByUserId,
			setDeliveryAddress,
			toAddUserAddress,
			editUserAddress,
			removeUserAddress,
			toggleUserAddress, // 修改：使用切换函数
			sexFilter,
			orderId,
			addressSelectedId,
			submitOrder,
			showConfirmModal,
			closeModal,
			confirmDelete,
			goBack
		};
	},
}
</script>

<style scoped>
.wrapper {
	width: 100%;
	height: 100%;
}

.wrapper .header {
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
.wapper title {
 font-size: 1.1rem;
  color: #ffffff;
  font-weight: 600;
  margin: 0;
}
.back-icon {
  position: absolute;
  left: 15px; /* 调整左边距以更好地对齐 */
  font-size: 1.2rem;
  color: #ffffff;
  cursor: pointer;
  padding: 5px;
}

.wrapper .addresslist {
	width: 100%;
	margin-top: 14vw;
	background-color: #fff;
}

.wrapper .addresslist li {
	width: 100%;
	box-sizing: border-box;
	border-bottom: solid 1px #DDD;
	padding: 3vw;
	display: flex;
}

.wrapper .addresslist li .addresslist-left {
	flex: 2.5;
	user-select: none;
	cursor: pointer;
}

.wrapper .addresslist li .addresslist-left h3 {
	font-size: 4.6vw;
	font-weight: 300;
	color: #666;
}

.wrapper .addresslist li .addresslist-left p {
	font-size: 4vw;
	color: #666;
}

.wrapper .addresslist li .addresslist-right {
	flex: 1;
	font-size: 5.6vw;
	color: #999;
	cursor: pointer;
	display: flex;
	justify-content: space-around;
	align-items: center;
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

.wrapper .addbtn {
	width: 100%;
	height: 14vw;
	border-top: solid 1px #DDD;
	border-bottom: solid 1px #DDD;
	background-color: #fff;
	margin-top: 4vw;
	display: flex;
	justify-content: center;
	align-items: center;
	font-size: 4.5vw;
	color: #0097FF;
	user-select: none;
	cursor: pointer;
}

.wrapper .addbtn p {
	margin-left: 2vw;
}


.wrapper .addresslist .addresslist-right .select-btn {
	background-color: #0097ef;
	color: #fff;
	border: none;
	padding: 2vw 3.5vw;
	border-radius: 5px;
	font-size: 3vw;
	cursor: pointer;
	margin-left: 2vw;
	transition: all 0.3s;
}

.wrapper .addresslist .addresslist-right .select-btn.selected {
    background-color: #0081e6;
    color: #fcfafa;
	border: none;
	padding: 2vw 3.5vw;
	border-radius: 5px;
	font-size: 3vw;
	cursor: pointer;
	margin-left: 2vw;
}

.wrapper .addresslist .addresslist-right .select-btn:not(.selected):hover {
	background-color: #0081e6;
}

.wrapper .addresslist .addresslist-right .select-btn:disabled {
	cursor: not-allowed;
	opacity: 0.8;
}


.wrapper .order-bar {
  position: fixed;
  left: 0;   /* 左右设为0，让容器撑满宽度 */
  right: 0;
  bottom: 0; /* 固定在底部 */
  z-index: 1000;
  background-color: #fff; /* 加背景色，与页面区分 */
  padding: 3vw; /* 内边距，让按钮不贴边 */
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1); /* 底部阴影，增强层次感 */
}

.wrapper .order-bar .checkout-order-btn {
	background-color: #0097FF;
	color: #fff;
	border: none;
	padding: 3vw 10vw;
	border-radius: 8vw;
	font-size: 4.5vw;
	font-weight: bold;
	cursor: pointer;
	box-shadow: 0 2vw 4vw rgba(0, 151, 255, 0.3);
	transition: all 0.3s ease;
	min-width: 25vw;
	text-align: center;
	margin-left: 220px;
}

.wrapper .order-bar .checkout-order-btn:hover {
  background-color: #0081e6; /*  hover 时加深蓝色 */
  transform: translateY(-1px); /* 轻微上浮，增强交互感 */
  box-shadow: 0 4px 12px rgba(0, 151, 255, 0.3);
}

.wrapper .order-bar .checkout-order-btn:active {
  transform: translateY(1px); /* 点击时轻微下沉 */
  box-shadow: 0 2px 6px rgba(0, 151, 255, 0.2);
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

</style>