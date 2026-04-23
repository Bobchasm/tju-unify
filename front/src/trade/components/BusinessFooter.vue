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