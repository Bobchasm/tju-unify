<template>
    <div class="back-btn-container" v-if="!showRulesModal">
        <BackButton style="margin-top: 2vw;"/>
    </div>
    <div class="details-container">
        <div class="header app-header-fixed">
            <h3>我的积分</h3>
            <i class="fas fa-info-circle rules-icon" @click="showRulesModal = true"></i>
        </div>

        <div class="tab-content overview-content">
            
            <div class="current-points-card" @click="goToDetails">
                <i class="fas fa-star points-icon-large"></i>
                <div class="points-detail">
                    <span class="label">可用积分</span>
                    <span class="value">{{ availablePoints }}</span>
                    
                    <div class="available-points-info">
                        <span class="available-label">当前积分:</span>
                        <span class="available-value">{{ totalPoints }}</span>
                    </div>
                </div>
                <i class="fas fa-chevron-right detail-link-icon"></i> 
            </div>

            <div 
                :class="['recent-expiry-detail', { 'no-warning': expiringCount === 0 }]" 
                class="card-small"
                @click="goToExpiringPage"
            >
                <i :class="['fas', expiringCount > 0 ? 'fa-exclamation-triangle' : 'fa-check-circle']"></i> 
                <span v-if="expiringCount > 0">
                    您有 {{ expiringCount }} 积分即将过期！
                </span>
                <span v-else>
                    暂无即将过期的积分。
                </span>
                <i class="fas fa-chevron-right detail-link-icon-small"></i>
            </div>

            <div class="lottery-entry-card card-small" @click="goToLottery">
                <div class="main-info">
                    <i class="fas fa-gift lottery-icon"></i>
                    <span class="title">点击进入积分抽奖</span>
                </div>
                <div class="extra-tip">
                    <i class="fas fa-crown"></i> 
                    <span>升级VIP尽享更多福利~</span>
                    <i class="fas fa-chevron-right"></i>
                </div>
            </div>
            <div class="exchange-section card">
                <h4>积分兑换商品 ({{ exchangeProducts.length }})</h4>
                <div class="product-grid">
                    <div 
                        v-for="product in exchangeProducts.filter(p => p.stockQuantity > 0)" 
                        :key="product.foodId" 
                        class="product-card"
                    >
                        <div class="product-image" :style="{ backgroundImage: 'url(' + product.foodImg + ')' }"></div>
                        <div class="product-info">
                            <p class="name">{{ product.foodName }}</p>
                            <div class="price">
                                <i class="fas fa-star"></i>
                                <span class="points-cost">{{ product.requiredPoints }}</span>
                            </div>
                            <div class="stock-info">
                                库存: <span :class="{'low-stock': product.stockQuantity < 10}">{{ product.stockQuantity }}</span>
                            </div>
                            <button 
                                :disabled="availablePoints < product.requiredPoints || product.stockQuantity <= 0"
                                class="exchange-btn"
                                @click.stop="handleExchange(product)"
                            >
                                立即兑换
                            </button>
                        </div>
                    </div>
                </div>
                <div v-if="exchangeProducts.filter(p => p.stockQuantity > 0).length === 0" class="empty-state-small">
                    <i class="fas fa-box-open"></i> 暂无商品可供兑换。
                </div>
            </div>
        </div>

        <div v-if="showExchangeModal" class="modal-overlay" @click.self="showExchangeModal = false">
            <div class="modal-content">
                <div class="modal-header">
                    <h4>兑换 {{ currentProduct.foodName }}</h4>
                    <button class="close-btn" @click="showExchangeModal = false">&times;</button>
                </div>
                <div v-if="currentProduct" class="modal-body">
                    <p>
                        <strong>消耗积分:</strong> 
                        <span class="required-points">{{ currentProduct.requiredPoints * exchangeQuantity }}</span>
                        (当前可用: {{ availablePoints }})
                    </p>
                    <p>
                        <strong>单品积分:</strong> {{ currentProduct.requiredPoints }}
                    </p>
                    
                    <div class="form-group">
                        <label>兑换数量:</label>
                        <div class="quantity-control">
                            <button @click="decreaseQuantity" :disabled="exchangeQuantity <= 1">-</button>
                            <input type="number" v-model.number="exchangeQuantity" readonly min="1" :max="maxQuantity"/>
                            <button @click="increaseQuantity" :disabled="exchangeQuantity >= maxQuantity">+</button>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>配送地址:</label>
                        <select v-model="selectedAddressId" class="address-select">
                            <option value="">请选择收货地址</option>
                            <option 
                                v-for="address in userAddresses" 
                                :key="address.id" 
                                :value="address.id"
                            >
                                {{ address.contactName }} ({{ address.contactTel }}) - {{ address.address }}
                            </option>
                        </select>
                        <p v-if="userAddresses.length === 0" class="address-tip">
                            <i class="fas fa-info-circle"></i> 暂无地址，请先添加。
                        </p>
                    </div>

                    <p v-if="availablePoints < currentProduct.requiredPoints * exchangeQuantity" class="warning-text">
                        <i class="fas fa-exclamation-circle"></i> 积分不足以兑换当前数量。
                    </p>

                    <button 
                        @click="confirmExchange" 
                        :disabled="!selectedAddressId || availablePoints < currentProduct.requiredPoints * exchangeQuantity || isExchanging"
                        class="confirm-btn"
                    >
                        {{ isExchanging ? '兑换中...' : `确认兑换 (${currentProduct.requiredPoints * exchangeQuantity} 积分)` }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 积分规则弹窗 -->
        <div v-if="showRulesModal" class="modal-overlay" @click.self="showRulesModal = false">
            <div class="modal-content rules-modal">
                <div class="modal-header">
                    <h4>积分规则</h4>
                    <button class="close-btn" @click="showRulesModal = false">&times;</button>
                </div>
                <div class="modal-body rules-content">
                    <div v-if="loadingRules" class="loading-rules">
                        <i class="fas fa-spinner fa-spin"></i> 加载中...
                    </div>
                    <div v-else>
                        <div class="rule-section">
                            <h5 class="rule-title">
                                <i class="fas fa-star"></i> 积分获取规则
                            </h5>
                            <div class="rule-text">
                                <div v-if="pointsRules.length === 0" class="no-rules">暂无规则信息</div>
                                <div v-else>
                                    <div v-for="(rule, index) in pointsRules" :key="rule.id || index" class="rule-item">
                                        <div class="rule-name">{{ rule.ruleName || '未命名规则' }}</div>
                                        <div class="rule-details">
                                            <div v-if="rule.pointsRatio != null" class="rule-detail-item">
                                                <span class="detail-label">积分比例：</span>
                                                <span class="detail-value">{{ rule.pointsRatio }}（消费1元获得{{ rule.pointsRatio }}积分）</span>
                                            </div>
                                            <div v-if="rule.pointsMultiplier != null" class="rule-detail-item">
                                                <span class="detail-label">积分倍数：</span>
                                                <span class="detail-value">{{ rule.pointsMultiplier }}</span>
                                            </div>
                                            <div v-if="rule.expireDays != null" class="rule-detail-item">
                                                <span class="detail-label">有效期：</span>
                                                <span class="detail-value">{{ rule.expireDays }} 天</span>
                                            </div>
                                            <div v-if="rule.priority != null" class="rule-detail-item">
                                                <span class="detail-label">优先级：</span>
                                                <span class="detail-value">{{ rule.priority }}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="rule-section">
                            <h5 class="rule-title">
                                <i class="fas fa-exchange-alt"></i> 积分消费规则
                            </h5>
                            <div class="rule-text">
                                <div v-if="exchangeRules.length === 0" class="no-rules">暂无规则信息</div>
                                <div v-else>
                                    <div v-for="(rule, index) in exchangeRules" :key="rule.id || index" class="rule-item">
                                        <div class="rule-name">{{ rule.ruleName || '未命名规则' }}</div>
                                        <div class="rule-details">
                                            <div v-if="rule.exchangeRatio != null" class="rule-detail-item">
                                                <span class="detail-label">兑换比例：</span>
                                                <span class="detail-value">{{ rule.exchangeRatio }} 积分 = 1 元</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="confirm-btn" @click="showRulesModal = false">我知道了</button>
                </div>
            </div>
        </div>

    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router'; 
import request from '@/trade/utils/tradeRequest';
import { toast } from '@/trade/utils/toast';

// 初始化路由
const router = useRouter();

// --- 响应式数据 ---
const totalPoints = ref(0); 
const availablePoints = ref(0);
const expiringCount = ref(0); 
const exchangeProducts = ref([]);
const userAddresses = ref([]); 

// 兑换弹窗状态
const showExchangeModal = ref(false);
const currentProduct = ref(null);
const exchangeQuantity = ref(1);
const selectedAddressId = ref('');
const maxQuantity = ref(1); 
const isExchanging = ref(false);

// 规则弹窗状态
const showRulesModal = ref(false);
const pointsRules = ref([]);
const exchangeRules = ref([]);
const loadingRules = ref(false); 

// --- 工具函数 ---

const getToken = () => {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
};

const getUserId = () => {
    try {
        const userInfo = JSON.parse(sessionStorage.getItem('userInfo'));
        return userInfo ? userInfo.id : null; 
    } catch (e) {
        return null;
    }
};

const handleApiError = (error, fallbackMessage) => {
    console.error(fallbackMessage, error);
    
    if (error.response && error.response.status === 401) {
        toast.error('登录已过期，请重新登录！');
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('userInfo');
        router.push({ path: '/trade/login' });
    } else {
        toast.error(fallbackMessage + (error.message ? `: ${error.message}` : ''));
    }
    isExchanging.value = false;
};

// --- API 调用逻辑 (保持不变，省略大部分实现细节) ---
// ... (fetchPointsAccount, fetchExchangeGoods, fetchExpiringCount, fetchUserAddresses, confirmExchange) ...

const refreshAllData = async () => {
    await Promise.all([
        fetchPointsAccount(),
        fetchExchangeGoods(),
        fetchExpiringCount(),
        fetchUserAddresses(),
    ]);
};

// 1. 查询积分账户
const fetchPointsAccount = async () => {
    const token = getToken();
    if (!token) return;
    try {
        const response = await request.get(`/api/points/account`, {
            headers: { 'Authorization': `Bearer ${token}` },
        });

        if (response && response.success && response.data) {
            availablePoints.value = response.data.availablePoints || 0;
            totalPoints.value = response.data.totalPoints || 0;
        } else {
            toast.error('获取积分账户失败: ' + (response ? response.message : '未知错误'));
        }
    } catch (e) {
        handleApiError(e, '获取积分账户异常');
    }
};

// 2. 获取可兑换商品列表
const fetchExchangeGoods = async () => {
    const token = getToken();
    if (!token) return;
    try {
        const response = await request.get(`/api/points/exchange-goods`, {
            headers: { 'Authorization': `Bearer ${token}` },
        });

        if (response && response.success && response.data) {
            exchangeProducts.value = response.data.map(item => ({
                foodId: item.foodId,
                foodName: item.foodName,
                foodImg: item.foodImg || 'https://via.placeholder.com/150/cccccc/ffffff?text=Product',
                requiredPoints: item.requiredPoints,
                stockQuantity: item.stockQuantity,
            }));
        } else {
            toast.error('获取兑换商品失败: ' + (response ? response.message : '未知错误'));
        }
    } catch (e) {
        handleApiError(e, '获取兑换商品异常');
    }
};

// 3. 统计即将过期的积分总数
const fetchExpiringCount = async () => {
    const token = getToken();
    if (!token) return;
    try {
        // 接口地址改为 /api/points/expiring/count (与文档确认一致)
        const response = await request.get(`/api/points/expiring/count`, {
            headers: { 'Authorization': `Bearer ${token}` },
        });
        
        if (response && response.success) {
            // data直接返回积分总数 (int64)
            expiringCount.value = response.data || 0; 
        } else {
            // 不进行 toast.error，避免频繁弹窗，但记录错误
            console.error('获取过期积分统计失败', response);
            expiringCount.value = 0; // 失败时重置为 0
        }
    } catch (e) {
        // handleApiError(e, '获取过期积分统计异常'); // 避免对主页面的过度干扰
        console.error('获取过期积分统计异常', e);
        expiringCount.value = 0;
    }
};

// 4. 获取用户地址列表 (新增)
const fetchUserAddresses = async () => {
    const token = getToken();
    const userId = getUserId();
    if (!token || !userId) {
        userAddresses.value = [];
        return;
    }

    try {
        const response = await request.get(`/api/addresses/listDeliveryAddressByUserId`, {
            params: { userId },
            headers: { 'Authorization': `Bearer ${token}` },
        });

        if (response && response.success && Array.isArray(response.data)) {
            userAddresses.value = response.data;
            if (userAddresses.value.length > 0) {
                selectedAddressId.value = userAddresses.value[0].id;
            }
        } else {
            toast.error('获取收货地址失败: ' + (response ? response.message : '未知错误'));
            userAddresses.value = [];
        }
    } catch (e) {
        handleApiError(e, '获取收货地址异常');
        userAddresses.value = [];
    }
};

// 5. 提交积分兑换
const confirmExchange = async () => {
    if (isExchanging.value) return;
    if (!currentProduct.value || !selectedAddressId.value || exchangeQuantity.value <= 0) {
        toast.error('请选择商品、数量和配送地址。');
        return;
    }
    const requiredPoints = currentProduct.value.requiredPoints * exchangeQuantity.value;
    if (availablePoints.value < requiredPoints) {
        toast.error('积分不足，无法完成兑换！');
        return;
    }

    isExchanging.value = true;
    const token = getToken();

    const payload = {
        foodId: currentProduct.value.foodId,
        quantity: exchangeQuantity.value,
        addressId: selectedAddressId.value
    };

    try {
        const response = await request.post(`/api/points/exchange`, payload, {
            headers: { 'Authorization': `Bearer ${token}` },
        });

        if (response && response.success) {
            toast.success('积分兑换成功！');
            showExchangeModal.value = false;
            await refreshAllData();
        } else {
            toast.error('兑换失败: ' + (response ? response.message : '未知错误'));
        }
    } catch (e) {
        handleApiError(e, '兑换商品异常');
    } finally {
        isExchanging.value = false;
    }
};


// --- 获取积分规则 ---
const fetchPointsRules = async () => {
    loadingRules.value = true;
    const token = getToken();
    if (!token) {
        loadingRules.value = false;
        return;
    }
    
    try {
        // 并行获取两个规则
        const [pointsResponse, exchangeResponse] = await Promise.all([
            request.get('/api/marketing/points/rules', {
                headers: { 'Authorization': `Bearer ${token}` },
            }),
            request.get('/api/marketing/points/exchange-rules', {
                headers: { 'Authorization': `Bearer ${token}` },
            })
        ]);

        if (pointsResponse && pointsResponse.success) {
            pointsRules.value = Array.isArray(pointsResponse.data) ? pointsResponse.data : [];
        } else {
            pointsRules.value = [];
        }

        if (exchangeResponse && exchangeResponse.success) {
            exchangeRules.value = Array.isArray(exchangeResponse.data) ? exchangeResponse.data : [];
        } else {
            exchangeRules.value = [];
        }
    } catch (e) {
        console.error('获取积分规则失败:', e);
        pointsRules.value = [];
        exchangeRules.value = [];
    } finally {
        loadingRules.value = false;
    }
};

// 监听弹窗打开，获取规则
watch(showRulesModal, (newVal) => {
    if (newVal) {
        fetchPointsRules();
    }
});

// --- 逻辑处理 ---

const handleExchange = (product) => {
    if (availablePoints.value < product.requiredPoints) {
        toast.error('积分不足，无法兑换！');
        return;
    }
    if (product.stockQuantity <= 0) {
        toast.error('该商品库存不足！');
        return;
    }

    currentProduct.value = product;
    exchangeQuantity.value = 1;
    const maxByStock = product.stockQuantity;
    const maxByPoints = Math.floor(availablePoints.value / product.requiredPoints);
    maxQuantity.value = Math.min(maxByStock, maxByPoints);
    
    if (exchangeQuantity.value > maxQuantity.value) {
        exchangeQuantity.value = maxQuantity.value;
    }
    
    if (userAddresses.value.length === 0) {
        toast.warning('请先添加收货地址！');
    }
    
    if (userAddresses.value.findIndex(a => a.id === selectedAddressId.value) === -1) {
        selectedAddressId.value = userAddresses.value.length > 0 ? userAddresses.value[0].id : '';
    }

    showExchangeModal.value = true;
};

const increaseQuantity = () => {
    if (exchangeQuantity.value < maxQuantity.value) {
        exchangeQuantity.value++;
    }
};

const decreaseQuantity = () => {
    if (exchangeQuantity.value > 1) {
        exchangeQuantity.value--;
    }
};

// 跳转到积分明细页面 (PointsDetails)
const goToDetails = () => {
    router.push({ name: 'TradePointsDetails' }); 
};

// 【主要改动】：跳转到即将过期积分页面 (PointsExpiring)
const goToExpiringPage = () => {
    router.push({ name: 'TradePointsExpiring' }); 
}

// 跳转到积分抽奖页面
const goToLottery = () => {
    router.push({ name: 'TradePointsLottery' }); 
}

onMounted(() => {
    const token = getToken();
    const userId = getUserId();

    if (!token || !userId) {
        toast.error('用户未登录或用户ID缺失，请先登录！');
        router.push({ path: '/trade/login' });
        return;
    }

    refreshAllData();
});
</script>

<style scoped>
/* CSS 样式部分 (为新功能添加小箭头样式) */

/* 容器和全局样式 */
/* .points-container {
    padding: 0;
    max-width: 600px;
    margin: 0 auto;
    background-color: #f4f7f9;
    min-height: 100vh;
}

/* 页面头部 */
/* .header {
    background-color: #fff;
    padding: 15px 20px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    position: sticky;
    top: 0;
    z-index: 10;
    text-align: center;
}
.header h3 {
    margin: 0;
    font-size: 1.2rem;
    color: #333;
}  */

.back-btn-container {
    position: fixed; /* 固定定位，不随滚动移动 */
    left: 0vw; /* 距离左侧的距离，可根据需求调整 */
    top: 0vw; /* 距离顶部的距离，与 header 高度（12vw）适配，确保垂直居中 */
    z-index: 1001; /* 比 header 的 z-index:1000 高，避免被遮挡 */
}

.tab-content {
    padding: 0 15px 20px 15px;
}

/* 公共卡片样式 */
.card {
    background-color: #fff;
    border-radius: 12px;
    padding: 15px;
    margin-bottom: 15px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.card h4 {
    color: #333;
    font-size: 1.05rem;
    margin-bottom: 15px;
    border-left: 4px solid #2979ff;
    padding-left: 8px;
    font-weight: 600;
}

/* 1. 积分概览样式 */
.current-points-card {
    background: linear-gradient(135deg, #2979ff, #64b5f6);
    color: white;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 8px 20px rgba(41, 121, 255, 0.3);
    display: flex;
    align-items: center;
    justify-content: space-between;
    cursor: pointer;
    margin-bottom: 15px;
    transition: transform 0.2s ease;
}
.current-points-card:active {
    transform: scale(0.98);
}
.points-icon-large {
    font-size: 3rem;
    opacity: 0.9;
    flex-shrink: 0;
}
.points-detail {
    display: flex;
    flex-direction: column;
    flex-grow: 1;
    margin-left: 15px;
}
.points-detail .label {
    font-size: 0.9rem;
    opacity: 0.8;
}
.points-detail .value {
    font-size: 2.5rem;
    font-weight: 700;
    line-height: 1.1;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.available-points-info {
    font-size: 0.75rem; 
    margin-top: 5px; 
    padding-top: 5px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
    display: flex;
    gap: 5px;
    align-items: center;
}

.available-label {
    opacity: 0.7;
    font-weight: 300;
}

.available-value {
    font-weight: 600;
}
.detail-link-icon {
    font-size: 1.2rem;
    color: rgba(255, 255, 255, 0.8);
    flex-shrink: 0;
    margin-left: 10px;
}
/* 【新增样式】：小卡片右侧的箭头 */
.detail-link-icon-small {
    font-size: 1rem;
    color: #999;
    margin-left: auto; /* 靠右对齐 */
    flex-shrink: 0;
}


/* B. 即将过期积分提醒 */
.card-small {
    padding: 10px 15px;
    border-radius: 8px;
    margin-bottom: 15px; 
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    cursor: pointer; 
}
.recent-expiry-detail {
    background-color: #fff3e0;
    border: 1px solid #ffcc80;
    font-size: 0.9rem;
    color: #e65100;
    line-height: 1.5;
    display: flex;
    align-items: center;
    justify-content: flex-start; /* 保持内容左对齐 */
    position: relative;
    padding-right: 35px; /* 留出箭头空间 */
}
.recent-expiry-detail i {
    margin-right: 8px;
    color: #ff9800;
}
.recent-expiry-detail strong {
    font-weight: 700;
    color: #b74a00;
}
.recent-expiry-detail.no-warning {
    background-color: #e8f5e9;
    border-color: #a5d6a7;
    color: #388e3c;
}
.recent-expiry-detail.no-warning i {
    color: #4caf50;
}
/* 将小箭头定位到右侧 */
.recent-expiry-detail .detail-link-icon-small {
    position: absolute;
    right: 15px;
    color: #e65100;
}
.recent-expiry-detail.no-warning .detail-link-icon-small {
    color: #4caf50;
}

/* ⭐ 积分抽奖入口样式 ⭐ */
.lottery-entry-card {
    background: linear-gradient(90deg, #ffeb3b, #ffc107);
    border: 1px solid #ff9800;
    color: #333;
    cursor: pointer;
    padding: 12px 15px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    transition: transform 0.2s;
    overflow: hidden; 
}

.lottery-entry-card:active {
    transform: scale(0.98);
}

.lottery-entry-card .main-info {
    display: flex;
    align-items: center;
    font-size: 1rem;
    font-weight: 700;
    color: #d84315; 
}

.lottery-icon {
    font-size: 1.4rem;
    margin-right: 10px;
    animation: pulse 1.5s infinite;
}

.lottery-entry-card .title {
    flex-grow: 1;
}

.lottery-entry-card .extra-tip {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 0.8rem;
    color: #795548; 
    padding-top: 5px;
    border-top: 1px dashed rgba(255, 255, 255, 0.5); 
}

.lottery-entry-card .extra-tip i {
    color: #e91e63; 
    margin-right: 5px;
}
.lottery-entry-card .extra-tip .fa-chevron-right {
    color: #d84315;
    margin-left: 10px;
}

/* 脉冲动画 */
@keyframes pulse {
    0% { transform: scale(1); }
    50% { transform: scale(1.05); }
    100% { transform: scale(1); }
}

/* C. 积分兑换商品 */
.exchange-section {
    padding: 15px;
}
.product-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr); 
    gap: 15px;
}
.product-card {
    background-color: #fff;
    border: 1px solid #eee;
    border-radius: 10px;
    box-shadow: none;
    overflow: hidden;
    transition: transform 0.2s;
}
.product-card:hover {
    transform: translateY(-2px);
}
.product-image {
    width: 100%;
    height: 100px;
    background-size: cover;
    background-position: center;
    border-bottom: 1px solid #f0f0f0;
}
.product-info {
    padding: 8px;
    display: flex;
    flex-direction: column;
    gap: 3px;
}
.product-info .name {
    font-size: 0.85rem;
    font-weight: 500;
    color: #333;
    height: 32px; 
    overflow: hidden;
    line-height: 1.1;
}
.price {
    font-size: 0.9rem;
    color: #ff9800;
    font-weight: 700;
}
.price i {
    margin-right: 3px;
    font-size: 0.8rem;
}
.stock-info {
    font-size: 0.75rem;
    color: #777;
    margin-bottom: 5px;
}
.stock-info .low-stock {
    color: #e65100;
    font-weight: 600;
}

.exchange-btn {
    padding: 6px 0;
    font-size: 0.85rem;
    border-radius: 6px;
    background-color: #2979ff;
    color: white;
    border: none;
    cursor: pointer;
    transition: background-color 0.3s;
}
.exchange-btn:disabled {
    background-color: #ccc;
    color: #666;
    font-weight: 500;
    cursor: not-allowed;
}
.empty-state-small {
    text-align: center;
    padding: 20px;
    color: #999;
    font-size: 0.9rem;
}

/* ==================================== */
/* 兑换弹窗 Modal 样式 */
/* ==================================== */
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
    z-index: 1000;
}
.modal-content {
    background: white;
    padding: 20px;
    border-radius: 12px;
    width: 90%;
    max-width: 400px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-20px); }
    to { opacity: 1; transform: translateY(0); }
}

/* 规则图标样式 */
.rules-icon {
    position: absolute;
    right: 4vw;
    top: 50%;
    transform: translateY(-50%);
    font-size: 5vw;
    color: #fff;
    cursor: pointer;
    transition: all 0.3s;
    z-index: 1001;
}

.rules-icon:hover {
    color: #ffd700;
    transform: translateY(-50%) scale(1.1);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
}
.modal-header h4 {
    margin: 0;
    font-size: 1.2rem;
    color: #333;
}
.close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #999;
    padding: 0;
}
/* 规则图标样式 */
.rules-icon {
    position: absolute;
    right: 4vw;
    top: 50%;
    transform: translateY(-50%);
    font-size: 5vw;
    color: #fff;
    cursor: pointer;
    transition: all 0.3s;
    z-index: 1001;
}

.rules-icon:hover {
    color: #ffd700;
    transform: translateY(-50%) scale(1.1);
}

.modal-body p {
    margin: 10px 0;
    font-size: 0.95rem;
    color: #555;
}
.modal-body strong {
    color: #333;
    font-weight: 600;
}
.required-points {
    color: #f44336;
    font-weight: 700;
}
.form-group {
    margin-bottom: 15px;
}
.form-group label {
    display: block;
    margin-bottom: 5px;
    font-weight: 500;
    color: #333;
}

/* 数量控制器 */
.quantity-control {
    display: flex;
    align-items: center;
    width: 120px;
    border: 1px solid #ddd;
    border-radius: 8px;
    overflow: hidden;
}
.quantity-control button {
    width: 30px;
    height: 30px;
    background-color: #f0f0f0;
    border: none;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.2s;
}
.quantity-control button:disabled {
    background-color: #eee;
    color: #ccc;
    cursor: not-allowed;
}
.quantity-control input[type="number"] {
    flex-grow: 1;
    text-align: center;
    border: none;
    height: 30px;
    font-size: 0.95rem;
    font-weight: 600;
    color: #333;
    -moz-appearance: textfield; 
}
.quantity-control input::-webkit-outer-spin-button,
.quantity-control input::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
}

/* 地址选择器 */
.address-select {
    width: 100%;
    padding: 10px;
    border-radius: 8px;
    border: 1px solid #ccc;
    font-size: 0.9rem;
    background-color: white;
}
.address-tip {
    font-size: 0.8rem;
    color: #ff9800;
    margin-top: 5px;
}

/* 警告/确认按钮 */
.warning-text {
    color: #f44336 !important;
    font-weight: 600;
    margin-top: 15px;
}
.warning-text i {
    margin-right: 5px;
}

/* 规则弹窗样式 */
.rules-modal {
    max-width: 90%;
    max-height: 80vh;
    overflow-y: auto;
}

.rules-content {
    max-height: 60vh;
    overflow-y: auto;
    padding: 15px 0;
}

.loading-rules {
    text-align: center;
    padding: 40px 20px;
    color: #999;
    font-size: 1rem;
}

.loading-rules i {
    font-size: 1.5rem;
    margin-right: 10px;
}

.rule-section {
    margin-bottom: 25px;
    padding-bottom: 20px;
    border-bottom: 1px dashed #e0e0e0;
}

.rule-section:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
}

.rule-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 1.1rem;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
}

.rule-title i {
    color: #ff6b6b;
    font-size: 1rem;
}

.rule-text {
    color: #666;
    line-height: 1.8;
    font-size: 0.95rem;
    padding-left: 24px;
}

.rule-text p {
    margin: 8px 0;
}

.rule-text ul,
.rule-text ol {
    margin: 8px 0;
    padding-left: 20px;
}

.rule-text li {
    margin: 5px 0;
}

.rule-item {
    margin-bottom: 12px;
    padding: 10px;
    background-color: #f8f9fa;
    border-radius: 6px;
    border-left: 3px solid #ff6b6b;
}

.rule-name {
    font-weight: 600;
    color: #333;
    font-size: 0.95rem;
    margin-bottom: 8px;
}

.rule-details {
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid #e8e8e8;
}

.rule-detail-item {
    display: flex;
    align-items: center;
    margin-bottom: 6px;
    font-size: 0.85rem;
    color: #666;
}

.rule-detail-item:last-child {
    margin-bottom: 0;
}

.detail-label {
    color: #888;
    margin-right: 6px;
    min-width: 70px;
}

.detail-value {
    color: #333;
    font-weight: 500;
}

.no-rules {
    color: #999;
    font-style: italic;
    text-align: center;
    padding: 20px;
}

.modal-footer {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px solid #eee;
    text-align: center;
}

.modal-footer .confirm-btn {
    background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
    color: white;
    border: none;
    padding: 10px 40px;
    border-radius: 25px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
}

.modal-footer .confirm-btn:hover {
    background: linear-gradient(135deg, #ff5252, #ff7979);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.confirm-btn {
    width: 100%;
    padding: 12px;
    margin-top: 20px;
    border-radius: 8px;
    background-color: #2a7efc;
    color: white;
    border: none;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.3s;
}
.confirm-btn:disabled {
    background-color: #ccc;
    cursor: not-allowed;
}

/* 移动端适配 */
@media (max-width: 480px) {
    .modal-content {
        width: 95%;
    }
}
</style>