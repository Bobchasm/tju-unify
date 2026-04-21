<template>
    <div class="container">
        <div class="fixed-top">
            <div class="top-background">
                <h1>个人信息</h1>
            </div>
        </div>

        <div class="user-card">
            <div class="avatar" @click="triggerFileInput">
                <img :src="userInfo?.photo || '/trade-assets/default-avatar.png'" alt="用户头像">
                <div class="avatar-overlay">
                    <i class="fas fa-camera"></i>
                    <span>更换头像</span>
                </div>
            </div>
            <div class="user-details">
                <div class="user-name">
                    {{ userInfo?.username || '未设置昵称' }}
                    <i class="fas fa-pencil-alt edit-icon" @click="openEditModal"></i>
                </div>
                <div class="user-full-name">
                    <i class="fas fa-id-card-alt full-name-icon"></i>
                    <span class="first-name">{{ userInfo?.firstName || '未设置姓氏' }}</span>
                    <span class="last-name">{{ userInfo?.lastName || '未设置名字' }}</span>
                </div>
                <div class="user-phone">
                    <i class="fas fa-phone phone-icon"></i>
                    <span>{{ userInfo?.phone || '未设置手机号' }}</span>
                </div>
                <div class="user-email">
                    <i class="fas fa-envelope-open-text email-icon"></i>
                    <span>{{ userInfo?.email || '未设置邮箱' }}</span>
                </div>
                </div>
            <div class="card-button-section">
                <button class="logout-btn" @click="logout">
                    <i class="fas fa-sign-out-alt"></i>退出登录
                </button>
            </div>
        </div>

        <input type="file" ref="fileInput" style="display: none" accept="image/*" @change="handleFileUpload">

        <div class="menu-section">
            <div class="section-title">常用功能</div>
            <div class="menu-list">
                <div class="menu-item" @click="showAddressSection = !showAddressSection">
                    <div class="menu-icon">
                        <i class="fas fa-map-marker-alt"></i>
                    </div>
                    <span class="menu-text">收货地址</span>
                    <i class="fas fa-chevron-right menu-arrow"></i>
                </div>
                <div class="menu-item" @click="myfavorite">
                    <div class="menu-icon">
                        <i class="fas fa-heart"></i>
                    </div>
                    <span class="menu-text">我的收藏</span>
                    <i class="fas fa-chevron-right menu-arrow"></i>
                </div>
                <div class="menu-item message-item" @click="navigateTo('notifications')">
                    <div class="menu-icon">
                        <i class="fas fa-bell"></i>
                    </div>
                    <span class="menu-text">消息与通知</span>
                    <div class="notification-badge" v-if="unreadMessageCount > 0">
                        {{ unreadMessageCount }}
                    </div>
                    <i class="fas fa-chevron-right menu-arrow"></i>
                </div>
            </div>
        </div>

        <div v-if="uploading" class="upload-loading">
            <i class="fas fa-spinner fa-spin"></i> 上传中...
        </div>

        <AddressManager v-if="showAddressSection" :id="userInfo?.id" class="address-manager" />

        <div class="loading" v-if="loading">
            <i class="fas fa-spinner fa-spin"></i> 加载中...
        </div>

        <div class="error-message" v-if="errorMessage">
            <i class="fas fa-exclamation-circle"></i> {{ errorMessage }}
        </div>

        <Footer />

        <div v-if="showEditModal" class="modal-overlay">
            <div class="modal-content">
                <h3>编辑个人信息</h3>
                <div class="modal-item">
                    <label>姓氏</label>
                    <input v-model="editFormData.firstName" placeholder="输入姓氏" />
                </div>
                <div class="modal-item">
                    <label>名字</label>
                    <input v-model="editFormData.lastName" placeholder="输入名字" />
                </div>
                <div class="modal-item">
                    <label>手机号</label>
                    <input v-model="editFormData.phone" placeholder="输入手机号" />
                </div>
                <div class="modal-item">
                    <label>邮箱</label>
                    <input v-model="editFormData.email" placeholder="输入邮箱" type="email" />
                </div>
                <div class="modal-buttons">
                    <button @click="submitEdits">提交</button>
                    <button @click="closeEditModal">取消</button>
                </div>
            </div>
        </div>

        <div v-if="showMerchantApplyModal" class="modal-overlay">
            <div class="modal-content merchant-apply-modal">
                <div class="modal-icon">
                    <i class="fas fa-store"></i>
                </div>
                <h3>申请成为商家</h3>
                <p class="modal-message">当前无商家权限，是否申请成为商家？</p>
                <div class="modal-buttons">
                    <button class="apply-btn" @click="applyForMerchant">申请</button>
                    <button class="cancel-btn" @click="closeMerchantApplyModal">否</button>
                </div>
            </div>
        </div>

        <div v-if="showWalletActivateModal" class="modal-overlay">
            <div class="modal-content wallet-activate-modal">
                <div class="modal-icon">
                    <i class="fas fa-wallet"></i>
                </div>
                <h3>开通虚拟钱包</h3>
                <p class="modal-message">您还没有虚拟钱包账户，是否现在开通？</p>
                <div class="modal-buttons">
                    <button class="apply-btn" @click="activateWallet">确认开通</button>
                    <button class="cancel-btn" @click="showWalletActivateModal = false">取消</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import Footer from '../components/Footer.vue';
import AddressManager from '../components/AddressManager.vue';
import request from '@/trade/utils/tradeRequest';
import { useRouter } from 'vue-router';
import { toast } from '@/trade/utils/toast';

export default {
    name: 'MyApplication',
    components: {
        Footer,
        AddressManager
    },
    setup() {
        const router = useRouter();
        const userInfo = ref({});
        const loading = ref(false);
        const errorMessage = ref('');
        const showEditModal = ref(false);
        const showMerchantApplyModal = ref(false);
        const showWalletActivateModal = ref(false);

        const getCurrentUser = () => {
            try {
                const localUser = localStorage.getItem('userInfo');
                if (localUser) return JSON.parse(localUser);
                const sessionUser = sessionStorage.getItem('userInfo');
                if (sessionUser) return JSON.parse(sessionUser);
            } catch (error) {
                console.error('解析用户信息失败:', error);
            }
            return null;
        };

        const getWalletInfoKey = () => {
            const user = getCurrentUser();
            return user ? `walletInfo_${user.id}` : 'walletInfo_guest';
        };

        const getWalletTransactionsKey = () => {
            const user = getCurrentUser();
            return user ? `walletTransactions_${user.id}` : 'walletTransactions_guest';
        };
        const showAddressSection = ref(false);
        const unreadMessageCount = ref(0);
        const uploading = ref(false);
        const fileInput = ref(null);
        const webSocket = ref(null);
        const isConnected = ref(false);

        const editFormData = ref({
            firstName: '',
            lastName: '',
            phone: '',
            email: ''
        });

        const getToken = () => {
            return localStorage.getItem('token') || sessionStorage.getItem('token');
        };

        const formattedPhone = computed(() => {
            if (!userInfo.value.phone) return '未绑定手机';
            return userInfo.value.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
        });

        const initWebSocket = () => {
            // 清除之前的连接
            if (webSocket.value) {
                webSocket.value.close();
            }

            try {
                // 使用userId作为WebSocket连接标识
                const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
                const wsUrl = `${wsProtocol}//localhost:8086/ws/${userInfo.value.id}`;

                webSocket.value = new WebSocket(wsUrl);

                webSocket.value.onopen = () => {
                    console.log('WebSocket 连接成功');
                    isConnected.value = true;
                    //toast.success('已连接消息通知');
                };

                webSocket.value.onmessage = (event) => {
                    try {
                        const message = JSON.parse(event.data);
                        handleNewMessage(message);
                    } catch (err) {
                        console.error('解析消息失败:', err);
                    }
                };

                webSocket.value.onclose = (event) => {
                    console.log('WebSocket 连接关闭，代码:', event.code);
                    isConnected.value = false;
                    if (event.code !== 1000) {
                        setTimeout(initWebSocket, 3000);
                    }
                };

                webSocket.value.onerror = (err) => {
                    console.error('WebSocket 错误:', err);
                    isConnected.value = false;
                };
            } catch (err) {
                console.error('初始化 WebSocket 失败:', err);
            }
        };


        onMounted(async () => {
            const token = getToken();
            if (!token) {
                toast.warning('用户未登录，请先登录！');
                router.push({ path: '/trade/login' });
                return;
            }
            initWebSocket();
            await loadUserData();
            await checkNewMessages();
        });
        onUnmounted(() => {
            if (webSocket.value) {
                webSocket.value.close();
            }
        });
        const handleNewMessage = (message) => {
            console.log('MyInformation收到WebSocket消息:', message);
            
            // 处理订单相关消息(不显示toast,由订单页面处理)
            if (message.type === 'order_update' || message.type === 'new_order') {
                console.log('订单消息,不在个人信息页面显示toast');
                return; // 订单消息由OrderList/MerchantOrders页面处理
            }
            
            // 处理钱包消息(不显示toast,由钱包页面处理)
            if (message.type === 'wallet_opened') {
                console.log('钱包消息,不在个人信息页面显示toast');
                return; // 钱包消息由Wallet页面处理
            }
            
            // 处理通知消息(有content字段的消息)
            if (message.content) {
                toast.info(`您有新消息啦`);
                if (message.content.includes('您的成为商家申请已通过审核')) {
                    if (userInfo.value.authorities && Array.isArray(userInfo.value.authorities)) {
                        const hasBusinessAuth = userInfo.value.authorities.some(auth => auth.name === 'BUSINESS');
                        if (!hasBusinessAuth) {
                            userInfo.value.authorities.push({ name: 'BUSINESS' });
                            const tokenFromLocal = localStorage.getItem('token');
                            const tokenFromSession = sessionStorage.getItem('token');
                            const storage = tokenFromLocal ? localStorage : (tokenFromSession ? sessionStorage : null);
                            storage.setItem('userInfo', JSON.stringify(userInfo.value));
                        }
                    }
                }
                setTimeout(() => {
                    checkNewMessages().catch(err => {
                        console.error('新消息触发重新检查失败:', err);
                        toast.error('新消息已收到，但加载失败');
                    });
                }, 300);
            }
        };
        const checkNewMessages = async () => {
            try {
                const token = getToken();
                if (!token) return;
                const response = await request.get(`/api/notifications?userId=${userInfo.value.id}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (response && response.success && response.data) {
                    unreadMessageCount.value = response.data.filter(
                        item => item.isDeleted === 0 && item.isRead === 0
                    ).length;
                } else {
                    unreadMessageCount.value = 0;
                }
            } catch (error) {
                console.error('检查未读消息失败:', error);
                unreadMessageCount.value = 0;
            }
        };
        const triggerFileInput = () => {
            fileInput.value.click();
        };
        const handleFileUpload = async (event) => {
            const file = event.target.files[0];
            if (!file) return;
            if (!file.type.startsWith('image/')) {
                toast.error('请选择图片文件！');
                return;
            }
            if (file.size > 5 * 1024 * 1024) {
                toast.error('图片大小不能超过5MB！');
                return;
            }
            uploading.value = true;
            try {
                const token = getToken();
                const formData = new FormData();
                formData.append('file', file);
                const uploadResponse = await request.post('/upload', formData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    }
                });
                if (uploadResponse && uploadResponse.data) {
                    const updateResponse = await request.put('/api/person/info', {
                        id: userInfo.value.id,
                        photo: uploadResponse.data
                    }, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        }
                    });
                    if (updateResponse && updateResponse.success) {
                        userInfo.value.photo = uploadResponse.data;
                        sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value));
                        toast.success('头像更新成功！');
                    } else {
                        toast.error('头像更新失败！');
                    }
                } else {
                    toast.error('图片上传失败！');
                }
            } catch (error) {
                console.error('头像上传失败:', error);
                toast.error('头像上传失败，请重试！');
            } finally {
                uploading.value = false;
                event.target.value = '';
            }
        };
        const loadUserData = async () => {
            loading.value = true;
            errorMessage.value = '';
            try {
                const token = getToken();
                if (!token) {
                    toast.warning('用户未登录，请先登录！');
                    router.push({ path: '/trade/login' });
                    return;
                }
                const response = await request.get('/api/person', {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (response.success) {
                    userInfo.value = response.data;

                    // 获取积分信息
                    try {
                        const pointsResponse = await request.get('/api/points/account', {
                            headers: {
                                'Authorization': `Bearer ${token}`
                            }
                        });

                        if (pointsResponse && pointsResponse.success) {
                            // 确保使用 data 路径获取 totalPoints
                            userInfo.value.availablePoints = pointsResponse.data.availablePoints || 0;
                            console.log('积分已更新为:', userInfo.value.totalPoints); // 👈 保留检查点
                        }

                    } catch (pointsError) {
                        console.error('获取积分信息失败:', pointsError);
                        userInfo.value.totalPoints = 0; // 设置默认值
                    }

                    sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value));
                    console.log('用户信息加载成功:', userInfo.value);
                }
            } catch (error) {
                console.error('获取用户信息失败:', error);
                if (error.response && error.response.status === 401) {
                    toast.error('登录已过期，请重新登录！');
                    localStorage.removeItem('token');
                    sessionStorage.removeItem('token');
                    sessionStorage.removeItem('userInfo');
                    router.push({ path: '/trade/login' });
                } else {
                    errorMessage.value = '获取用户信息失败，请重试！';
                    toast.error('获取用户信息失败，请重试！');
                }
            } finally {
                loading.value = false;
            }
        };
        const logout = () => {
            localStorage.removeItem('token');
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('userInfo');
            router.push({ path: '/trade' });
        };
        const switchToMerchant = async () => {
            try {
                const hasBusinessPermission = userInfo.value.authorities?.some(
                    auth => auth.name === 'BUSINESS'
                );
                if (hasBusinessPermission) {
                    router.push({ path: '/trade' });
                } else {
                    showMerchantApplyModal.value = true;
                }
            } catch (error) {
                console.error('检查商家权限失败:', error);
                toast.error('请勿重复申请');
            }
        };
        const openEditModal = () => {
            if (userInfo.value) {
                editFormData.value.firstName = userInfo.value.firstName || '';
                editFormData.value.lastName = userInfo.value.lastName || '';
                editFormData.value.phone = userInfo.value.phone || '';
                editFormData.value.email = userInfo.value.email || '';
            }
            showEditModal.value = true;
        };
        const closeEditModal = () => {
            showEditModal.value = false;
        };
        const closeMerchantApplyModal = () => {
            showMerchantApplyModal.value = false;
        };
        const applyForMerchant = async () => {
            try {
                const token = getToken();
                const response = await request.post('/api/permission/apply-merchant', {}, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (response && response.success) {
                    toast.success('申请成功，请等待管理员审核！');
                    closeMerchantApplyModal();
                } else {
                    toast.error(response.message);
                }
            } catch (error) {
                // console.error('申请成为商家失败:', error);
                toast.error(error);
            }
        };
        const submitEdits = async () => {
            if (!editFormData.value.phone) {
                toast.warning('手机号不能为空！');
                return;
            }
            try {
                const token = getToken();
                const response = await request.put('/api/person/info', {
                    id: userInfo.value.id,
                    firstName: editFormData.value.firstName,
                    lastName: editFormData.value.lastName,
                    email: editFormData.value.email,
                    phone: editFormData.value.phone,
                    photo: userInfo.value.photo
                }, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (response.success) {
                    userInfo.value.firstName = editFormData.value.firstName;
                    userInfo.value.lastName = editFormData.value.lastName;
                    userInfo.value.email = editFormData.value.email;
                    userInfo.value.phone = editFormData.value.phone;
                    sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value));
                    toast.success('个人信息修改成功！');
                    closeEditModal();
                } else {
                    toast.error('个人信息修改失败！');
                }
            } catch (error) {
                console.error(error);
                toast.error('个人信息修改失败！');
            }
        };
        const myfavorite = () => {
            router.push({ path: '/trade/favorites' });
        };
        const navigateTo = async (page) => {
            const pageRoutes = {
                'wallet': '/trade/wallet',
                'notifications': '/trade/notifications',
                'points': '/trade/points'
            };
            if (pageRoutes[page]) {
                if (page === 'wallet') {
                    // 检测钱包是否存在
                    await checkAndCreateWallet();
                } else {
                    router.push({ path: pageRoutes[page] });
                }
                if (page === 'notifications') {
                    unreadMessageCount.value = 0;
                }
            } else {
                toast.warning('功能待开发');
            }
        };

        const addTransactionRecord = (record) => {
            try {
                const transactions = JSON.parse(localStorage.getItem(getWalletTransactionsKey()) || '[]');
                const transaction = {
                    id: Date.now(),
                    transactionTime: new Date().toISOString(),
                    ...record
                };
                transactions.unshift(transaction);
                localStorage.setItem(getWalletTransactionsKey(), JSON.stringify(transactions));
            } catch (error) {
                console.error('保存交易记录失败:', error);
            }
        };

        // 检测并创建钱包（后端优先；本地模拟保留为注释）
        const checkAndCreateWallet = async () => {
            try {
                // 前端模拟备用：
                // const savedWalletInfo = localStorage.getItem(getWalletInfoKey());
                // if (savedWalletInfo) { router.push({ path: '/trade/wallet' }); return; }

                const response = await request.get('/api/wallet/message');
                if (response && response.success) {
                    router.push({ path: '/trade/wallet' });
                } else {
                    showWalletActivateModal.value = true;
                }
            } catch (error) {
                if (error.response?.status === 404) {
                    showWalletActivateModal.value = true;
                } else {
                    console.error('检查钱包失败:', error);
                    toast.error('检查钱包状态失败');
                }
            }
        };

        // 激活钱包（后端；本地模拟保留为注释）
        const activateWallet = async () => {
            try {
                // 前端模拟备用：
                // await new Promise(r => setTimeout(r, 400));
                // const newWalletInfo = { balance: 0, isVip: false, overdraftLimit: 0, usedOverdraft: 0 };
                // localStorage.setItem(getWalletInfoKey(), JSON.stringify(newWalletInfo));
                // addTransactionRecord({ transactionType: 'create', amount: 0, reason: '钱包开通成功' });
                // toast.success('钱包开通成功'); showWalletActivateModal.value = false; router.push({ path: '/trade/wallet' }); return;

                const response = await request.get('/api/wallet/open');
                if (response && response.success) {
                    toast.success('钱包开通成功');
                    showWalletActivateModal.value = false;
                    router.push({ path: '/trade/wallet' });
                } else {
                    toast.error('钱包开通失败：' + (response.message || '未知错误'));
                }
            } catch (error) {
                console.error('激活钱包失败:', error);
                toast.error('钱包开通失败，请重试');
            }
        };
        return {
            userInfo,
            formattedPhone,
            loading,
            uploading,
            errorMessage,
            showEditModal,
            showMerchantApplyModal,
            editFormData,
            fileInput,
            logout,
            openEditModal,
            closeEditModal,
            closeMerchantApplyModal,
            applyForMerchant,
            submitEdits,
            myfavorite,
            navigateTo,
            switchToMerchant,
            showAddressSection,
            triggerFileInput,
            handleFileUpload,
            unreadMessageCount,
            showWalletActivateModal,
            activateWallet
        };
    },
};
</script>

<style scoped>
/* 保持原有的样式，只修改或新增以下部分 */
.container {
    max-width: 600px;
    background: #fff;
    min-height: 100vh;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    border-radius: 16px;
    padding-bottom: 20vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
}

/****************** 固定顶部栏 ******************/
.fixed-top {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 1000;
    max-width: 600px;
    margin: 0 auto;
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
    position: relative;
    overflow: hidden;
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

@keyframes shine {
    0% {
        transform: rotate(30deg) translate(-10%, -10%);
    }

    100% {
        transform: rotate(30deg) translate(10%, 10%);
    }
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

/****************** 内容区域 ******************/
.content-area {
    margin-top: 100px;
    /* 固定顶部栏的高度 */
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.user-card {
    width: 92%;
    max-width: 500px;
    margin: 0 auto 20px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    padding: 20px 0;
    display: flex;
    align-items: center;
    gap: 20px;
    position: relative;
    z-index: 2;
    transform: translateY(-50px);
    flex-wrap: wrap;
    /* 允许元素换行 */
    justify-content: center;
    top: 21vw;
    /* 居中对齐子元素 */
}

/* 新增：卡片内的按钮区域 */
.card-button-section {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 15px;
    padding: 0 20px 20px;
    /* 增加内边距，保持与卡片一致 */
    box-sizing: border-box;
}

.avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    overflow: hidden;
    border: 3px solid white;
    box-shadow: 0 6px 20px rgba(0, 151, 255, 0.3);
    flex-shrink: 0;
    background: #f8f9fa;
    margin-left: 15px;
}

.avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 50%;
}

.user-details {
    flex: 1;
    background-color: #f8f9fa;
    padding: 15px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    border: 1px solid #e9ecef;
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-right: 15px;
}

.user-name,
.user-full-name,
.user-phone,
.user-email {
    font-size: 0.95rem;
    color: #495057;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    padding: 10px;
    display: flex;
    align-items: center;
}

.user-name {
    font-size: 1.1rem;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
}

.user-full-name .first-name {
    margin-right: 5px;
}

.user-name .edit-icon,
.user-full-name .full-name-icon,
.user-phone .phone-icon,
.user-email .email-icon {
    margin-right: 8px;
    color: #3498db;
}

.edit-icon {
    margin-left: auto;
    color: #3498db;
    font-size: 16px;
    cursor: pointer;
}

.menu-section {
    width: 92%;
    max-width: 500px;
    margin: 80px auto;
    transform: translateY(-50px);
}

.section-title {
    font-size: 1.1rem;
    color: #2c3e50;
    margin-bottom: 15px;
    padding-left: 10px;
    font-weight: 600;
    border-left: 4px solid #3498db;
}

.menu-list {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.menu-item {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: all 0.3s ease;
}

.menu-item:hover {
    background-color: #f1f8ff;
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.menu-item:last-child {
    border-bottom: none;
}

.menu-icon {
    width: 22px;
    height: 22px;
    margin-right: 15px;
    color: #3498db;
    display: flex;
    justify-content: center;
    align-items: center;
}

.menu-text {
    flex: 1;
    font-size: 0.95rem;
    color: #34495e;
    font-weight: 500;
}

.menu-arrow {
    color: #bdc3c7;
    font-size: 14px;
}

/* 移除 .button-section 样式，因为按钮已移动到 .user-card 中 */
.loading {
    text-align: center;
    padding: 15px;
    color: #3498db;
    font-size: 1rem;
    transform: translateY(-50px);
}

.error-message {
    text-align: center;
    padding: 10px;
    background: #ffecec;
    color: #e74c3c;
    border-radius: 8px;
    margin: 10px;
    font-size: 0.9rem;
    transform: translateY(-50px);
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
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
    max-width: 400px;
    width: 80%;
    box-sizing: border-box;
    text-align: center;
}

.modal-content h3 {
    margin-top: 0;
    color: #2c3e50;
    margin-bottom: 20px;
}

.modal-item {
    margin-bottom: 15px;
    text-align: left;
}

.modal-item label {
    display: block;
    font-weight: 500;
    color: #555;
    margin-bottom: 5px;
}

.modal-content input,
.modal-content textarea {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 16px;
    box-sizing: border-box;
}

.modal-buttons {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
}

.modal-buttons button {
    padding: 8px 16px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 1rem;
}

.modal-buttons button:first-child {
    background: #3498db;
    color: white;
    transition: background-color 0.3s;
}

.modal-buttons button:first-child:hover {
    background: #2980b9;
}

.modal-buttons button:last-child {
    background: #e0e0e0;
    color: #333;
    transition: background-color 0.3s;
}

.modal-buttons button:last-child:hover {
    background: #c7c7c7;
}

.merchant-apply-modal {
    text-align: center;
    max-width: 350px;
}

.modal-icon {
    font-size: 3rem;
    color: #ff6b6b;
    margin-bottom: 15px;
}

.modal-icon i {
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.1);
    }

    100% {
        transform: scale(1);
    }
}

.modal-message {
    color: #666;
    font-size: 1rem;
    margin: 15px 0 25px 0;
    line-height: 1.5;
}

.apply-btn {
    background: linear-gradient(135deg, #ff6b6b, #ff8e8e) !important;
    color: white !important;
    font-weight: 600;
    padding: 12px 24px !important;
    margin-right: 10px;
    transition: all 0.3s ease;
}

.apply-btn:hover {
    background: linear-gradient(135deg, #ff5252, #ff7979) !important;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.cancel-btn {
    background: #e0e0e0 !important;
    color: #666 !important;
    font-weight: 500;
    padding: 12px 24px !important;
    transition: all 0.3s ease;
}

.cancel-btn:hover {
    background: #d0d0d0 !important;
    transform: translateY(-2px);
}

.menu-item.message-item {
    position: relative;
}

.notification-badge {
    position: absolute;
    top: 5px;
    right: 35px;
    min-width: 18px;
    height: 18px;
    padding: 0 4px;
    background-color: #ff4d4f;
    color: white;
    border-radius: 9px;
    font-size: 12px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid white;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

@media (max-width: 480px) {

    .container,
    .user-card,
    .menu-section,
    .card-button-section {
        max-width: 100vw;
        width: 100vw;
        border-radius: 0;
        padding: 0;
    }

    .top-background {
        height: 90px;
        border-radius: 0;
    }

    .user-card {
        flex-direction: column;
        align-items: center;
        gap: 10px;
        padding: 20px 0;
        margin-top: 15vw;
        transform: translateY(-50px);
        width: 90%;
    }

    .avatar {
        width: 80px;
        height: 80px;
        margin-left: 0;
    }

    .user-details {
        width: 85%;
        padding: 10px;
        gap: 6px;
        margin-right: 0;
    }

    .menu-item {
        padding: 14px 16px;
    }

    .card-button-section {
        width: 85%;
        padding: 0;
        margin: 10px 0;
    }
}

.avatar {
    position: relative;
    cursor: pointer;
    transition: transform 0.3s ease;
}

.avatar:hover {
    transform: scale(1.05);
}

.avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    color: white;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    opacity: 0;
    transition: opacity 0.3s ease;
    border-radius: 50%;
}

.avatar:hover .avatar-overlay {
    opacity: 1;
}

.avatar-overlay i {
    font-size: 24px;
    margin-bottom: 5px;
}

.avatar-overlay span {
    font-size: 12px;
    text-align: center;
}

.upload-loading {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 20px;
    border-radius: 10px;
    z-index: 1000;
}

/* 修改按钮的渐变色 */
.switch-btn {
    width: 100%;
    padding: 14px;
    text-align: center;
    background: linear-gradient(135deg, #2782dd, #61c8f4);
    /* 修改为蓝紫色渐变 */
    color: white;
    font-weight: 600;
    border-radius: 12px;
    border: none;
    font-size: 0.95rem;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: all 0.3s ease;
    margin-bottom: 10px;
}

.switch-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.logout-btn {
    width: 100%;
    padding: 14px;
    text-align: center;
    background: linear-gradient(135deg, #3258cc, #71a3ff);
    /* 修改为浅紫色渐变 */
    color: white;
    font-weight: 600;
    border-radius: 12px;
    border: none;
    font-size: 0.95rem;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
    cursor: pointer;
    transition: all 0.3s ease;
}

.logout-btn:hover {
    background: linear-gradient(135deg, #8e80f0, #4c20a2);
    /* 调整hover效果的颜色 */
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.logout-btn i {
    margin-right: 8px;
}

.address-manager {
    margin-top: -120px;
    /* 向上移动 */
    transform: translateY(-20px);
    /* 进一步调整位置 */
}
</style>