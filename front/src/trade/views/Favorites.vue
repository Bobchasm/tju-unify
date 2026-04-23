<template>

    <BackButton style="margin-top: 2vw;"/>
    <div class="fixed-top">
          <div class="top-background">
              <h1>我的收藏</h1>
          </div>
        </div>
    <div class="container">
      <div v-if="loading" class="loading-message">
        <i class="fas fa-spinner fa-spin"></i> 加载中...
      </div>

      <div v-else-if="errorMessage" class="error-message">
        <i class="fas fa-exclamation-circle"></i> {{ errorMessage }}
      </div>

      <div v-else-if="favoriteList.length === 0" class="empty-message">
        <i class="fas fa-heart-broken"></i>
        <p>还没有收藏的店铺，快去逛逛吧！</p>
      </div>

      <div v-else class="business-list">
        <div v-for="business in favoriteList" :key="business.businessId" class="business-item"
          @click="goToBusinessInfo(business.businessId)">
          <img :src="business.businessImg || '/trade-assets/default-business.png'"
     :alt="business.businessName"
     class="business-img"
     @error="handleImageError"><div class="business-details">
            <div class="business-name">{{ business.businessName }}</div>
            <div class="business-info-row">
              <span class="star-rating">
                <i class="fas fa-star"></i> {{ business.starRating }}
              </span>
              <span class="sales-volume">月售{{ business.salesVolume }}单</span>
            </div>
            <div class="business-delivery-row">
              <span class="delivery-fee">配送费￥{{ business.deliveryFee.toFixed(2) }}</span>
              <span class="start-price">起送费￥{{ business.startPrice.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { toast } from '@/trade/utils/toast';
import request from '@/trade/utils/tradeRequest';
import BackButton from '@/trade/components/BackButton.vue';
export default {
  name: 'Favorites',
  components: { BackButton },
  setup() {
    const router = useRouter();
    const favoriteList = ref([]);
    const loading = ref(false);
    const errorMessage = ref('');

    const fetchFavorites = async () => {
      loading.value = true;
      errorMessage.value = '';
      try {
        const userInfo = JSON.parse(sessionStorage.getItem('userInfo'));
        if (!userInfo || !userInfo.id) {
          toast.warning('请先登录以查看收藏列表！');
          router.push('/trade/login');
          return;
        }

        const response = await request.get(`/api/merchant/interaction/collections/${userInfo.id}`);

        favoriteList.value = response.data.map(business => ({
          businessId: business.id,
          businessName: business.businessName,
          businessImg: business.businessImg,
          starRating: Number(business.score) || 0,
          salesVolume: business.salesCount || 0,
          deliveryFee: Number(business.deliveryPrice) || 0,
          startPrice: Number(business.startPrice) || 0
        }));

      } catch (error) {
        console.error('获取收藏列表失败:', error);
        errorMessage.value = '获取收藏列表失败，请稍后重试。';
        toast.error(errorMessage.value);
      } finally {
        loading.value = false;
      }
    };

    const goToBusinessInfo = (businessId) => {
      router.push({
        path: '/trade/businessInfo',
        query: { businessId: businessId }
      });
    };

    const goBack = () => {
      router.back();
    };

    onMounted(() => {
      fetchFavorites();
    });

    return {
      favoriteList,
      loading,
      errorMessage,
      goToBusinessInfo,
      goBack
    };
  }
};
</script>

<style scoped>
body {
  margin: 0;
  padding: 0;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 50px;
  background-color: #0097ff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.container {
  width:100%;
  padding-bottom: 40px;
  background-color: #f0f2f5;
  min-height: 100vh;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  margin-top: calc(-13vw);
}

.header h1 {
  font-size: 1.1rem;
  color: #ffffff;
  font-weight: 600;
  color:white;
}

.back-icon {
  position: absolute;
  left: 15px; /* 调整左边距以更好地对齐 */
  font-size: 1.2rem;
  color: #ffffff;
  cursor: pointer;
  padding: 5px;
}

.loading-message,
.error-message,
.empty-message {
  text-align: center;
  padding: 50px 20px;
  color: #777;
  font-size: 1rem;
}

.error-message {
  color: #e74c3c;
}

.empty-message p {
  margin-top: 10px;
  font-size: 0.9rem;
  color: #999;
}

.empty-message .fas {
  font-size: 2.5rem;
  color: #ccc;
  margin-bottom: 10px;
}

.loading-message .fas {
  color: #3498db;
  margin-right: 8px;
}

.business-list {
  padding: 0px 15px; /* 增加左右内边距，使列表内容与页面两边有一定间隔 */
  margin-top: 25px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.business-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.business-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.business-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  margin-right: 15px;
  border: 1px solid #eee;
}

.business-details {
  flex: 1;
}

.business-name {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.business-info-row,
.business-delivery-row {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
  color: #777;
}

.business-info-row {
  margin-bottom: 5px;
}

.star-rating i {
  color: #f39c12;
  margin-right: 5px;
}

.sales-volume {
  margin-left: 15px;
}

.delivery-fee {
  margin-right: 15px;
}

.start-price {
  color: #ff5722;
  font-weight: 500;
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