<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/trade/utils/tradeRequest'; 

const route = useRoute();
const router = useRouter();

const products = ref([]);
const loading = ref(true);
const error = ref(null);
const defaultImg = ref('https://via.placeholder.com/60x60?text=Food'); 

const EXPLAIN_LIMIT = 15;
const formatExplain = (explain) => {
    if (!explain) return '暂无说明';
    return explain.length > EXPLAIN_LIMIT ? explain.substring(0, EXPLAIN_LIMIT) + '...' : explain;
};

const getToken = () => {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
};

const getUserId = () => {
    console.log('🔍 获取用户ID，检查存储...');
    
    try {
        const userInfoStr = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo');
        console.log('userInfo存储:', userInfoStr);
        if (userInfoStr) {
            const userInfo = JSON.parse(userInfoStr);
            console.log('userInfo解析:', userInfo);
            if (userInfo) {
                const userId = userInfo.id || userInfo.userId || userInfo.userId || userInfo.uid;
                if (userId) {
                    console.log('✅ 从userInfo获取到用户ID:', userId);
                    return userId;
                }
            }
        }
    } catch (err) {
        console.warn('解析用户信息失败:', err);
    }

    const storedUserId = localStorage.getItem('userId') || sessionStorage.getItem('userId');
    console.log('直接存储的userId:', storedUserId);
    if (storedUserId) {
        console.log('✅ 从直接存储获取到用户ID:', storedUserId);
        return storedUserId;
    }

    try {
        const token = getToken();
        if (token) {
            console.log('尝试解析token...');
            const payload = token.split('.')[1];
            if (payload) {
                const decoded = JSON.parse(atob(payload));
                console.log('Token解析结果:', decoded);
                const userId = decoded.userId || decoded.id || decoded.sub || decoded.user_id;
                if (userId) {
                    console.log('✅ 从token解析到用户ID:', userId);
                    return userId;
                }
            }
        }
    } catch (err) {
        console.warn('解析token失败:', err);
    }

    console.log('❌ 未找到用户ID');
    return null;
};

const showAlert = (message, type = 'error') => {
    if (type === 'error') {
        alert(`错误: ${message}`);
    } else {
        alert(message);
    }
};

const handleApiError = (error, fallbackMessage) => {
    console.error(fallbackMessage, error);
    if (error.response && error.response.status === 401) {
        showAlert('登录已过期，请重新登录！');
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        sessionStorage.removeItem('userInfo');
        localStorage.removeItem('userId');
        sessionStorage.removeItem('userId');
        router.push({ path: '/trade/login' });
    } else if (error.response) {
        showAlert(`${fallbackMessage} (HTTP ${error.response.status})` + 
                 (error.response.data?.message ? `: ${error.response.data.message}` : ''));
    } else {
        showAlert(fallbackMessage + (error.message ? `: ${error.message}` : ''));
    }
};

const fetchProducts = async (ids) => {
    const token = getToken();

    if (!ids || ids.length === 0) {
        loading.value = false;
        products.value = [];
        return;
    }

    if (!token) {
        console.error('❌ [DEBUG] 认证失败：getToken() 返回空值。');
        loading.value = false;
        error.value = '请先登录以查看商品详情。';
        return;
    }
    
    loading.value = true;
    error.value = null;
    
    console.log(`[DEBUG] Authorization Token (Exists): ${!!token}`);
    
    const detailPromises = ids.map(foodId => {
        const url = `/api/foods/detail/${foodId}`;
        console.log(`[DEBUG] 准备发送请求: ${url}`);
        
        return request.get(url, {
            headers: { 'Authorization': `Bearer ${token}` },
            validateStatus: function (status) { return status >= 200 && status < 600; }
        })
        .then(response => {
            const httpStatus = response.status || 200; 
            console.log(`[DEBUG] foodId: ${foodId} 请求成功响应 (HTTP ${httpStatus})`, response.data);
            return { ...response, foodId: foodId }; // 添加 foodId 到响应对象
        }) 
        .catch(err => {
            console.error(`🚨 [ERROR] foodId: ${foodId} 请求异常捕获:`, err);
            return { 
                isError: true, 
                status: err.response?.status || 'NETWORK_ERR',
                data: err.response?.data || { message: err.message || '底层网络或配置失败' },
                foodId: foodId
            };
        });
    });

    try {
        const responses = await Promise.all(detailPromises);
        
        const fetchedProducts = [];
        const failedRequests = [];

        responses.forEach((response) => {
            const foodId = response.foodId;

            if (response.isError) {
                failedRequests.push(foodId);
                return; 
            }

            let foodDetail = null;
            
            if (response.data && typeof response.data === 'object') {
                if (response.data.success !== undefined) {
                    if (response.data.success === true && response.data.data) {
                        foodDetail = response.data.data;
                    } else {
                        failedRequests.push(foodId);
                        console.warn(`⚠️ 获取 foodId: ${foodId} 详情业务失败: ${response.data.message || '未知错误'}`);
                        return;
                    }
                } else if (response.data.id !== undefined) {
                    foodDetail = response.data;
                }
            }

            if (!foodDetail) {
                failedRequests.push(foodId);
                console.error(`🚨 [DEBUG] foodId: ${foodId} 数据解析失败，响应结构:`, response.data);
                return;
            }
                    
            const shelveStatus = foodDetail.shelveStatus ?? 'N/A';
            const deletedStatus = foodDetail.deleted ?? 'N/A';
            console.log(`[DEBUG] foodId: ${foodId} 状态检查: shelveStatus=${shelveStatus}, deleted=${deletedStatus}`); 

            const isShelved = shelveStatus === 1 || shelveStatus === '1'; 
            const isNotDeleted = deletedStatus === false || deletedStatus === 0 || deletedStatus === '0'; 

            if (isShelved && isNotDeleted) {
                fetchedProducts.push(foodDetail);
            } else {
                failedRequests.push(foodId);
                const reason = (deletedStatus === true || deletedStatus === 1) 
                    ? '已删除' 
                    : (!isShelved ? '已下架 (Status:' + shelveStatus + ')' : '其他过滤');
                console.warn(`⚠️ foodId: ${foodId} 未通过前端显示条件，被过滤 (${reason})`);
            }
        });
        
        products.value = fetchedProducts;
        console.log(`✅ 成功加载 ${fetchedProducts.length} 条促销商品详情.`, fetchedProducts);
        
        if (failedRequests.length > 0) {
            console.warn(`部分商品请求失败或无法显示 (ID: ${failedRequests.join(', ')})`);
        }

    } catch (err) {
        console.error('❌ 批量获取商品详情失败 (Promise.all 之外的错误):', err);
        error.value = '连接商品详情服务失败。';
    } finally {
        loading.value = false;
    }
};

const navigateToPayment = async (product) => {
    if (!product || !product.id) {
        console.warn('⚠️ 缺少商品信息，无法进行购买。');
        showAlert('商品信息不完整，请刷新页面重试');
        return;
    }

    console.log('🛒 开始购买，检查用户信息...');

    const token = getToken();
    if (!token) {
        console.error('❌ 未找到用户认证信息，请先登录');
        showAlert('请先登录以进行购买');
        router.push({ path: '/trade/login' });
        return;
    }

    try {
        console.log(`🛒 开始购买商品: ${product.foodName} (ID: ${product.id})`);
        
        const userId = getUserId();
        
        if (!userId) {
            showAlert('无法获取用户信息，请重新登录');
            localStorage.removeItem('userInfo');
            sessionStorage.removeItem('userInfo');
            localStorage.removeItem('userId');
            sessionStorage.removeItem('userId');
            router.push({ path: '/trade/login' });
            return;
        }

        console.log('👤 当前用户ID:', userId);

        if (!product.businessId) {
            showAlert('商品信息不完整，缺少商家信息');
            return;
        }

        console.log(`🛒 正在将商品加入购物车: foodId=${product.id}, quantity=1`);
        
        const addCartResponse = await request.get('/api/carts/add', {
            params: {
                foodId: product.id,
                quantity: 1
            },
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        console.log('📦 购物车响应:', addCartResponse);

        if (!addCartResponse.data || addCartResponse.data.success === false) {
            const errorMsg = addCartResponse.data?.message || '加入购物车失败';
            console.error('❌ 加入购物车失败:', errorMsg);
            showAlert(errorMsg);
            return;
        }

        console.log('✅ 成功加入购物车');

        console.log(`📍 跳转到地址选择页面，商家ID: ${product.businessId}`);
        
        router.push({ 
            path: '/trade/userAddress', 
            query: { 
                businessId: product.businessId
            } 
        });

    } catch (err) {
        console.error('❌ 购买流程失败:', err);
        console.error('❌ 错误堆栈:', err.stack);
        
        if (err.response?.status === 401 || err.message.includes('登录') || err.message.includes('认证')) {
            showAlert('登录已过期，请重新登录');
            localStorage.removeItem('token');
            sessionStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            sessionStorage.removeItem('userInfo');
            router.push({ path: '/trade/login' });
        } else {
            const errorMsg = err.response?.data?.message || err.message || '请稍后重试';
            showAlert(`购买失败: ${errorMsg}`);
        }
    }
};


onMounted(() => {
    const idsParam = route.query.ids; 
    let foodIdsArray = [];
    
    if (idsParam) {
        foodIdsArray = idsParam.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
    }
    
    if (foodIdsArray.length === 0) {
        foodIdsArray = [1, 2, 3, 4]; 
    }

    console.log('🚀 页面获取到的 foodIds (真实请求):', foodIdsArray);
    
    fetchProducts(foodIdsArray);
});

defineExpose({
    products,
    loading,
    error,
    formatExplain,
    navigateToPayment,
    defaultImg
});
</script>

<template>
        <div class="back-btn-container">
        <BackButton style="margin-top: 2vw;"/>
    </div>
        <!-- 页面标题 -->
        <div class="details-container">
        <div class="header app-header-fixed">
            <h3>促销商品</h3>
        </div>


        <!-- 加载状态 -->
        <div v-if="loading" class="loading">
            加载中...
        </div>

        <!-- 错误状态 -->
        <div v-if="error && !loading" class="error">
            {{ error }}
        </div>

        <!-- 商品列表 -->
        <div v-if="!loading && !error && products.length > 0" class="product-list">
            <div v-for="product in products" :key="product.id" class="product-card">
                <div class="product-image">
                    <img :src="product.foodImg || defaultImg" :alt="product.foodName" @error="defaultImg" />
                </div>
                <div class="product-info">
                    <h3>{{ product.businessName ? `${product.businessName} | ${product.foodName}` : product.foodName }}</h3>
                    <p class="product-description">{{ formatExplain(product.foodExplain) }}</p>
                    <div class="product-price">
                        <span class="current-price">¥{{ product.foodPrice }}</span>
                        <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
                    </div>
                    <div class="product-actions">
                        <button class="buy-btn" @click="navigateToPayment(product)">
                            立即购买
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && !error && products.length === 0" class="empty">
            暂无促销商品
        </div>
    </div>
</template>

<style scoped>
.app-header-fixed {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    max-width: 600px; 
    left: 50%;
    transform: translateX(-50%); 
    
    height: 12vw; /* 统一高度，使用 vw */
    background-color: #0097FF;
    color: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.app-header-fixed h3,
.app-header-fixed h1 { /* 统一 h1 和 h3 的样式 */
    font-size: 4.8vw; /* 统一字体大小 */
    margin: 0;
    font-weight: 500;
    color: inherit; /* 继承父级的 #fff 颜色 */
}

.details-container {
    padding-top: 13vw; /* 顶部容器增加内边距，等于头部高度 */
}

.back-btn-container {
    position: fixed; /* 固定定位，不随滚动移动 */
    left: 0vw; /* 距离左侧的距离，可根据需求调整 */
    top: 0vw; /* 距离顶部的距离，与 header 高度（12vw）适配，确保垂直居中 */
    z-index: 1001; /* 比 header 的 z-index:1000 高，避免被遮挡 */
}

.page-header {
    text-align: center;
    margin-bottom: 30px;
}

.page-header h1 {
    font-size: 24px;
    color: #333;
    font-weight: bold;
}

.loading, .error, .empty {
    text-align: center;
    padding: 50px;
    font-size: 16px;
    color: #666;
}

.error {
    color: #f56c6c;
}

.product-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.product-card {
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    transition: transform 0.3s ease;
}

.product-card:hover {
    transform: translateY(-5px);
}

.product-image {
    width: 100%;
    height: 180px;
    overflow: hidden;
}

.product-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.product-info {
    padding: 15px;
}

.product-info h3 {
    font-size: 18px;
    color: #333;
    margin-bottom: 10px;
    font-weight: bold;
}

.product-description {
    font-size: 14px;
    color: #666;
    margin-bottom: 15px;
    line-height: 1.4;
}

.product-price {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 15px;
}

.current-price {
    font-size: 20px;
    color: #e4393c;
    font-weight: bold;
}

.original-price {
    font-size: 14px;
    color: #999;
    text-decoration: line-through;
}

.product-actions {
    text-align: right;
}

.buy-btn {
    background-color: #0097FF;
    color: #fff;
    border: none;
    padding: 8px 20px;
    border-radius: 20px;
    font-size: 14px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.3s ease;
}

.buy-btn:hover {
    background-color: #0081e6;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 151, 255, 0.3);
}

.buy-btn:active {
    transform: translateY(0);
}

@media (max-width: 768px) {
    .promotion-container {
        padding: 10px;
    }
    
    .product-list {
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 15px;
    }
    
    .product-image {
        height: 150px;
    }
}
</style>