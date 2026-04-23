<template>
	<div class="wrapper">
	  <!-- header部分 -->
	  <header>
		<div class="back-btn-container">
		  <button @click="goBack" class="back-btn">←</button>
		</div>
		<p>编辑送货地址</p>
	  </header>
  
	  <!-- 表单部分 -->
	  <ul class="form-box">
		<li>
		  <div class="title">
			联系人：
		  </div>
		  <div class="content">
			<input type="text" v-model="deliveryAddress.contactName" placeholder="请输入联系人姓名">
		  </div>
		</li>
		<li>
		  <div class="title">
			性别：
		  </div>
		  <div class="content gender-selection">
			<label class="radio-option">
			  <input type="radio" v-model="deliveryAddress.contactSex" value="1">
			  <span class="radio-custom"></span>
			  男
			</label>
			<label class="radio-option">
			  <input type="radio" v-model="deliveryAddress.contactSex" value="0">
			  <span class="radio-custom"></span>
			  女
			</label>
		  </div>
		</li>
		<li>
		  <div class="title">
			电话：
		  </div>
		  <div class="content">
			<input type="tel" v-model="deliveryAddress.contactTel" placeholder="请输入手机号码">
		  </div>
		</li>
		<li>
		  <div class="title">
			收货地址：
		  </div>
		  <div class="content">
			<input type="text" v-model="deliveryAddress.address" placeholder="请输入详细收货地址">
		  </div>
		</li>
	  </ul>
  
	  <div class="button-add">
		<button @click="editUserAddress" class="update-btn">更新地址</button>
	  </div>
	</div>
  </template>
  
  <script>
  import { ref, onMounted } from 'vue';
  import Footer from '../components/Footer.vue';
  import request from '@/trade/utils/tradeRequest';
  import { useRoute,useRouter } from 'vue-router';
  import { toast } from '@/trade/utils/toast';

  export default {
	name: 'EditUserAddress',
	components: {
	  Footer
	},
	setup() {
	  const businessId = ref(null);
	  const id = ref(null);
	  const user = ref({});
	  const deliveryAddress = ref({
		contactSex: '1' // 默认选择男性
	  });
	  const route = useRoute();
	  const router = useRouter();
	  const reg = /^1[3456789]\d{9}$/;
	  
	  onMounted(async () => {
			businessId.value = route.query.businessId;
			id.value = route.query.id;
			const userFromLocal = localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')) : null;
			const userFromSession = sessionStorage.getItem('userInfo') ? JSON.parse(sessionStorage.getItem('userInfo')) : null;
			user.value = userFromLocal || userFromSession;

			try {
				const response = await request.get('/api/addresses/getDeliveryAddressById', {
					params:{id: id.value}
				});
				deliveryAddress.value = response.data;
			} catch (error) {
				console.error('获取配送地址失败:', error);
			}
		});
		
		const goBack = () => {
			router.back();
		};
  
	  const editUserAddress = () => {
		if (!(deliveryAddress.value.contactName?.trim())) {
				toast.error("联系人姓名不能为空！");
				return;
			}
			if (!reg.test(deliveryAddress.value.contactTel)) {
				toast.error("请输入正确的手机号码！");
				return;
			}
			if (!(deliveryAddress.value.address?.trim())) {
				toast.error("收货地址不能为空！");
				return;
			}
  
		request.post('/api/addresses/updateDeliveryAddress',deliveryAddress.value)
		  .then(response => {
			if (response.success) {
			  toast.success("地址更新成功！");
			  router.push({
				path: '/trade/userAddress',
				query: {
				  businessId: businessId.value
				}
			  });
			} else {
			  toast.error("更新地址失败！");
			}
		  }).catch(error => {
			console.error(error);
			toast.error("网络错误，请稍后重试");
		  });
	  };
  
	  return {
		deliveryAddress,
		editUserAddress,
		goBack
	  };
	}
  }
  </script>
  
  <style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Arial', sans-serif;
  background-color: #f0f2f5;
}

.wrapper {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f0f2f5;
}

header {
  width: 100%;
  height: 15vw;
  max-height: 80px;
  background-color: #0097FF;
  color: #fff;
  font-size: clamp(20px, 5vw, 24px);
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.back-btn-container {
  position: fixed;
  left: 4vw;
  top: 2vw;
  z-index: 1001;
}

.back-btn {
  width: 10vw;
  height: 10vw;
  max-width: 40px;
  max-height: 40px;
  background: transparent;
  border: none;
  color: white;
  font-size: 6vw;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.form-box {
  width: 90%;
  max-width: 400px;
  background: #fff;
  margin-top: 25vw;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  list-style: none;
}

.form-box li {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.form-box li .title {
  font-size: 16px;
  font-weight: 600;
  color: #444;
  flex-basis: 90px;
  flex-shrink: 0;
}

.form-box li .content {
  flex: 1;
}

.form-box li .content input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  color: #333;
  transition: border-color 0.3s;
}

.form-box li .content input:focus {
  outline: none;
  border-color: #0097FF;
}

.gender-selection {
  display: flex;
  gap: 20px;
}

.radio-option {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  position: relative;
}

.radio-option input[type="radio"] {
  opacity: 0;
  position: absolute;
  width: 0;
  height: 0;
}

.radio-custom {
  width: 16px;
  height: 16px;
  border: 2px solid #ccc;
  border-radius: 50%;
  margin-right: 6px;
  position: relative;
  transition: all 0.3s;
  accent-color: #0097FF;
}

.radio-option input[type="radio"]:checked + .radio-custom {
  border-color: #0097FF;
  background-color: #0097FF;
}

.radio-option input[type="radio"]:checked + .radio-custom::after {
  content: '';
  width: 6px;
  height: 6px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.button-add {
  width: 90%;
  max-width: 400px;
  margin-top: 20px;
}

.button-add .update-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  background-color: #0097FF;
  border-radius: 8px;
  border: none;
  outline: none;
  cursor: pointer;
  transition: background-color 0.3s, transform 0.1s, box-shadow 0.3s;
  box-shadow: 0 4px 12px rgba(0, 151, 255, 0.3);
}

.button-add .update-btn:hover {
  background-color: #007acc;
}

.button-add .update-btn:active {
  transform: translateY(1px);
}

@media (min-width: 768px) {
  header {
    height: 80px;
  }
  
  .form-box {
    margin-top: 120px;
  }
  
  .back-btn-container {
    left: 20px;
    top: 10px;
  }
  
  .back-btn {
    width: 40px;
    height: 40px;
    font-size: 24px;
  }
}
</style>