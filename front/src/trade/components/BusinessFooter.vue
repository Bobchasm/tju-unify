<!-- <template>
  <ul class="footer">
    <li @click="toBusinessView">
      <i class="fa fa-store"></i>
      <p>商铺</p>
    </li>
    <li @click="toBusinessOrders">
      <i class="fa fa-list"></i>
      <p>订单</p>
    </li>
    <li @click="toBusinessInfo">
      <i class="fa fa-user-o"></i>
      <p>商家</p>
    </li>
  </ul>
</template>

<script>
import { defineComponent, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

export default defineComponent({
  name: 'BusinessFooter',
  setup() {
    const router = useRouter();
    const route = useRoute();

    const checkBusinessLogin = () => {
      const businessUser = sessionStorage.getItem('businessUser') ? JSON.parse(sessionStorage.getItem('businessUser')) : null;
      if (!businessUser || !businessUser.isBusiness) {
        alert('登录已过期，请重新登录！');
        router.push('/trade/businessLogin');
        return false;
      }
      return businessUser;
    };

    const toBusinessView = () => {
      const businessUser = checkBusinessLogin();
      if (businessUser) {
        router.push({ 
          path: '/businessView',
          query: { businessId: businessUser.businessId }
        });
      }
    };

    const toBusinessInfo = () => {
      const businessUser = checkBusinessLogin();
      if (businessUser) {
        router.push({ 
          path: '/businessInformation',
          query: { businessId: businessUser.businessId }
        });
      }
    };

    const toBusinessOrders = () => {
      const businessUser = checkBusinessLogin();
      if (businessUser) {
        router.push({
          path: '/businessOrderManage',
          query: { businessId: businessUser.businessId }
        });
      }
    };

    return {
      toBusinessView,
      toBusinessInfo,
      toBusinessOrders
    };
  },
});
</script>

<style scoped>
.footer {
  width: 100%;
  height: 8vh;
  border-top: solid 1px #ddd;
  background-color: #fff;
  position: fixed;
  left: 0;
  bottom: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 1000;
}

.footer > li {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #999;
  user-select: none;
  cursor: pointer;
  flex: 1;
}

.footer > li p {
  font-size: 1.8vh;
}

.footer > li i {
  font-size: 3.5vh;
}

.footer > li:hover {
  color: #0097FF;
}
</style>  -->

<!-- 上面是原有的商家导航栏，估计带了登陆验证。勿动！ -->
<template>
  <ul class="footer">
    <li @click="toBusiness">
      <i class="fa fa-store"></i>
      <p>商铺</p>
    </li>
    <li @click="toOrders">
      <i class="fa fa-file-text-o"></i>
      <p>订单</p>
    </li>
    <li @click="toProfile">
      <i class="fa fa-user-o"></i>
      <p>我的</p>
    </li>
  </ul>
</template>

<script>
import { defineComponent } from 'vue';
import { useRouter } from 'vue-router';
export default defineComponent({
  name: 'Footer',
  setup() {
    const router = useRouter();

    // 修改跳转方法，指向商家页面的正确路由
    const toBusiness = () => {
      router.push({ path: '/merchant/business' });
    };
    
    const toOrders = () => {
      router.push({ path: '/merchant/orders'});
    };
    
    const toProfile = () => {
      router.push({ path: '/merchant/profile' });
    };

    return {
      toBusiness,
      toOrders,
      toProfile,
    };
  },
});
</script>

<style scoped>
/* 保持所有原有样式不变 */
.footer {
	width: 100%;
	height: 8vh; /* 使用视口高度单位，适应不同屏幕 */
	border-top: solid 1px #ddd;
	background-color: #fff;
  
	position: fixed;
	left: 0;
	bottom: 0;
  
	display: flex;
	justify-content: space-around;
	align-items: center;
	z-index: 1000;
  }
  
  .footer > li {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	color: #999;
	user-select: none;
	cursor: pointer;
	flex: 1; /* 使每个 li 平均分配宽度 */
  }
  
  .footer > li p {
	font-size: 1.8vh; /* 使用视口高度单位 */
  }
  
  .footer > li i {
	font-size: 3.5vh; /* 使用视口高度单位 */
  }
</style>