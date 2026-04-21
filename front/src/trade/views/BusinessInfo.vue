<template>
    <div class="wrapper">
        <!-- <BackButton :show-back-button="true" style="margin-top: -10vw;"/> -->
        <!-- header部分 -->
        <div class="fixed-top">
          <div class="top-background">
              <h1>商家信息</h1>
          </div>
        </div>

        <!-- 商家logo部分 -->
        <div class="business-logo">
            <img :src="business.businessImg || '/trade-assets/business-default.png'" />
        </div>

        <!-- 商家信息部分 -->
        <div class="business-info">
            <h1>{{ business.businessName || ''}}</h1>
            <p>
                &#165;{{ business.startPrice.toFixed(2) || 0}}起送 &#165;{{
                    business.deliveryPrice.toFixed(2) || 0
                }}配送
            </p>
            <p class="explain-info">{{ business.businessExplain || ''}}</p>
            <div class="reactions">
                <div class="reaction" @click.stop="toggleLike"
                    :class="{ 'active': isLiked, 'disabled': interactionLoading }" :title="isLiked ? '已点赞' : '点赞'">
                    <i class="fa fa-thumbs-up"
                        :style="isLiked ? 'color:#e74c3c' : interactionLoading ? 'color:#ddd' : 'color:#bbb'"></i>
                    <span v-if="interactionLoading" class="loading-dots">...</span>
                </div>
                <div class="reaction" @click.stop="toggleFavorite"
                    :class="{ 'active': isFavorited, 'disabled': interactionLoading }"
                    :title="isFavorited ? '已收藏' : '收藏'">
                    <i class="fa fa-star"
                        :style="isFavorited ? 'color:#e74c3c' : interactionLoading ? 'color:#ddd' : 'color:#bbb'"></i>
                    <span v-if="interactionLoading" class="loading-dots">...</span>
                </div>
            </div>
        </div>

        <!-- 食品列表部分 -->
        <ul class="food">
            <li v-for="(item, index) in foodArr" :key="item.foodId">
                <div class="food-left">
                    <img :src="item.foodImg || '/trade-assets/food-default.png'" />
                    <div class="food-left-info">
                        <h3>{{ item.foodName || ''}}</h3>
                        <p>{{ item.foodExplain || ''}}</p>
                        <p>&#165;{{ item.foodPrice.toFixed(2) || 0}}</p>
                    </div>
                </div>
                <div class="food-right">
                    <div>
                        <i class="fa fa-minus-circle" @click="minus(index)" v-show="getCartQuantity(item.id) > 0"></i>
                    </div>
                    <p>
                        <span v-show="getCartQuantity(item.id) > 0">{{ getCartQuantity(item.id) || 0}}</span>
                    </p>
                    <div>
                        <i class="fa fa-plus-circle" @click="add(index)"></i>
                    </div>
                </div>
            </li>
        </ul>

        <!-- 购物车部分 -->
        <div class="cart">
            <div class="cart-left">
                <div class="cart-left-icon" :style="totalQuantity == 0
                    ? 'background-color:#505051;'
                    : 'background-color:#3190E8;'
                    " @click="goToCart()">
                    <i class="fa fa-shopping-cart"></i>
                    <div class="cart-left-icon-quantity" v-show="totalQuantity != 0">
                        {{ totalQuantity || 0}}
                    </div>
                </div>
                <div class="cart-left-info">
                    <p>&#165;{{ totalPrice.toFixed(2) || 0}}</p>
                    <p>另需配送费{{ business.deliveryPrice || 0}}元</p>
                </div>
            </div>
            <div class="cart-right">
                <!-- 不够起送费 -->
                <div class="cart-right-item" v-show="!canOrder" style="background-color: #535356; cursor: default">
                    &#165;{{ business.startPrice || 0}}起送
                </div>
                <!-- 达到起送费但商品总价小于配送费 -->
                <div class="cart-right-item" v-show="canOrder && totalPrice < business.deliveryPrice"
                    style="background-color: #969696; cursor: not-allowed">
                    商品总价不足
                </div>
                <!-- 达到起送费且商品总价大于等于配送费 -->
                <div class="cart-right-item" v-show="canOrder && totalPrice >= business.deliveryPrice" @click="toOrder"
                    style="background-color: #38ca73; cursor: pointer">
                    去结算
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, onMounted, computed, watch, onErrorCaptured } from "vue";
import { useRoute, useRouter } from "vue-router";
import request from "@/trade/utils/tradeRequest";
import BackButton from "@/trade/components/BackButton.vue";
import { toast } from '@/trade/utils/toast';
export default {
    name: "BusinessInfo",
    components: { BackButton },
    setup() {
        const route = useRoute();
        const router = useRouter();
        const userInfo = ref(null);
        console.log("BusinessInfo组件初始化，路由参数:", route.query);

        // 基础数据
        const businessId = ref(null);
        const business = ref({
            id: 0,
            businessName: "",
            businessImg: "",
            startPrice: 0,
            deliveryPrice: 0,
            businessExplain: "",
            businessAddress: "",
            orderTypeId: 0,
            remarks: ""
        });
        const foodArr = ref([]);
        const cartItems = ref([]); // 购物车商品列表
        const loadingBusiness = ref(false);
        const loadingFoods = ref(false);
        const loadingCart = ref(false);

        // 用户交互状态
        const isLiked = ref(false);
        const isFavorited = ref(false);
        const interactionLoading = ref(false);

        const fetchUserInfo = async () => {
            const token = localStorage.getItem('token') || sessionStorage.getItem('token');
            if (!token) return;

            try {
                const res = await request.get('/api/user');
                if (res) {
                    userInfo.value = res;
                    console.log(userInfo.value);
                    // 保存用户信息到存储
                    const storage = localStorage.getItem('token') ? localStorage : sessionStorage;
                    storage.setItem('userInfo', JSON.stringify(res));
                } else {
                    console.error('获取用户信息失败');
                    userInfo.value = null;
                }
            } catch (error) {
                console.error('获取用户信息异常:', error);
                userInfo.value = null;
            }
        };

        // 错误捕获
        onErrorCaptured((error) => {
            console.error('组件错误捕获:', error);
            return false;
        });

        // 获取购物车列表
        const fetchCartList = async () => {
            if (!userInfo.value?.id || !businessId.value) {
                console.log('用户未登录或商家ID为空，不获取购物车');
                cartItems.value = [];
                return;
            }

            loadingCart.value = true;
            try {
                const response = await request.get("/api/carts/list", {
                    params: { businessId: businessId.value }
                });
                console.log("购物车列表API响应:", response);

                if (response.success) {
                    cartItems.value = response.data || [];
                    console.log("购物车列表设置成功:", cartItems.value);
                } else {
                    console.error('获取购物车列表失败:', response.message);
                    cartItems.value = [];
                }
            } catch (error) {
                console.error("获取购物车列表失败:", error);
                cartItems.value = [];
            } finally {
                loadingCart.value = false;
            }
        };

        // 获取指定食品在购物车中的数量
        const getCartQuantity = (foodId) => {
            const cartItem = cartItems.value.find(item => item.foodId === foodId);
            return cartItem ? cartItem.quantity : 0;
        };

        // 添加商品到购物车
        const addToCart = async (index) => {
            const food = foodArr.value[index];
            console.log(`添加商品到购物车: ${food.foodName}, foodId: ${food.id}`);

            // 检查用户是否登录
            if (!userInfo.value?.id) {
                toast.error('请先登录');
                return;
            }

            try {
                const currentQuantity = getCartQuantity(food.id);
                const newQuantity = currentQuantity + 1;

                console.log('当前数量:', currentQuantity, '新数量:', newQuantity);

                if (currentQuantity > 0) {
                    // 如果商品已在购物车中，更新数量
                    await updateCartItem(food.id, newQuantity);
                } else {
                    // 如果商品不在购物车中，添加新商品
                    await addNewCartItem(food.id);
                }

                // 重新获取购物车列表以更新显示
                await fetchCartList();
                console.log('添加商品成功');
            } catch (error) {
                console.error('添加商品到购物车失败:', error);
                toast.error('添加商品失败，请重试');
            }
        };

        // 添加新商品到购物车
        const addNewCartItem = async (foodId) => {
            try {
                console.log('添加新商品到购物车, foodId:', foodId);

                const response = await request.get("/api/carts/add", {
                    params: { foodId: foodId, quantity: 1 }
                });

                console.log('添加购物车接口响应:', response);

                if (response.success) {
                    console.log('添加新商品成功');
                    return true;
                } else {
                    throw new Error(response.message || '添加商品失败');
                }
            } catch (error) {
                console.error('添加新商品到购物车失败:', error);
                throw error;
            }
        };

        // 更新购物车商品数量
        const updateCartItem = async (foodId, newQuantity) => {
            try {
                console.log('更新购物车商品数量, foodId:', foodId, 'newQuantity:', newQuantity);

                // 先找到对应的购物车项ID
                const cartItem = cartItems.value.find(item => item.foodId === foodId);
                if (!cartItem) {
                    throw new Error(`未找到foodId ${foodId}对应的购物车项`);
                }

                const response = await request.get("/api/carts/quantity", {
                    params: {
                        cartId: cartItem.id,
                        quantity: newQuantity
                    }
                });

                if (response.success) {
                    console.log('更新商品数量成功');
                    return true;
                } else {
                    throw new Error(response.message || '更新数量失败');
                }
            } catch (error) {
                console.error('更新购物车商品数量失败:', error);
                throw error;
            }
        };

        // 从购物车移除商品（减少数量）
        const removeFromCart = async (index) => {
            const food = foodArr.value[index];
            console.log(`从购物车移除商品: ${food.foodName}, foodId: ${food.id}`);

            // 检查用户是否登录
            if (!userInfo.value?.id) {
                toast.error('请先登录');
                return;
            }

            try {
                const currentQuantity = getCartQuantity(food.id);
                if (currentQuantity <= 0) {
                    console.log('商品不在购物车中');
                    return;
                }

                const newQuantity = currentQuantity - 1;

                if (newQuantity <= 0) {
                    // 如果数量为0，从购物车中删除
                    await deleteCartItem(food.id);
                } else {
                    // 减少数量
                    await updateCartItem(food.id, newQuantity);
                }

                // 重新获取购物车列表以更新显示
                await fetchCartList();
                console.log('移除商品成功');
            } catch (error) {
                console.error('从购物车移除商品失败:', error);
                toast.error('移除商品失败，请重试');
            }
        };

        // 从购物车删除商品
        const deleteCartItem = async (foodId) => {
            try {
                console.log('从购物车删除商品, foodId:', foodId);

                const cartItem = cartItems.value.find(item => item.foodId === foodId);
                if (!cartItem) {
                    console.log('商品不在购物车中，无需删除');
                    return;
                }

                const response = await request.get("/api/carts/remove", {
                    params: { cartId: cartItem.id }
                });

                if (response.success) {
                    console.log('删除商品成功');
                    return true;
                } else {
                    throw new Error(response.message || '删除商品失败');
                }
            } catch (error) {
                console.error('从购物车删除商品失败:', error);
                throw error;
            }
        };

        // 加载用户互动状态
        const loadReactions = async () => {
            try {
                if (!businessId.value) {
                    console.error("缺少businessId");
                    return;
                }

                let retry = 0;
                while (!userInfo.value?.id && retry < 4) {
                    await new Promise(resolve => setTimeout(resolve, 500));
                    retry++;
                }

                const userId = userInfo.value?.id;
                if (!userId) {
                    console.log("用户未登录，不加载互动状态");
                    isLiked.value = false;
                    isFavorited.value = false;
                    return;
                }

                console.log(`加载互动状态，userId: ${userId}, merchantId: ${businessId.value}`);

                const response = await request.get('/api/merchant/interaction/status', {
                    params: { userId: userId, merchantId: businessId.value },
                });

                console.log("互动状态API响应:", response);

                // 根据实际API响应结构调整
                if (response?.success) {
                    isLiked.value = Boolean(response.data?.liked);
                    isFavorited.value = Boolean(response.data?.collected);
                    console.log(`设置互动状态 - 点赞: ${isLiked.value}, 收藏: ${isFavorited.value}`);
                } else {
                    console.error("API返回失败:", response?.message);
                    isLiked.value = false;
                    isFavorited.value = false;
                }
            } catch (error) {
                console.error("加载互动状态异常:", error);
                isLiked.value = false;
                isFavorited.value = false;
            }
        };
        // 更新互动状态到后端
        const updateInteraction = async (type, newValue) => {
            try {
                // 如果已经是目标状态，则不再执行
                if ((type === 'like' && isLiked.value === newValue) ||
                    (type === 'favorite' && isFavorited.value === newValue)) {
                    console.log(`已经是目标状态，无需更新: ${type}=${newValue}`);
                    return;
                }

                interactionLoading.value = true;
                const userId = userInfo.value?.id;
                if (!userId) {
                    toast.error('请先登录');
                    return;
                }

                const dto = {
                    userId,
                    merchantId: businessId.value,
                    liked: type === 'like' ? newValue : isLiked.value,
                    collected: type === 'favorite' ? newValue : isFavorited.value
                };

                console.log(`更新互动状态:`, dto);

                const response = await request.post('/api/merchant/interaction/update', dto);

                if (response.success) {
                    if (type === 'like') {
                        isLiked.value = newValue;
                    } else {
                        isFavorited.value = newValue;
                    }
                    console.log(`${type}状态更新成功: ${newValue}`);
                } else {
                    console.error(`${type}状态更新失败:`, response.message);
                    toast.error('操作失败，请重试');
                }
            } catch (error) {
                console.error(`${type}状态更新异常:`, error);
                toast.error('操作异常，请检查网络');
            } finally {
                interactionLoading.value = false;
            }
        };


        // 修改切换函数，增加状态检查
        const toggleLike = async () => {
            if (interactionLoading.value) return;
            if (!userInfo.value?.id) {
                toast.error('请先登录');
                return;
            }
            await updateInteraction('like', !isLiked.value);
        };

        const toggleFavorite = async () => {
            if (interactionLoading.value) return;
            if (!userInfo.value?.id) {
                toast.error('请先登录');
                return;
            }
            await updateInteraction('favorite', !isFavorited.value);
        };



        // 获取商家信息
        const fetchBusinessInfo = async () => {
            loadingBusiness.value = true;
            console.log(`开始获取商家信息，businessId: ${businessId.value}`);

            try {
                const response = await request.get(`/api/businesses/${businessId.value}`);
                console.log("商家信息API完整响应:", response);

                if (response.success === true) {
                    console.log("API请求成功，开始处理数据");
                    business.value = {
                        id: response.data.id,
                        businessName: response.data.businessName,
                        businessImg: response.data.businessImg,
                        startPrice: response.data.startPrice,
                        deliveryPrice: response.data.deliveryPrice,
                        businessExplain: response.data.businessExplain,
                        businessAddress: response.data.businessAddress,
                        orderTypeId: response.data.orderTypeId,
                        remarks: response.data.remarks
                    };
                    console.log("商家信息设置成功:", business.value);
                } else {
                    console.log("API请求失败，进入else分支");
                    const errorMsg = response.message || "获取商家信息失败";
                    console.error("商家信息API返回失败:", errorMsg);
                    throw new Error(errorMsg);
                }
            } catch (error) {
                console.error("获取商家信息失败:", error);
            } finally {
                loadingBusiness.value = false;
            }
        };

        // 获取食品列表
        const fetchFoodList = async () => {
            loadingFoods.value = true;
            console.log(`开始获取食品列表，businessId: ${businessId.value}`);

            try {
                const response = await request.get("/api/foods/list", {
                    params: { businessId: businessId.value }
                });
                console.log("食品列表API响应:", response);

                if (response.success) {
                    // 过滤掉下架商品（shelveStatus === 0）
                    const availableFoods = response.data.filter(food => food.shelveStatus === 1);
                    console.log("可用食品列表:", availableFoods);

                    foodArr.value = availableFoods.map(item => ({
                        id: item.id,
                        foodId: item.id,
                        foodName: item.foodName,
                        foodPrice: item.foodPrice,
                        foodExplain: item.foodExplain,
                        foodImg: item.foodImg,
                        remarks: item.remarks,
                        businessId: item.businessId,
                        businessName: item.businessName
                    }));
                    console.log("食品列表设置成功:", foodArr.value);
                } else {
                    const errorMsg = response.message || "获取食品列表失败";
                    console.error("食品列表API返回失败:", errorMsg);
                    throw new Error(errorMsg);
                }
            } catch (error) {
                console.error("获取食品列表失败:", error);
                console.error("错误详情:", error.response || error.message);

                // 开发环境使用模拟数据
                if (import.meta.env.DEV) {
                    console.log("使用模拟食品数据");
                    foodArr.value = [
                        {
                            id: 1,
                            foodId: 1,
                            foodName: "模拟食品1",
                            foodPrice: 15,
                            foodExplain: "模拟食品描述",
                            foodImg: '/trade-assets/default-food.png',
                            remarks: "模拟备注",
                            businessId: businessId.value,
                            businessName: "模拟商家"
                        }
                    ];
                }
            } finally {
                loadingFoods.value = false;
                console.log("食品列表加载完成");
            }
        };

        // 跳转到购物车页面
        const goToCart = () => {
            console.log("跳转到订单页面，当前购物车商品数量:", totalQuantity.value);
            if (totalQuantity.value === 0) {
                toast.error("请先添加商品到购物车");
                return;
            }
            router.push({
                path: "/trade/cart",
                query: {
                    businessId: businessId.value
                }
            });
        };

        // 跳转到订单页面
        const toOrder = () => {
            console.log("跳转到购物车页面，当前购物车商品数量:", totalQuantity.value);
            if (totalQuantity.value === 0) {
                toast.error("购物车为空");
                return;
            }
            // 跳转到结算页面
			router.push({
				path: '/trade/userAddress',
				query: {
					businessId: businessId.value,
				}
			});
        };

        // 计算属性 - 基于后端购物车数据
        const totalPrice = computed(() => {
            const total = cartItems.value.reduce((total, item) => {
                return total + (item.foodPrice || 0) * (item.quantity || 0);
            }, 0);
            console.log(`计算总价: ${total}`);
            return total;
        });

        const totalQuantity = computed(() => {
            const quantity = cartItems.value.reduce((sum, item) => sum + (item.quantity || 0), 0);
            console.log(`计算总数量: ${quantity}`);
            return quantity;
        });

        const totalSettle = computed(() => {
            const settle = totalPrice.value + (business.value.deliveryPrice || 0);
            console.log(`计算结算总额: ${settle}`);
            return settle;
        });

        // 检查是否达到起送费
        const canOrder = computed(() => {
            const canOrder = totalPrice.value >= business.value.startPrice;
            console.log(`检查是否可下单: ${canOrder}`);
            return canOrder;
        });

        // 初始化
        onMounted(async () => {
            console.log("组件挂载完成");
            businessId.value = parseInt(route.query.businessId);

            if (!businessId.value) {
                console.error("无效的商家ID:", route.query.businessId);
                router.push("/trade");
                return;
            }

            await fetchUserInfo();
            await fetchBusinessInfo();
            await fetchFoodList();
            await fetchCartList(); // 获取购物车数据
            await loadReactions();
        });

        // 监听businessId变化
        watch(() => route.query.businessId, (newId) => {
            console.log("路由businessId变化:", newId);
            if (newId && parseInt(newId) !== businessId.value) {
                businessId.value = parseInt(newId);
                console.log("新的businessId:", businessId.value);
                fetchUserInfo();
                fetchBusinessInfo();
                fetchFoodList();
                fetchCartList(); // 重新获取购物车数据
                loadReactions();
            }
        });

        return {
            business,
            foodArr,
            loadingBusiness,
            loadingFoods,
            totalPrice,
            totalQuantity,
            totalSettle,
            canOrder,
            isLiked,
            isFavorited,
            getCartQuantity,
            add: addToCart,
            minus: removeFromCart,
            toOrder,
            goToCart,
            toggleLike,
            toggleFavorite,
            interactionLoading
        };
    }
};
</script>

<style scoped>
/* 样式部分保持不变 */
/****************** 总容器 ******************/
.wrapper {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  min-height: 100vh;
}

/****************** header部分 ******************/
/* .wrapper header {
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
} */
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
.wrapper title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color:white;
}
/****************** 商家logo部分 ******************/
.wrapper .business-logo {
    width: 100%;
    height: 50vw;
    /*使用上外边距避开header部分*/
    margin-top: 20vw;
    display: flex;
    justify-content: center;
    align-items: center;
}

.wrapper .business-logo img {
    width: 40vw;
    height: 40vw;
    border-radius: 5px;
}

/****************** 商家信息部分 ******************/
.wrapper .business-info {
    width: 100%;
    height: 20vw;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    position: relative;
}

.wrapper .business-info h1 {
    font-size: 5vw;
}

.wrapper .business-info .reactions {
    position: absolute;
    right: 3vw;
    bottom: 7vw;
    display: flex;
    gap: 4vw;
}

.wrapper .business-info .reactions .reaction {
    display: flex;
    align-items: center;
    gap: 1vw;
    cursor: pointer;
    user-select: none;
}

.wrapper .business-info .reactions .reaction i {
    font-size: 5vw;
    color: #bbb;
}

.wrapper .business-info p {
    font-size: 3vw;
    color: #666;
    margin-top: 1vw;
}

/****************** 食品列表部分 ******************/
.wrapper .food {
    width: 100%;
    /*使用下外边距避开footer部分*/
    margin-bottom: 14vw;
}

.wrapper .food li {
    width: 100%;
    box-sizing: border-box;
    padding: 2.5vw;
    user-select: none;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.wrapper .food li .food-left {
    display: flex;
    align-items: center;
}

.wrapper .food li .food-left img {
    width: 20vw;
    height: 20vw;
}

.wrapper .food li .food-left .food-left-info {
    margin-left: 3vw;
}

.wrapper .food li .food-left .food-left-info h3 {
    font-size: 3.8vw;
    color: #555;
}

.wrapper .food li .food-left .food-left-info p {
    font-size: 3vw;
     font-weight: 600;
    color: #888;
    margin-top: 2vw;
}

.wrapper .food li .food-right {
    width: 16vw;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.wrapper .food li .food-right .fa-minus-circle {
    font-size: 5.5vw;
    color: #999;
    cursor: pointer;
}

.wrapper .food li .food-right p {
    font-size: 3.6vw;
    color: #333;
}

.wrapper .food li .food-right .fa-plus-circle {
    font-size: 5.5vw;
    color: #0097ef;
    cursor: pointer;
}

/****************** 购物车部分 ******************/
.wrapper .cart {
    width: 100%;
    height: 14vw;
    position: fixed;
    left: 0;
    bottom: 0;
    display: flex;
}

.wrapper .cart .cart-left {
    flex: 2;
    background-color: #505051;
    display: flex;
}

.wrapper .cart .cart-left .cart-left-icon {
    width: 16vw;
    height: 16vw;
    box-sizing: border-box;
    border: solid 1.6vw #444;
    border-radius: 8vw;
    background-color: #3190e8;
    font-size: 7vw;
    color: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: -4vw;
    margin-left: 3vw;
    position: relative;
}

.wrapper .cart .cart-left .cart-left-icon-quantity {
    width: 5vw;
    height: 5vw;
    border-radius: 2.5vw;
    background-color: red;
    color: #fff;
    font-size: 3.6vw;
    display: flex;
    justify-content: center;
    align-items: center;
    position: absolute;
    right: -1.5vw;
    top: -1.5vw;
}

.wrapper .cart .cart-left .cart-left-info p:first-child {
    font-size: 4.5vw;
    color: #fff;
    margin-top: 1vw;
}

.wrapper .cart .cart-left .cart-left-info p:last-child {
    font-size: 2.8vw;
    color: #aaa;
}

.wrapper .cart .cart-right {
    flex: 1;
}

/*达到起送费时的样式*/
.wrapper .cart .cart-right .cart-right-item {
    width: 100%;
    height: 100%;
    background-color: #38ca73;
    color: #fff;
    font-size: 4.5vw;
    font-weight: 700;
    user-select: none;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
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


