<template>
    <div class="back-btn-container">
        <BackButton style="margin-top: 2vw;"/>
    </div>
    <div class="wrapper">

        <div class="fixed-top">
          <div class="top-background">
              <h1>在线支付</h1>
          </div>
      	</div>

        <div v-if="loading" class="loading">
            <p>加载中...</p>
        </div>

        <template v-else>
            <div class="content">
                <div class="section order-section">
                    
                    <div class="section-header final-amount-section">
                        <h3 class="final-label">合计</h3>
                        <span class="final-amount">¥{{ finalPaymentAmount }}</span>
                    </div>

                    <div class="delivery-info">
                        <div class="info-item">
                            <i class="fa fa-map-marker"></i>
                            <span>{{ orderDetail.address || '未选择地址' }}</span>
                        </div>
                        <div class="info-item">
                            <i class="fa fa-user"></i>
                            <span>{{ orderDetail.contactName }} {{ orderDetail.contactSex === 1 ? '先生' : '女士' }}</span>
                        </div>
                        <div class="info-item">
                            <i class="fa fa-phone"></i>
                            <span>{{ orderDetail.contactTel }}</span>
                        </div>
                    </div>
                    
                    <div class="section-header">
                        <h3>订单总金额</h3>
                        <span class="total-amount">¥{{ originalOrderTotal.toFixed(2) }}</span>
                    </div>

                    <!-- 积分抵扣 - 启用/有积分且可抵扣 -->
                    <div class="section-header points-deduct-section" 
                            v-if="maxDeductibleAmount > 0 && availablePoints > 0"
                            @click="usePoints = !usePoints"> 
                        
                        <div class="points-info">
                            <h3 class="points-label">
                                积分抵扣
                                <span class="points-available">({{ availablePoints }} 积分可用)</span>
                            </h3>
                            <span class="discount-amount" :class="{ disabled: !usePoints }">
                                总优惠 ¥{{ actualDiscount.toFixed(2) }} 
                            </span>
                        </div>
                        
                        <i class="fa fa-check-circle" :class="{ active: usePoints }"></i>
                    </div>

                    <!-- 积分抵扣 - 禁用/积分不足 (新增的逻辑) -->
                    <div class="section-header points-deduct-section static-deduct-section"
                         v-else-if="availablePoints === 0"> 
                        
                        <div class="points-info">
                            <h3 class="points-label disabled-label">
                                积分抵扣
                                <span class="points-unavailable">(0 积分)</span>
                            </h3>
                        </div>
						<br>
                        <span class="discount-message text-disabled">您的积分不足，无法进行抵扣</span>
						<br>
                    </div>


                    <div class="section-header" @click="detailetShow">
                        <h3>订单详情</h3>
                        <i class="fa fa-angle-down" :class="{ rotate: isShowDetailet }"></i>
                    </div>

                    <div class="merchant-details" v-show="isShowDetailet">
                        <div class="merchant-info">
                            <img :src="orderDetail.businessImg" :alt="orderDetail.businessName" class="merchant-logo">
                            <div class="merchant-name">
                                {{ orderDetail.businessName || '未知商家' }}
                            </div>
                        </div>

                        <div class="order-details">
                            <template v-if="orderDetail.foodList && orderDetail.foodList.length > 0">
                                <div class="detail-item" v-for="item in orderDetail.foodList" :key="item.id">
                                    <span class="item-name">{{ item.foodName || '未知商品' }} ¥{{ item.foodPrice }} &nbsp; × {{ item.quantity || 0 }}</span>
                                    <span class="item-price">¥{{ (item.foodPrice * item.quantity).toFixed(2) }}</span>
                                </div>
                            </template>
                            <div class="detail-item delivery-fee">
                                <span>配送费</span>
                                <span>¥{{ orderDetail.deliveryPrice.toFixed(2) || '0.00' }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="section payment-section">
                    <h3>选择支付方式</h3>
                    <div class="payment-options">
                        <div class="payment-option" :class="{ active: selectedPayment === 'wallet' }"
                            @click="selectPayment('wallet')">
                            <div class="wallet-icon">
                                <i class="fa fa-wallet"></i>&nbsp;&nbsp;
                                <span>虚拟钱包</span>
                            </div>
                            <i class="fa fa-check-circle"></i>
                        </div>
                        <div class="payment-option" :class="{ active: selectedPayment === 'alipay' }"
                            @click="selectPayment('alipay')">
                            <img src="/trade-assets/alipay.png" alt="支付宝支付" onerror="this.onerror=null;this.src='https://placehold.co/100x40/0097FF/ffffff?text=Alipay';">
                            <i class="fa fa-check-circle"></i>
                        </div>
                        <div class="payment-option" :class="{ active: selectedPayment === 'wechat' }"
                            @click="selectPayment('wechat')">
                            <img src="/trade-assets/wechat.png" alt="微信支付" onerror="this.onerror=null;this.src='https://placehold.co/100x40/38CA73/ffffff?text=WeChat';">
                            <i class="fa fa-check-circle"></i>
                        </div>
                    </div>
                </div>

                <div class="payment-action">
                    <button class="pay-button" @click="handlePayment" :disabled="paying">
                        <span v-if="paying">支付中...</span>
                        <span v-else>确认支付 ¥{{ finalPaymentAmount }}</span>
                    </button>
                </div>
            </div>
        </template>

        <div v-if="showWalletCreateModal" class="modal-overlay" @click.self="showWalletCreateModal = false">
            <div class="modal-content wallet-modal">
                <div class="modal-header">
                    <h3>开通虚拟钱包</h3>
                    <i class="fa fa-times close-btn" @click="showWalletCreateModal = false"></i>
                </div>
                <div class="modal-body">
                    <p class="wallet-tip">您还没有虚拟钱包账户，是否现在开通？</p>
                    <p class="wallet-desc">开通后即可使用钱包余额进行支付，享受便捷的支付体验。</p>
                </div>
                <div class="modal-footer">
                    <button class="cancel-btn" @click="showWalletCreateModal = false">取消</button>
                    <button class="confirm-btn" @click="handleCreateWallet" :disabled="creatingWallet">
                        <span v-if="creatingWallet">开通中...</span>
                        <span v-else>确认开通</span>
                    </button>
                </div>
            </div>
        </div>

        <div v-if="showOverdraftConfirmModal" class="modal-overlay" @click.self="showOverdraftConfirmModal = false">
            <div class="modal-content overdraft-modal">
                <div class="modal-header">
                    <h3>余额不足提示</h3>
                    <i class="fa fa-times close-btn" @click="showOverdraftConfirmModal = false"></i>
                </div>
                <div class="modal-body">
                    <div class="overdraft-info">
                        <i class="fa fa-exclamation-triangle warning-icon"></i>
                        <p class="overdraft-tip">钱包余额不足，可能需要透支</p>
                        <div class="balance-details">
                            <div class="balance-item">
                                <span class="label">当前余额：</span>
                                <span class="value">¥{{ walletInfo?.balance?.toFixed(2) || '0.00' }}</span>
                            </div>
                            <div class="balance-item">
                                <span class="label">订单金额：</span>
                                <span class="value amount">¥{{ finalPaymentAmount }}</span>
                            </div>
                            <div class="balance-item">
                                <span class="label">透支额度：</span>
                                <span class="value">¥{{ walletInfo?.overdraftLimit?.toFixed(2) || '0.00' }}</span>
                            </div>
                            <div class="balance-item">
                                <span class="label">已透支：</span>
                                <span class="value">¥{{ walletInfo?.overdrawnAmount?.toFixed(2) || '0.00' }}</span>
                            </div>
                        </div>
                        <p class="overdraft-desc">继续支付将使用透支功能，可能会产生额外费用</p>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="cancel-btn" @click="cancelOverdraftPayment">取消支付</button>
                    <button class="confirm-btn" @click="confirmOverdraftPayment" :disabled="paying">
                        <span v-if="paying">支付中...</span>
                        <span v-else>确认支付</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/trade/utils/tradeRequest';
import { toast } from '@/trade/utils/toast';
import BackButton from '../components/BackButton.vue';

// --- 【常量】 ---
const POINT_DEDUCTION_KEY = 'usePointsDeduction'; // 用于 sessionStorage 的 Key

// --- 【积分逻辑相关的 API 方法】 ---
const fetchPointsAccount = async () => {
    console.log('[DEBUG] 正在请求用户可用积分...');
    try {
        const res = await request.get('/api/points/account');
        if (res.success && res.data) {
            console.log(`[DEBUG] 积分账户返回: ${res.data.availablePoints} 分`);
            return res.data.availablePoints || 0;
        }
    } catch (error) {
        console.error('[ERROR] 查询积分账户失败:', error);
    }
    return 0;
};

const calculateDeductibleAmount = async (orderAmount) => {
    if (!orderAmount || orderAmount <= 0) {
        return 0;
    }
    console.log(`[DEBUG] 正在请求计算订单金额 ¥${orderAmount.toFixed(2)} 的最大可抵扣金额...`);
    try {
        const res = await request.get('/api/points/deductible-amount', {
            params: { orderAmount: orderAmount }
        });
        
        if (res.success && typeof res.data === 'number') {
            const deductible = Math.min(res.data, orderAmount);
            console.log(`[DEBUG] 最大可抵扣金额返回: ¥${deductible.toFixed(2)}`);
            return deductible;
        }
    } catch (error) {
        console.error('[ERROR] 计算可抵扣金额失败:', error);
    }
    return 0;
};
// --- 【积分逻辑相关的 API 方法 结束】 ---

const orderDetail = ref(null);
const isShowDetailet = ref(true);
const route = useRoute();
const router = useRouter();
const orderId = ref();
const loading = ref(true);
const selectedPayment = ref('wallet'); 
const paying = ref(false);
const showWalletCreateModal = ref(false);
const creatingWallet = ref(false);
const walletExists = ref(false);
const showOverdraftConfirmModal = ref(false);
const walletInfo = ref(null);

// --- 【积分相关状态】 ---
const availablePoints = ref(0);
const maxDeductibleAmount = ref(0);
const usePoints = ref(true); 
// --- 【积分相关状态 结束】 ---


// --- 【计算属性 - 订单金额】 ---
const originalOrderTotal = computed(() => {
    return orderDetail.value?.orderTotal || 0;
});

const actualDiscount = computed(() => {
    if (!orderDetail.value || !availablePoints.value) return 0;
    const discount = usePoints.value ? maxDeductibleAmount.value : 0;
    console.log(`[DEBUG] 计算属性: 实际优惠金额更新为 ¥${discount.toFixed(2)} (使用积分: ${usePoints.value})`);
    return discount;
});

const finalPaymentAmount = computed(() => {
    const amount = originalOrderTotal.value - actualDiscount.value;
    const finalAmount = Math.max(0, amount).toFixed(2);
    console.log(`[DEBUG] 计算属性: 最终支付金额更新为 ¥${finalAmount}`);
    return finalAmount;
});

// --- 【计算属性 结束】 ---


// --- 【积分数据初始化逻辑】 ---
const initPointsLogic = async (orderTotal) => {
    console.log(`[DEBUG] 开始初始化积分逻辑，订单总额: ¥${orderTotal.toFixed(2)}`);
    if (!orderTotal || orderTotal <= 0) {
        loading.value = false;
        return;
    }

    const points = await fetchPointsAccount();
    availablePoints.value = points;

    if (points > 0) {
        const deductible = await calculateDeductibleAmount(orderTotal);
        maxDeductibleAmount.value = deductible;
        // 初始状态下，如果可以抵扣，则默认启用
        usePoints.value = deductible > 0;
    } else {
        maxDeductibleAmount.value = 0;
        usePoints.value = false;
    }
    loading.value = false; 
    console.log('[DEBUG] 积分逻辑初始化完成。');
};
// --- 【积分数据初始化逻辑 结束】 ---

// 获取订单详情
const fetchOrderDetails = async () => {
    console.log(`[DEBUG] 正在获取订单详情, OrderID: ${orderId.value}`);
    try {
        const response = await request.get("/api/orders/detail", {
            params: { orderId: orderId.value }
        });
        
        if (response && response.success) {
            console.log('[DEBUG] 订单详情获取成功。');
            orderDetail.value = response.data;
            await initPointsLogic(response.data.orderTotal); 
        } else {
            console.error('[ERROR] 获取订单详情失败:', response.data?.message);
            toast.error("获取订单信息失败，请重试！");
            router.push({ path: '/trade/userAddress' });
        }
    } catch (error) {
        console.error('[ERROR] 请求订单详情失败:', error);
        toast.error("获取订单信息失败，请重试！");
        router.push({ path: '/trade/userAddress' });
    } 
};

// 检查钱包是否存在（后端）
const checkWalletExists = async () => {
    console.log('[DEBUG] 正在检查钱包是否存在...');
    try {
        const response = await request.get("/api/wallet/message");
        if (response && response.success && response.data) {
            console.log('[DEBUG] 钱包存在。');
            walletExists.value = true;
            walletInfo.value = response.data; 
            return true;
        }
        if (response && response.code === 'VIRTUAL_WALLET_MISSED') {
            console.log('[DEBUG] 钱包不存在（需开通）。');
            walletExists.value = false;
            return false;
        }
        console.log('[DEBUG] 钱包检查失败或返回未知代码。');
        walletExists.value = false;
        return false;
    } catch (error) {
        console.error('[ERROR] 检查钱包失败:', error);
        walletExists.value = false;
        return false;
    }
};

// 获取钱包余额信息
const fetchWalletBalance = async () => {
    console.log('[DEBUG] 正在获取钱包余额信息...');
    try {
        const response = await request.get("/api/wallet/message");
        if (response && response.success) {
            walletInfo.value = response.data;
            console.log(`[DEBUG] 钱包余额获取成功: ¥${walletInfo.value.balance.toFixed(2)}`);
            return response.data;
        }
        return null;
    } catch (error) {
        console.error('[ERROR] 获取钱包余额失败:', error);
        return null;
    }
};

// 创建钱包（后端）
const handleCreateWallet = async () => {
    console.log('[ACTION] 尝试开通虚拟钱包...');
    try {
        creatingWallet.value = true;
        const response = await request.get("/api/wallet/open");
        if (response && response.success) {
            console.log('[SUCCESS] 钱包开通成功！');
            walletExists.value = true;
            showWalletCreateModal.value = false;
            toast.success('钱包开通成功');
            await fetchWalletBalance();
        } else {
            console.error('[FAIL] 钱包开通失败:', response?.message);
            toast.error("钱包开通失败：" + (response?.message || '未知错误'));
        }
    } catch (error) {
        console.error('[ERROR] 开通钱包失败:', error);
        toast.error("钱包开通失败，请重试");
    } finally {
        creatingWallet.value = false;
    }
};

/**
 * 钱包支付（后端）
 * 成功后同时存储积分使用状态到 sessionStorage
 */
const performWalletPayment = async (forcePay = false) => {
    console.log(`[ACTION] 钱包支付发起。订单ID: ${orderId.value}, 最终金额: ¥${finalPaymentAmount.value}, 抵扣金额: ¥${actualDiscount.value}, 强制支付: ${forcePay}`);
    try {
        paying.value = true;
        
        const response = await request.get("/api/wallet/transaction/payment", {
            params: { 
                orderId: orderId.value,
                finalAmount: finalPaymentAmount.value, 
                pointsDeduction: actualDiscount.value, 
            }
        });

        if (response && response.success) {
            console.log('[SUCCESS] 钱包支付成功！');
            // 存储积分使用状态到 sessionStorage，作为辅助信息
            sessionStorage.setItem(POINT_DEDUCTION_KEY, String(usePoints.value)); 
            console.log(`[INFO] SessionStorage 写入积分状态: ${usePoints.value}`);
            
            router.push({
                path: '/trade/successfulPayment',
                query: { orderId: orderId.value }
            });
        } else {
            console.error('[FAIL] 钱包支付失败:', response?.message);
            // 检查是否是余额不足/需要透支的错误码
            if (response?.code === 'VIRTUAL_WALLET_OVERDRAFT_LIMIT') {
                if (!forcePay) {
                    // 弹出透支确认框，不Toast错误
                    showOverdraftConfirmModal.value = true;
                    return; // 暂停流程，等待用户确认
                } else {
                    toast.error("透支支付失败：" + (response?.message || '超出透支额度'));
                }
            } else {
                toast.error("钱包支付失败：" + (response?.message || '余额不足或账户异常'));
            }
        }
    } catch (error) {
        console.error('[ERROR] 钱包支付请求失败:', error);
        toast.error("钱包支付失败，请重试！");
    } finally {
        // 如果没有弹出透支框，则设置paying为false
        if (!showOverdraftConfirmModal.value) {
            paying.value = false;
        }
    }
};

// 检查余额是否足够 (此函数在实际的后端透支判断后可能不再是主要的，但保留其逻辑完整性)
const checkBalanceSufficient = () => {
    if (!walletInfo.value || !orderDetail.value) {
        return false;
    }
    const balance = walletInfo.value.balance || 0;
    const finalAmount = parseFloat(finalPaymentAmount.value);
    const result = balance >= finalAmount;
    console.log(`[DEBUG] 余额检查: 余额 ¥${balance.toFixed(2)}, 最终支付 ¥${finalAmount.toFixed(2)}。结果: ${result ? '足够' : '不足'}`);
    return result;
};

// 处理钱包支付前的余额检查
const handleWalletPayment = async () => {
    const walletData = await fetchWalletBalance();
    if (!walletData) {
        toast.error("获取钱包信息失败");
        return;
    }

    // 直接发起支付，让后端来处理余额不足和透支逻辑
    await performWalletPayment();
};

// 确认透支支付
const confirmOverdraftPayment = async () => {
    showOverdraftConfirmModal.value = false;
    console.log('[ACTION] 用户确认透支支付。');
    // 再次调用支付函数，但这次是在用户确认后的流程，无需再次弹框
    await performWalletPayment(true);
};

// 取消透支支付
const cancelOverdraftPayment = () => {
    showOverdraftConfirmModal.value = false;
    paying.value = false;
    console.log('[ACTION] 用户取消透支支付。');
    toast.info("已取消支付");
};

/**
 * 支付处理
 */
const handlePayment = async () => {
    if (paying.value) return;

    console.log(`[ACTION] 用户点击支付按钮。选择支付方式: ${selectedPayment.value}`);

    if (selectedPayment.value === 'wallet') {
        const exists = await checkWalletExists();
        if (!exists) { 
            console.log('[INFO] 钱包不存在，弹出开通模态框。');
            showWalletCreateModal.value = true; 
            return; 
        }
        // 直接进入钱包支付流程，由其内部处理余额/透支问题
        await handleWalletPayment(); 
    } else {
        // 第三方支付逻辑
        try {
            paying.value = true;
            console.log(`[ACTION] 第三方支付发起（模拟）。订单ID: ${orderId.value}, 最终金额: ¥${finalPaymentAmount.value}, 抵扣金额: ¥${actualDiscount.value}`);
            
            // 构造包含 orderState=1, orderId, usePoints 的查询字符串
            const queryParams = new URLSearchParams();
            queryParams.append('orderState', 1);
            queryParams.append('orderId', orderId.value);
            // 只有当明确不使用积分时，才需要传递 usePoints=false
            if (!usePoints.value) {
                queryParams.append('usePoints', false); 
            }
            
            const url = `/api/orders/status?${queryParams.toString()}`;
            console.log(`[DEBUG] 第三方支付请求URL: ${url}`);

            // 根据 API 文档，仅传递 orderState 和 usePoints，移除 Body
            const response = await request.put(url, {}); 

            if (response && response.success) {
                console.log('[SUCCESS] 第三方支付（模拟）成功！');
                sessionStorage.setItem(POINT_DEDUCTION_KEY, String(usePoints.value));
                console.log(`[INFO] SessionStorage 写入积分状态: ${usePoints.value}`);

                router.push({
                    path: '/trade/successfulPayment',
                    query: { orderId: orderId.value }
                });
            } else {
                console.error('[FAIL] 第三方支付（模拟）失败:', response.data?.message);
                toast.error("支付失败" + (response.data?.message || '未知错误'));
            }
        } catch (error) {
            console.error('[ERROR] 第三方支付请求失败:', error);
            toast.error("支付失败，请重试！");
        } finally {
            paying.value = false;
        }
    }
};

const selectPayment = (type) => {
    selectedPayment.value = type;
    console.log(`[ACTION] 支付方式切换为: ${type}`);
};

const detailetShow = () => {
    isShowDetailet.value = !isShowDetailet.value;
    console.log(`[INFO] 订单详情显示状态切换为: ${isShowDetailet.value}`);
};

onMounted(() => {
    orderId.value = route.query.orderId;
    console.log(`[LIFECYCLE] Payment 页面挂载，从 URL 获取 OrderID: ${orderId.value}`);
    fetchOrderDetails();
    checkWalletExists();
});

// 变量和函数自动暴露给模板
</script>
 
<style scoped>
/****************** 总容器 ******************/
.wrapper {
    min-height: 100vh;
    background-color: #f5f7fa;
}

/****************** header部分 ******************/
.wrapper header {
    width: 100%;
    height: 12vw;
    background-color: #0097FF;
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

.content {
    padding-top: 20vw;
    padding-bottom: 32vw;
}

.section {
    background: white;
    border-radius: 3vw;
    margin: 3vw;
    padding: 4vw;
    box-shadow: 0 0.2vw 1vw rgba(0, 0, 0, 0.05);
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4vw;
}

.section-header h3 {
    font-size: 4.2vw;
    color: #333;
    font-weight: 500;
    margin: 0;
}

.total-amount {
    font-size: 5vw;
    color: #ff6b00;
    font-weight: bold;
}

/* 配送信息样式 */
.delivery-info {
    margin-bottom: 4vw;
    padding-bottom: 3vw;
    border-bottom: 1px solid #f0f0f0;
}

.info-item {
    display: flex;
    align-items: center;
    margin-bottom: 2vw;
    font-size: 3.6vw;
    color: #666;
}

.info-item i {
    margin-right: 2vw;
    color: #0097FF;
    width: 5vw;
    text-align: center;
}

.merchant-info {
    display: flex;
    align-items: center;
    padding: 3vw 0;
    cursor: pointer;
}

.merchant-logo {
    width: 12vw;
    height: 12vw;
    border-radius: 2vw;
    object-fit: cover;
    margin-right: 3vw;
}

.merchant-name {
    flex: 1;
    font-size: 4vw;
    color: #333;
    display: flex;
    align-items: center;
    gap: 2vw;
}

.fa-angle-down {
    transition: transform 0.3s ease;
}

.fa-angle-down.rotate {
    transform: rotate(180deg);
}

.order-details {
    margin-top: 3vw;
    padding-top: 3vw;
    border-top: 0.2vw solid #f5f7fa;
}

.detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 2vw 0;
    font-size: 3.6vw;
    color: #666;
}

.delivery-fee {
    border-top: 0.2vw dashed #eee;
    margin-top: 2vw;
    padding-top: 2vw;
    color: #333;
    font-weight: bold;
}

.payment-options {
    display: flex;
    gap: 10vw;
    margin-top: 4vw;
    display: inline-block;
}

.payment-option {
    flex: 1;
    padding: 4vw;
    border: 0.2vw solid #eee;
    border-radius: 2vw;
    display: flex;
    align-items: center;
    justify-content: space-between;
    cursor: pointer;
    transition: all 0.3s ease;
    background: #f9f9f9;
    width: 80vw;
}

.payment-option img {
    height: 8vw;
    width: auto;
    object-fit: contain;
}

.payment-option .wallet-icon {
    display: flex;
    align-items: center;
    gap: 2vw;
}

.payment-option .wallet-icon i {
    font-size: 6vw;
    color: #0097FF;
}

.payment-option .wallet-icon span {
    font-size: 3.6vw;
    color: #333;
    font-weight: 500;
}

.payment-option .fa-check-circle {
    font-size: 5vw;
    color: #ddd;
    transition: all 0.3s ease;
}

.payment-option.active {
    border-color: #38CA73;
    background: #f0fff5;
}

.payment-option.active .fa-check-circle {
    color: #38CA73;
}

.payment-action {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 4vw;
    background: white;
    box-shadow: 0 -0.2vw 1vw rgba(0, 0, 0, 0.05);
}

.pay-button {
    width: 100%;
    height: 12vw;
    border: none;
    border-radius: 6vw;
    background: linear-gradient(to right, #38CA73, #2EAF62);
    color: white;
    font-size: 4.2vw;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
}

.pay-button:disabled {
    background: #ccc;
    cursor: not-allowed;
}

.pay-button:not(:disabled):active {
    transform: scale(0.98);
}

.loading {
    width: 100%;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 4vw;
    color: #666;
}
/* 1. 给 BackButton 父容器加固定定位，与 header 对齐 */
.back-btn-container {
    position: fixed; /* 固定定位，不随滚动移动 */
    left: 0vw; /* 距离左侧的距离，可根据需求调整 */
    top: 0vw; /* 距离顶部的距离，与 header 高度（12vw）适配，确保垂直居中 */
    z-index: 1001; /* 比 header 的 z-index:1000 高，避免被遮挡 */
}

/* 钱包开通对话框样式 */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 2000;
}

.wallet-modal {
    background: white;
    border-radius: 4vw;
    width: 85%;
    max-width: 500px;
}

.wallet-modal .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4vw;
    border-bottom: 1px solid #f0f0f0;
}

.wallet-modal .modal-header h3 {
    font-size: 4.5vw;
    margin: 0;
    color: #333;
}

.wallet-modal .close-btn {
    font-size: 5vw;
    color: #999;
    cursor: pointer;
}

.wallet-modal .modal-body {
    padding: 4vw;
}

.wallet-tip {
    font-size: 4vw;
    color: #333;
    margin-bottom: 3vw;
    text-align: center;
}

.wallet-desc {
    font-size: 3.6vw;
    color: #666;
    line-height: 1.6;
    text-align: center;
}

.wallet-modal .modal-footer {
    display: flex;
    gap: 3vw;
    padding: 4vw;
    border-top: 1px solid #f0f0f0;
}

.wallet-modal .modal-footer button {
    flex: 1;
    padding: 3.5vw;
    border: none;
    border-radius: 2vw;
    font-size: 4vw;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
}

.wallet-modal .cancel-btn {
    background: #f5f5f5;
    color: #666;
}

.wallet-modal .cancel-btn:active {
    background: #e0e0e0;
}

.wallet-modal .confirm-btn {
    background: #0097FF;
    color: white;
}

.wallet-modal .confirm-btn:active {
    background: #0080e6;
}

.wallet-modal .confirm-btn:disabled {
    background: #ccc;
    cursor: not-allowed;
}

/* 透支确认对话框样式 */
.overdraft-modal {
    background: white;
    border-radius: 4vw;
    width: 85%;
    max-width: 500px;
}

.overdraft-modal .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4vw;
    border-bottom: 1px solid #f0f0f0;
}

.overdraft-modal .modal-header h3 {
    font-size: 4.5vw;
    margin: 0;
    color: #333;
}

.overdraft-modal .close-btn {
    font-size: 5vw;
    color: #999;
    cursor: pointer;
}

.overdraft-modal .modal-body {
    padding: 4vw;
}

.overdraft-info {
    text-align: center;
}

.warning-icon {
    font-size: 10vw;
    color: #faad14;
    margin-bottom: 3vw;
}

.overdraft-tip {
    font-size: 4.2vw;
    color: #333;
    font-weight: 500;
    margin-bottom: 4vw;
}

.balance-details {
    background: #f8f9fa;
    border-radius: 2vw;
    padding: 3vw;
    margin-bottom: 3vw;
}

.balance-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 2vw 0;
    font-size: 3.6vw;
    color: #666;
    border-bottom: 1px solid #f0f0f0;
}

.balance-item:last-child {
    border-bottom: none;
}

.balance-item .label {
    color: #999;
}

.balance-item .value {
    color: #333;
    font-weight: 500;
}

.balance-item .value.amount {
    color: #ff4d4f;
    font-weight: bold;
}

.overdraft-desc {
    font-size: 3.4vw;
    color: #faad14;
    line-height: 1.6;
    text-align: center;
}

.overdraft-modal .modal-footer {
    display: flex;
    gap: 3vw;
    padding: 4vw;
    border-top: 1px solid #f0f0f0;
}

.overdraft-modal .modal-footer button {
    flex: 1;
    padding: 3.5vw;
    border: none;
    border-radius: 2vw;
    font-size: 4vw;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;
}

.overdraft-modal .cancel-btn {
    background: #f5f5f5;
    color: #666;
}

.overdraft-modal .cancel-btn:active {
    background: #e0e0e0;
}

.overdraft-modal .confirm-btn {
    background: #ff4d4f;
    color: white;
}

.overdraft-modal .confirm-btn:active {
    background: #d9363e;
}

.overdraft-modal .confirm-btn:disabled {
    background: #ccc;
    cursor: not-allowed;
}


/* 【积分抵扣区域样式】 */
.points-deduct-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 3vw 0;
    border-top: 1px solid #f0f0f0;
    margin-bottom: 2vw; 
    cursor: pointer; 
}

/* 禁用的积分抵扣区域，移除点击效果 */
.points-deduct-section.static-deduct-section {
    cursor: default;
}

.points-info {
    display: flex;
    align-items: center;
    flex-grow: 1;
}

.points-label {
    font-size: 3.8vw;
    color: #0097FF; 
    font-weight: 500;
    margin: 0;
}

/* 积分标签禁用时的颜色 */
.points-label.disabled-label {
    color: #999 !important;
}

.points-available {
    font-size: 3.2vw;
    color: #999;
    margin-left: 2vw;
}

/* 可用积分为 0 时的颜色 */
.points-unavailable {
    color: #ccc !important;
}

.discount-amount {
    font-size: 4.2vw;
    color: #FF6B00; 
    font-weight: bold;
    margin-left: auto; 
    margin-right: 4vw;
    transition: color 0.3s ease;
    white-space: nowrap;
}

.discount-amount.disabled {
    color: #ccc;
    text-decoration: line-through;
}

/* 积分不足时的提示信息样式 */
.discount-message.text-disabled {
    font-size: 3.8vw;
    color: #999; 
    font-weight: 500;
    white-space: nowrap;
}

/* 最终支付金额/合计区域样式 */
.final-amount-section {
    padding-top: 0;
    margin-top: 0;
    margin-bottom: 4vw; 
    border-top: none; 
}

.final-label {
    font-size: 4.5vw;
    color: #333;
    font-weight: bold;
    margin: 0;
}

.final-amount {
    font-size: 6vw;
    color: #E51C23; 
    font-weight: bold;
}

/* 【积分抵扣勾选图标样式】 */
.points-deduct-section .fa-check-circle {
    font-size: 5vw;
    color: #ddd; 
    transition: all 0.3s ease;
}

.points-deduct-section .fa-check-circle.active {
    color: #38CA73; 
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