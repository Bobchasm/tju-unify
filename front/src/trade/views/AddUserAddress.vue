<template>
	<div class="wrapper">
		<!-- header部分 -->
		<BackButton style="margin-top: 2vw;"/>
    <div class="header">
      <p>新增送货地址</p>
    </div>

    <!-- 表单部分 -->
    <ul class="form-box">
      <li>
        <div class="title">联系人：</div>
        <div class="content">
          <input type="text" v-model="deliveryAddress.contactName" placeholder="联系人姓名">
        </div>
      </li>
      <li>
        <div class="title">性别：</div>
        <div class="content gender-select">
          <div class="gender-option">
            <input type="radio" v-model="deliveryAddress.contactSex" value="1" id="male">
            <label for="male">男</label>
          </div>
          <div class="gender-option">
            <input type="radio" v-model="deliveryAddress.contactSex" value="0" id="female">
            <label for="female">女</label>
          </div>
        </div>
      </li>
      <li>
        <div class="title">电话：</div>
        <div class="content">
          <input type="tel" v-model="deliveryAddress.contactTel" placeholder="电话">
        </div>
      </li>
      <li>
        <div class="title">收货地址：</div>
        <div class="content">
          <input type="text" v-model="deliveryAddress.address" placeholder="收货地址">
        </div>
      </li>
    </ul>

    <div class="button-add">
      <button @click="addUserAddress">保存</button>
    </div>

    <!-- 底部菜单部分 (如果需要，请自行添加) -->
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter,useRoute } from 'vue-router';
import { toast } from '@/trade/utils/toast';
import request from '@/trade/utils/tradeRequest';
import BackButton from '../components/BackButton.vue';
export default {
	name: 'AddUserAddress',
	components: {
		BackButton
	},
	setup() {
		const deliveryAddress = ref({
			contactName: '',
			contactSex: 1,
			contactTel: '',
			address: '',
			customer: {
				id: 0,
				username: ''
			}
		});

		const route = useRoute();
		const businessId = ref(route.query.businessId);
		const user = ref(null);
		const router = useRouter();
		const reg = /^1[3456789]\d{9}$/;
		const goBack = () => {
      router.back();
    };

		onMounted(() => {
			const userFromLocal = localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')) : null;
			const userFromSession = sessionStorage.getItem('userInfo') ? JSON.parse(sessionStorage.getItem('userInfo')) : null;
			user.value = userFromLocal || userFromSession;
			if (user.value) {
			deliveryAddress.value.customer.id = user.value.id; 
			deliveryAddress.value.customer.username = user.value.username; 
          }
			businessId.value = route.query.businessId;
		});
		
		const addUserAddress = () => {
			if (!(deliveryAddress.value.contactName.trim())) {
				toast.warning('联系人姓名不能为空！');
				return;
			}
			if (!reg.test(deliveryAddress.value.contactTel)) {
				toast.warning('请输入正确的手机号码！');
				return;
			}
			if (!(deliveryAddress.value.address.trim())) {
				toast.warning('联系人地址不能为空！');
				return;
			}
			
			request.post('/api/addresses', deliveryAddress.value)
				.then(response => {
					if (response.success) {
							toast.success('添加地址成功！');
							router.push({ path: '/trade/userAddress' , query: { businessId: businessId.value }});
						} else {
							toast.error('新增地址失败！原因：' + response.message);
						}
				})
				.catch(error => {
					console.error(error);
					toast.error('新增地址失败，请重试！');
				});
		};

		return {
			deliveryAddress,
			businessId,
			user,
			addUserAddress,
			goBack
		};
	}
}
</script>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css");

.wrapper {
  width: 100%;
  height: 100vh;
  background-color: #f7f7f7;
  display: flex;
  flex-direction: column;
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

.header .back-btn {
  position: absolute;
  left: 3vw;
  top: 50%;
  transform: translateY(-50%);
  color: #fff;
  font-size: 5.5vw;
}

.form-box {
  width: 100%;
  box-sizing: border-box;
  padding: 0 4vw;
  margin-top: 12vw;
  background-color: #fff;
}

.form-box li {
  padding: 4vw 0;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.form-box li:last-child {
  border-bottom: none;
}

.form-box li .title {
  flex: 0 0 20vw;
  font-size: 3.5vw;
  font-weight: 600;
  color: #333;
}

.form-box li .content {
  flex: 1;
  display: flex;
  align-items: center;
}

.form-box li .content input {
  border: 1px solid #ddd;
  border-radius: 8px;
  outline: none;
  width: 100%;
  height: 9vw;
  font-size: 3.5vw;
  padding: 0 4vw;
  box-sizing: border-box;
}

.form-box li .content input::placeholder {
  color: #ccc;
}

.form-box li .gender-select {
  justify-content: flex-start;
  gap: 6vw;
}

.form-box li .gender-select .gender-option {
  display: flex;
  align-items: center;
}

.form-box li .gender-select input[type="radio"] {
  appearance: auto;
  -webkit-appearance: auto;
  -moz-appearance: auto;
  width: 5vw;
  height: 5vw;
  margin-right: 2vw;
}

.form-box li .gender-select label {
  font-size: 3.5vw;
  color: #666;
  cursor: pointer;
}

.button-add {
  width: 100%;
  box-sizing: border-box;
  padding: 4vw 3vw 0 3vw;
}

.button-add button {
  width: 100%;
  height: 10vw;
  font-size: 3.8vw;
  font-weight: 700;
  color: #fff;
  background-color: #0097ff;
  border-radius: 4px;
  border: none;
  outline: none;
}
</style>
