<template>
  <div class="loans-container">
    <BackButton style="margin-top: 2vw;" />
    <div class="header">
      <h1>贷款记录</h1>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 贷款列表 -->
    <div v-else class="loans-content">
      <!-- 统计信息 -->
      <div class="stats-section" v-if="loans.length > 0">
        <div class="stat-item">
          <span class="stat-label">总贷款金额</span>
          <span class="stat-value total-loan">¥{{ totalLoanAmount.toFixed(2) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">待还金额</span>
          <span class="stat-value pending-repay">¥{{ pendingRepayAmount.toFixed(2) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">贷款笔数</span>
          <span class="stat-value">{{ loans.length }}</span>
        </div>
      </div>

      <!-- 筛选器 -->
      <div class="filter-section">
        <div class="filter-tabs">
          <div 
            class="filter-tab" 
            :class="{ active: currentFilter === 'all' }"
            @click="changeFilter('all')"
          >
            全部
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: currentFilter === 'pending' }"
            @click="changeFilter('pending')"
          >
            待还款
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: currentFilter === 'repaid' }"
            @click="changeFilter('repaid')"
          >
            已还款
          </div>
        </div>
      </div>

      <!-- 贷款列表 -->
      <div class="loans-list">
        <div 
          v-for="loan in filteredLoans" 
          :key="loan.id" 
          class="loan-item"
          :class="{ 'pending-item': !loan.repayTime }"
          @click="showLoanDetail(loan)"
        >
          <div class="loan-icon" :class="getLoanStatusClass(loan)">
            <i :class="getLoanIcon(loan)"></i>
          </div>
          <div class="loan-info">
            <div class="loan-header">
              <div class="loan-title">
                <span class="loan-status" :class="getStatusClass(loan)">
                  {{ getStatusText(loan) }}
                </span>
              </div>
              <span class="loan-amount">¥{{ loan.loanAmount.toFixed(2) }}</span>
            </div>
            <div class="loan-details">
              <div class="loan-time">
                <i class="fas fa-calendar"></i>
                <span class="label">借款时间：</span>
                {{ formatTime(loan.createTime) }}
              </div>
              <div v-if="loan.repayTime" class="loan-repay-time">
                <i class="fas fa-check-circle"></i>
                <span class="label">还款时间：</span>
                {{ formatTime(loan.repayTime) }}
              </div>
            </div>
            <div class="loan-actions" v-if="!loan.repayTime">
              <button class="repay-btn" @click.stop="showRepayModal(loan)">
                还款
              </button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredLoans.length === 0" class="empty-state">
          <i class="fas fa-file-invoice-dollar"></i>
          <p v-if="currentFilter === 'pending'">暂无待还款记录</p>
          <p v-else-if="currentFilter === 'repaid'">暂无已还款记录</p>
          <p v-else>暂无贷款记录</p>
        </div>
      </div>
    </div>

    <!-- 贷款详情弹窗 -->
    <div v-if="showLoanDetailModal" class="modal-overlay" @click.self="closeLoanDetailModal">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <h3>贷款详情</h3>
          <i class="fas fa-times close-btn" @click="closeLoanDetailModal"></i>
        </div>
        <div class="modal-body">
          <div class="detail-section">
            <div class="detail-item">
              <span class="detail-label">借款金额：</span>
              <span class="detail-value amount">¥{{ loanDetail?.loanAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">利率：</span>
              <span class="detail-value">{{ (loanDetail?.rate * 100)?.toFixed(2) }}%</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">利息金额：</span>
              <span class="detail-value interest">¥{{ loanDetail?.interestAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">应还总额：</span>
              <span class="detail-value total-amount">
                ¥{{ ((loanDetail?.loanAmount || 0) + (loanDetail?.interestAmount || 0))?.toFixed(2) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">借款时间：</span>
              <span class="detail-value">{{ formatTime(loanDetail?.createTime) }}</span>
            </div>
            <div v-if="loanDetail?.repayTime" class="detail-item">
              <span class="detail-label">还款时间：</span>
              <span class="detail-value">{{ formatTime(loanDetail?.repayTime) }}</span>
            </div>
            <div class="detail-item status-item">
              <span class="detail-label">状态：</span>
              <span class="detail-value status" :class="loanDetail?.repayTime ? 'status-repaid' : 'status-pending'">
                {{ loanDetail?.repayTime ? '已还款' : '待还款' }}
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeLoanDetailModal">关闭</button>
          <button 
            v-if="!loanDetail?.repayTime" 
            class="confirm-btn" 
            @click="handleDetailRepay"
          >
            <i class="fas fa-credit-card"></i>
            立即还款
          </button>
        </div>
      </div>
    </div>

    <!-- 还款确认弹窗 -->
    <div v-if="showRepayConfirmModal" class="modal-overlay" @click.self="closeRepayModal">
      <div class="modal-content repay-modal">
        <div class="modal-header">
          <h3>确认还款</h3>
          <i class="fas fa-times close-btn" @click="closeRepayModal"></i>
        </div>
        <div class="modal-body">
          <div class="repay-summary">
            <div class="summary-item">
              <i class="fas fa-info-circle"></i>
              <span class="summary-label">贷款信息</span>
            </div>
            <div class="repay-details">
              <div class="detail-item">
                <span class="detail-label">借款金额：</span>
                <span class="detail-value amount">¥{{ currentLoan?.loanAmount.toFixed(2) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">借款时间：</span>
                <span class="detail-value">{{ formatTime(currentLoan?.createTime) }}</span>
              </div>
            </div>
          </div>

          <div class="wallet-balance">
            <div class="balance-info">
              <i class="fas fa-wallet"></i>
              <span class="balance-label">当前钱包余额：</span>
              <span class="balance-amount">¥{{ walletBalance.toFixed(2) }}</span>
            </div>
            <div v-if="walletBalance < (currentLoan?.loanAmount || 0)" class="balance-warning">
              <i class="fas fa-exclamation-triangle"></i>
              <span>余额不足，请先充值</span>
            </div>
            <div v-else class="balance-sufficient">
              <i class="fas fa-check-circle"></i>
              <span>余额充足，可完成还款</span>
            </div>
          </div>

          <div class="repay-method">
            <div class="method-title">
              <i class="fas fa-credit-card"></i>
              <span>还款方式</span>
            </div>
            <div class="method-options">
              <label class="method-option" :class="{ active: repayMethod === 'wallet', disabled: walletBalance < (currentLoan?.loanAmount || 0) }">
                <input 
                  type="radio" 
                  v-model="repayMethod" 
                  value="wallet" 
                  :disabled="walletBalance < (currentLoan?.loanAmount || 0)"
                />
                <div class="method-content">
                  <i class="fas fa-wallet"></i>
                  <div class="method-info">
                    <span class="method-name">钱包余额还款</span>
                    <span class="method-desc">从钱包余额中扣除还款金额</span>
                  </div>
                </div>
              </label>
              <label class="method-option" :class="{ active: repayMethod === 'thirdparty' }">
                <input type="radio" v-model="repayMethod" value="thirdparty" />
                <div class="method-content">
                  <i class="fas fa-mobile-alt"></i>
                  <div class="method-info">
                    <span class="method-name">第三方支付</span>
                    <span class="method-desc">使用支付宝、微信等支付</span>
                  </div>
                </div>
              </label>
            </div>
          </div>

          <div class="repay-notice" v-if="repayMethod === 'wallet' && walletBalance < (currentLoan?.loanAmount || 0)">
            <i class="fas fa-exclamation-circle"></i>
            <span>钱包余额不足，请选择第三方支付或先充值</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeRepayModal">取消</button>
          <button 
            class="confirm-btn" 
            @click="handleRepay" 
            :disabled="repayMethod === 'wallet' && walletBalance < (currentLoan?.loanAmount || 0)"
          >
            <i class="fas fa-check"></i>
            确认还款
          </button>
        </div>
      </div>
    </div>

    <!-- 还款成功弹窗 -->
    <div v-if="showRepaySuccessModal" class="modal-overlay" @click.self="closeSuccessModal">
      <div class="modal-content success-modal">
        <div class="success-icon">
          <i class="fas fa-check-circle"></i>
        </div>
        <div class="success-content">
          <h3>还款成功</h3>
          <div class="success-details">
            <div class="detail-item">
              <span class="label">还款金额：</span>
              <span class="value">¥{{ currentLoan?.loanAmount.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">还款时间：</span>
              <span class="value">{{ new Date().toLocaleString('zh-CN') }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="confirm-btn" @click="closeSuccessModal">完成</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/trade/utils/tradeRequest';
import { toast } from '@/trade/utils/toast';
import BackButton from '../components/BackButton.vue';
import eventBus from '../utils/eventBus';

export default {
  name: 'WalletLoans',
  components: {
    BackButton
  },
  setup() {
    const router = useRouter();
    const loading = ref(true);
    const loans = ref([]);
    const currentFilter = ref('all');
    const showRepayConfirmModal = ref(false);
    const showRepaySuccessModal = ref(false);
    const showLoanDetailModal = ref(false);
    const currentLoan = ref(null);
    const loanDetail = ref(null);
    const repayMethod = ref('wallet');
    const walletBalance = ref(0);

    // 获取贷款列表
    const fetchLoans = async () => {
      try {
        loading.value = true;
        console.log('获取贷款列表...');
        
        const response = await request.get('/api/wallet/loan/list');
        console.log('贷款列表接口响应:', response);

        if (response && response.success) {
          const list = Array.isArray(response.data) ? response.data : [];
          console.log('贷款列表数据:', list);
          
          loans.value = list.map(loan => ({
            id: loan.id,
            walletId: loan.walletId,
            createTime: loan.createTime,
            repayTime: loan.repayTime,
            loanAmount: loan.loanAmount || 0,
            status: loan.repayTime ? 'repaid' : 'pending'
          }));

          if (list.length === 0) {
            toast.info('暂无贷款记录');
          }
        } else {
          toast.error('获取贷款记录失败: ' + (response?.message || '接口调用失败'));
        }
      } catch (error) {
        console.error('获取贷款记录失败:', error);
        toast.error('获取贷款记录失败');
      } finally {
        loading.value = false;
      }
    };

    // 获取贷款详情
    const fetchLoanDetail = async (loanId) => {
      try {
        console.log('获取贷款详情，ID:', loanId);
        
        const response = await request.get('/api/wallet/loan/detail', {
          params: { loanId: loanId }
        });
        
        console.log('贷款详情接口响应:', response);

        if (response && response.success) {
          loanDetail.value = response.data;
        } else {
          toast.error('获取贷款详情失败: ' + (response?.message || '未知错误'));
        }
      } catch (error) {
        console.error('获取贷款详情失败:', error);
        toast.error('获取贷款详情失败');
      }
    };

    // 获取钱包余额
    const fetchWalletBalance = async () => {
      try {
        const response = await request.get('/api/wallet/message');
        if (response && response.success) {
          walletBalance.value = response.data?.balance || 0;
        }
      } catch (error) {
        console.error('获取钱包余额失败:', error);
        walletBalance.value = 0;
      }
    };

    // 过滤后的贷款列表
    const filteredLoans = computed(() => {
      if (currentFilter.value === 'all') {
        return loans.value;
      } else if (currentFilter.value === 'pending') {
        return loans.value.filter(loan => !loan.repayTime);
      } else if (currentFilter.value === 'repaid') {
        return loans.value.filter(loan => loan.repayTime);
      }
      return loans.value;
    });

    // 统计信息
    const totalLoanAmount = computed(() => {
      return loans.value.reduce((sum, loan) => sum + loan.loanAmount, 0);
    });

    const pendingRepayAmount = computed(() => {
      return loans.value
        .filter(loan => !loan.repayTime)
        .reduce((sum, loan) => sum + loan.loanAmount, 0);
    });

    // 更改筛选
    const changeFilter = (filter) => {
      currentFilter.value = filter;
    };

    // 显示贷款详情弹窗
    const showLoanDetail = async (loan) => {
      console.log('点击贷款记录:', loan);
      currentLoan.value = loan;
      showLoanDetailModal.value = true;
      await fetchLoanDetail(loan.id);
    };

    // 关闭贷款详情弹窗
    const closeLoanDetailModal = () => {
      showLoanDetailModal.value = false;
      loanDetail.value = null;
    };

    // 从详情弹窗跳转到还款
    const handleDetailRepay = () => {
      closeLoanDetailModal();
      showRepayModal(currentLoan.value);
    };

    // 显示还款弹窗
    const showRepayModal = (loan) => {
      currentLoan.value = loan;
      repayMethod.value = 'wallet';
      showRepayConfirmModal.value = true;
      fetchWalletBalance();
    };

    // 关闭还款弹窗
    const closeRepayModal = () => {
      showRepayConfirmModal.value = false;
      currentLoan.value = null;
      repayMethod.value = 'wallet';
    };

    // 关闭成功弹窗
    const closeSuccessModal = () => {
      showRepaySuccessModal.value = false;
      currentLoan.value = null;
      // 刷新贷款列表
      fetchLoans();
      // 通知钱包页面刷新
      eventBus.emit('walletUpdated');
    };

    // 处理还款
    const handleRepay = async () => {
      if (!currentLoan.value) return;

      try {
        console.log('发起还款请求，贷款ID:', currentLoan.value.id);
        
        const option = repayMethod.value === 'wallet' ? 1 : 0;
        
        const response = await request.get('/api/wallet/loan/repay', {
          params: { 
            id: currentLoan.value.id,
            option: option
          }
        });
        
        console.log('还款接口响应:', response);

        if (response && response.success) {
          // 还款成功
          showRepayConfirmModal.value = false;
          showRepaySuccessModal.value = true;
          toast.success('还款成功');
        } else {
          toast.error('还款失败: ' + (response?.message || '未知错误'));
        }
      } catch (error) {
        console.error('还款失败:', error);
        toast.error('还款失败，请重试');
      }
    };

    // 获取贷款状态样式类
    const getLoanStatusClass = (loan) => {
      return loan.repayTime ? 'status-repaid' : 'status-pending';
    };

    // 获取贷款图标
    const getLoanIcon = (loan) => {
      return loan.repayTime ? 'fas fa-check-circle' : 'fas fa-clock';
    };

    // 获取状态文本
    const getStatusText = (loan) => {
      return loan.repayTime ? '已还款' : '待还款';
    };

    // 获取状态样式类
    const getStatusClass = (loan) => {
      return loan.repayTime ? 'status-repaid' : 'status-pending';
    };

    // 格式化时间
    const formatTime = (timeStr) => {
      if (!timeStr) return '';
      const date = new Date(timeStr);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    };

    onMounted(() => {
      fetchLoans();
    });

    return {
      loading,
      loans,
      currentFilter,
      filteredLoans,
      totalLoanAmount,
      pendingRepayAmount,
      showRepayConfirmModal,
      showRepaySuccessModal,
      showLoanDetailModal,
      currentLoan,
      loanDetail,
      repayMethod,
      walletBalance,
      fetchLoans,
      changeFilter,
      showLoanDetail,
      closeLoanDetailModal,
      handleDetailRepay,
      showRepayModal,
      closeRepayModal,
      closeSuccessModal,
      handleRepay,
      getLoanStatusClass,
      getLoanIcon,
      getStatusText,
      getStatusClass,
      formatTime
    };
  }
};
</script>

<style scoped>
.loans-container {
  max-width: 600px;
  margin: 0 auto;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  height: 12vw;
  background-color: #0097FF;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header h1 {
  font-size: 4.8vw;
  margin: 0;
  font-weight: 500;
}

.loans-content {
  padding-top: 14vw;
  padding-bottom: 20vw; /* 减少底部内边距，避免被tabbar遮挡 */
}

.stats-section {
  background: white;
  padding: 4vw;
  margin: 0 4vw 3vw;
  border-radius: 3vw;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5vw;
}

.stat-label {
  font-size: 3.2vw;
  color: #666;
}

.stat-value {
  font-size: 3.8vw;
  font-weight: bold;
}

.stat-value.total-loan {
  color: #0097FF;
}

.stat-value.pending-repay {
  color: #ff4d4f;
}

.filter-section {
  background: white;
  padding: 3vw 4vw;
  margin-bottom: 2vw;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.filter-tabs {
  display: flex;
  gap: 2vw;
}

.filter-tab {
  flex: 1;
  padding: 2.5vw;
  border-radius: 2vw;
  font-size: 3.6vw;
  color: #666;
  background: #f5f7fa;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-tab.active {
  background: #0097FF;
  color: white;
}

.loans-list {
  padding: 0 4vw;
}

.loan-item {
  background: white;
  border-radius: 3vw;
  padding: 4vw;
  margin-bottom: 3vw;
  display: flex;
  align-items: flex-start;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  position: relative;
  cursor: pointer;
  width: 100%;
  box-sizing: border-box;
}

.loan-item:active {
  background: #f8f9fa;
  transform: scale(0.98);
}

.loan-item.pending-item {
  border-left: 4px solid #ff4d4f;
}

.loan-icon {
  width: 10vw;
  height: 10vw;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 3vw;
  flex-shrink: 0;
  margin-top: 1vw;
}

.loan-icon i {
  font-size: 4.5vw;
  color: white;
}

.status-pending {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.status-repaid {
  background: linear-gradient(135deg, #51cf66 0%, #40c057 100%);
}

.loan-info {
  flex: 1;
  min-width: 0;
  width: 100%;
}

.loan-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2.5vw;
  width: 100%;
}

.loan-title {
  display: flex;
  align-items: center;
  gap: 2vw;
}

.loan-status {
  font-size: 3.2vw;
  padding: 1vw 2vw;
  border-radius: 1.5vw;
  font-weight: 500;
  white-space: nowrap;
}

.status-pending {
  background: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.status-repaid {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.loan-amount {
  font-size: 4.2vw;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
  margin-left: auto;
}

.loan-details {
  font-size: 3.2vw;
  color: #999;
  line-height: 1.6;
  margin-bottom: 2.5vw;
  width: 100%;
}

.loan-time,
.loan-repay-time,
.loan-pending {
  display: flex;
  align-items: center;
  gap: 1.5vw;
  margin-bottom: 1.5vw;
  flex-wrap: wrap;
}

.loan-details i {
  font-size: 3.2vw;
  width: 4vw;
  text-align: center;
  flex-shrink: 0;
}

.loan-pending .pending-text {
  color: #ff4d4f;
  font-weight: 500;
}

.loan-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1vw;
  width: 100%;
}

.repay-btn {
  padding: 2vw 3.5vw;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  border: none;
  border-radius: 1.5vw;
  font-size: 3vw;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 1vw;
  white-space: nowrap;
  transition: all 0.3s;
  min-width: auto;
}

.repay-btn:active {
  transform: scale(0.95);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20vw;
  color: #999;
  text-align: center;
}

.empty-state i {
  font-size: 15vw;
  margin-bottom: 4vw;
  color: #ddd;
}

.empty-state p {
  font-size: 4vw;
  margin-bottom: 4vw;
}

.retry-btn {
  padding: 2.5vw 4vw;
  background: #0097FF;
  color: white;
  border: none;
  border-radius: 2vw;
  font-size: 3.4vw;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 1.5vw;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20vw;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #0097FF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 弹窗样式 */
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

.modal-content {
  background: white;
  border-radius: 4vw;
  width: 85%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4vw;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  font-size: 4.5vw;
  margin: 0;
  color: #333;
}

.close-btn {
  font-size: 5vw;
  color: #999;
  cursor: pointer;
}

.modal-body {
  padding: 4vw;
}

/* 详情弹窗样式 */
.detail-section {
  background: #f8f9fa;
  border-radius: 2vw;
  padding: 3vw;
}

.detail-section .detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2.5vw 0;
  font-size: 3.6vw;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}

.detail-section .detail-item:last-child {
  border-bottom: none;
}

.detail-section .detail-label {
  color: #999;
  font-weight: 400;
}

.detail-section .detail-value {
  color: #333;
  font-weight: 500;
}

.detail-section .detail-value.amount {
  color: #ff4d4f;
  font-weight: bold;
  font-size: 4vw;
}

.detail-section .detail-value.interest {
  color: #fa8c16;
  font-weight: bold;
}

.detail-section .detail-value.total-amount {
  color: #1890ff;
  font-weight: bold;
  font-size: 4.2vw;
}

.detail-section .status-item {
  background: #fff;
  border-radius: 1.5vw;
  padding: 2vw 3vw;
  margin-top: 1vw;
}

.detail-section .status {
  padding: 1vw 2vw;
  border-radius: 1vw;
  font-size: 3.2vw;
  font-weight: 500;
}

.detail-section .status-pending {
  background: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.detail-section .status-repaid {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.repay-summary {
  background: #f8f9fa;
  border-radius: 2vw;
  padding: 3vw;
  margin-bottom: 4vw;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 2vw;
  margin-bottom: 2vw;
  font-size: 3.8vw;
  font-weight: 500;
  color: #333;
}

.summary-item i {
  color: #0097FF;
  font-size: 4vw;
}

.repay-details {
  padding-left: 2vw;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5vw 0;
  font-size: 3.4vw;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  color: #999;
}

.detail-value {
  color: #333;
  font-weight: 500;
}

.detail-value.amount {
  color: #ff4d4f;
  font-weight: bold;
  font-size: 3.8vw;
}

.wallet-balance {
  background: #e6f7ff;
  border-radius: 2vw;
  padding: 3vw;
  margin-bottom: 4vw;
  border: 1px solid #91d5ff;
}

.balance-info {
  display: flex;
  align-items: center;
  gap: 2vw;
  margin-bottom: 2vw;
  font-size: 3.6vw;
}

.balance-info i {
  color: #0097FF;
  font-size: 4vw;
}

.balance-label {
  color: #666;
}

.balance-amount {
  color: #0097FF;
  font-weight: bold;
}

.balance-warning,
.balance-sufficient {
  display: flex;
  align-items: center;
  gap: 2vw;
  font-size: 3.2vw;
  padding: 2vw;
  border-radius: 1vw;
}

.balance-warning {
  background: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.balance-warning i {
  color: #ff4d4f;
}

.balance-sufficient {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.balance-sufficient i {
  color: #52c41a;
}

.repay-method {
  margin-bottom: 4vw;
}

.method-title {
  display: flex;
  align-items: center;
  gap: 2vw;
  font-size: 3.8vw;
  font-weight: 500;
  color: #333;
  margin-bottom: 3vw;
}

.method-title i {
  color: #0097FF;
  font-size: 4vw;
}

.method-options {
  display: flex;
  flex-direction: column;
  gap: 2vw;
}

.method-option {
  display: flex;
  align-items: center;
  padding: 3vw;
  border: 2px solid #e0e0e0;
  border-radius: 2vw;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.method-option.active {
  border-color: #0097FF;
  background: #f0f8ff;
}

.method-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.method-option input {
  margin-right: 2vw;
  width: 4vw;
  height: 4vw;
  cursor: pointer;
}

.method-option.disabled input {
  cursor: not-allowed;
}

.method-content {
  display: flex;
  align-items: center;
  gap: 2vw;
  flex: 1;
}

.method-content i {
  font-size: 4.5vw;
  color: #0097FF;
}

.method-info {
  display: flex;
  flex-direction: column;
  gap: 0.5vw;
}

.method-name {
  font-size: 3.6vw;
  font-weight: 500;
  color: #333;
}

.method-desc {
  font-size: 3vw;
  color: #999;
}

.repay-notice {
  display: flex;
  align-items: center;
  gap: 2vw;
  padding: 3vw;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 2vw;
  font-size: 3.2vw;
  color: #d46b08;
}

.repay-notice i {
  color: #fa8c16;
  font-size: 3.6vw;
}

.modal-footer {
  display: flex;
  gap: 3vw;
  padding: 4vw;
  border-top: 1px solid #f0f0f0;
}

.modal-footer button {
  flex: 1;
  padding: 3.5vw;
  border: none;
  border-radius: 2vw;
  font-size: 4vw;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5vw;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:active {
  background: #e0e0e0;
}

.confirm-btn {
  background: #0097FF;
  color: white;
}

.confirm-btn:active {
  background: #0080e6;
}

.confirm-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 成功弹窗样式 */
.success-modal {
  text-align: center;
  max-width: 400px;
}

.success-icon {
  margin: 6vw 0 4vw;
}

.success-icon i {
  font-size: 15vw;
  color: #52c41a;
}

.success-content h3 {
  font-size: 5vw;
  color: #333;
  margin-bottom: 3vw;
}

.success-content p {
  font-size: 3.8vw;
  color: #666;
  margin-bottom: 4vw;
}

.success-details {
  background: #f8f9fa;
  border-radius: 2vw;
  padding: 3vw;
  margin: 0 4vw 4vw;
}

.success-details .detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2vw 0;
  font-size: 3.4vw;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}

.success-details .detail-item:last-child {
  border-bottom: none;
}

.success-details .label {
  color: #999;
}

.success-details .value {
  color: #333;
  font-weight: 500;
}
</style>