<template>
  <div class="transactions-container">
    <BackButton style="margin-top: 2vw;" />
    <div class="header">
      <h1>交易明细</h1>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 交易列表 -->
    <div v-else class="transactions-content">
      <!-- 筛选器 -->
      <div class="filter-section">
        <!-- 时间筛选 -->
        <div class="date-filter">
          <div class="date-input-group">
            <label>开始日期</label>
            <input 
              type="date" 
              v-model="startDate" 
              class="date-input"
              @change="handleDateChange"
            />
          </div>
          <div class="date-input-group">
            <label>结束日期</label>
            <input 
              type="date" 
              v-model="endDate" 
              class="date-input"
              @change="handleDateChange"
            />
          </div>
          <button class="clear-date-btn" @click="clearDates" v-if="startDate || endDate">
            <i class="fas fa-times"></i>
            清空
          </button>
        </div>

        <!-- 类型筛选 -->
        <div class="filter-tabs">
          <div 
            class="filter-tab" 
            :class="{ active: currentFilter === -1 }"
            @click="changeFilter(-1)"
          >
            全部
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: currentFilter === 3 }"
            @click="changeFilter(3)"
          >
            充值
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: currentFilter === 2 }"
            @click="changeFilter(2)"
          >
            提现
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: currentFilter === 0 }"
            @click="changeFilter(0)"
          >
            支付
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: currentFilter === 1 }"
            @click="changeFilter(1)"
          >
            收款
          </div>
          <div 
            class="filter-tab" 
            :class="{ active: currentFilter === 4 }"
            @click="changeFilter(4)"
          >
            退款
          </div>
        </div>

        <!-- 状态筛选 -->
        <div class="status-filter">
          <div 
            class="status-tab" 
            :class="{ active: currentStatus === -1 }"
            @click="changeStatus(-1)"
          >
            全部状态
          </div>
          <div 
            class="status-tab" 
            :class="{ active: currentStatus === 0 }"
            @click="changeStatus(0)"
          >
            正常
          </div>
          <div 
            class="status-tab" 
            :class="{ active: currentStatus === 1 }"
            @click="changeStatus(1)"
          >
            冻结
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button class="reset-btn" @click="resetFilters">
            <i class="fas fa-redo"></i>
            重置筛选
          </button>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section" v-if="transactions.length > 0">
        <div class="stat-item">
          <span class="stat-label">总收入</span>
          <span class="stat-value income">+¥{{ totalIncome.toFixed(2) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">总支出</span>
          <span class="stat-value expense">-¥{{ totalExpense.toFixed(2) }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">交易笔数</span>
          <span class="stat-value">{{ transactions.length }}</span>
        </div>
      </div>

      <!-- 交易列表 -->
      <div class="transactions-list">
        <div 
          v-for="transaction in transactions" 
          :key="transaction.id" 
          class="transaction-item"
          @click="viewDetail(transaction.id)"
        >
          <div class="transaction-icon" :class="getTransactionTypeClass(transaction.type)">
            <i :class="getTransactionIcon(transaction.type)"></i>
          </div>
          <div class="transaction-info">
            <div class="transaction-header">
              <div class="transaction-title">
                <span class="transaction-type">{{ getTransactionTypeName(transaction.type) }}</span>
                <span class="transaction-status" :class="getStatusClass(transaction.status)">
                  {{ getStatusName(transaction.status) }}
                </span>
              </div>
              <span class="transaction-amount" :class="getAmountClass(transaction.inOrOut)">
                {{ getAmountPrefix(transaction.inOrOut) }}¥{{ Math.abs(transaction.amount).toFixed(2) }}
              </span>
            </div>
            <div class="transaction-details">
              <div class="transaction-reason" v-if="transaction.reason">
                <span class="label">备注：</span>
                <span class="reason-text">{{ transaction.reason }}</span>
              </div>
              <div class="transaction-time">
                <i class="fas fa-clock"></i>
                {{ formatTime(transaction.createTime) }}
              </div>
              <div class="transaction-fee" v-if="transaction.fee > 0">
                <span class="label">手续费：</span>
                <span class="fee-amount">¥{{ transaction.fee.toFixed(2) }}</span>
              </div>
              <div class="transaction-accounts">
                <div v-if="transaction.fromAccountName && transaction.type !== 3" class="account-info">
                  <span class="label">转出：</span>
                  <span>{{ transaction.fromAccountName }}</span>
                </div>
                <div v-if="transaction.toAccountName && transaction.type !== 2" class="account-info">
                  <span class="label">转入：</span>
                  <span>{{ transaction.toAccountName }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="transactions.length === 0" class="empty-state">
          <i class="fas fa-inbox"></i>
          <p>暂无交易记录</p>
          <!-- <button class="retry-btn" @click="fetchTransactions">
            <i class="fas fa-redo"></i>
            重新加载
          </button> -->
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/trade/utils/tradeRequest';
import { toast } from '@/trade/utils/toast';
import BackButton from '../components/BackButton.vue';
import eventBus from '../utils/eventBus';

export default {
  name: 'WalletTransactions',
  components: {
    BackButton
  },
  setup() {
    const router = useRouter();
    const loading = ref(true);
    const transactions = ref([]);
    const currentFilter = ref(-1); // -1表示全部
    const currentStatus = ref(-1); // -1表示全部状态
    const startDate = ref('');
    const endDate = ref('');

    // 获取交易明细
    const fetchTransactions = async () => {
      try {
        loading.value = true;
        transactions.value = [];

        // 构建请求参数，所有参数都是可选的
        const params = {};
        
        // 只有当不是全部时才添加类型参数
        if (currentFilter.value !== -1) {
          params.type = currentFilter.value;
        }
        
        // 只有当不是全部状态时才添加状态参数
        if (currentStatus.value !== -1) {
          params.status = currentStatus.value;
        }
        
        // 只有当有日期时才添加日期参数
        if (startDate.value) {
          // 确保日期格式为 yyyy-mm-dd
          params.startDate = startDate.value;
        }
        
        if (endDate.value) {
          // 确保日期格式为 yyyy-mm-dd
          params.endDate = endDate.value;
        }

        console.log('调用交易明细接口，参数:', params);
        const response = await request.get('/api/wallet/transaction/list', { params });
        console.log('交易明细接口响应:', response);

        if (response && response.success) {
          const list = Array.isArray(response.data) ? response.data : [];
          console.log('交易明细数据:', list);

          transactions.value = list;
          
          if (list.length === 0) {
            toast.info('暂无交易记录');
          }
        } else {
          toast.error('获取交易明细失败: ' + (response?.message || '接口调用失败'));
        }
      } catch (error) {
        console.error('获取交易明细失败:', error);
        toast.error('获取交易明细失败');
      } finally {
        loading.value = false;
      }
    };

    // 更改类型筛选
    const changeFilter = (type) => {
      currentFilter.value = type;
      fetchTransactions();
    };

    // 更改状态筛选
    const changeStatus = (status) => {
      currentStatus.value = status;
      fetchTransactions();
    };

    // 处理日期变化
    const handleDateChange = () => {
      // 确保开始日期不大于结束日期
      if (startDate.value && endDate.value && startDate.value > endDate.value) {
        toast.warning('开始日期不能大于结束日期');
        // 自动交换日期
        const temp = startDate.value;
        startDate.value = endDate.value;
        endDate.value = temp;
        return;
      }
      fetchTransactions();
    };

    // 清空日期
    const clearDates = () => {
      startDate.value = '';
      endDate.value = '';
      fetchTransactions();
    };

    // 重置筛选条件
    const resetFilters = () => {
      currentFilter.value = -1;
      currentStatus.value = -1;
      startDate.value = '';
      endDate.value = '';
      fetchTransactions();
    };

    // 查看详情
    const viewDetail = async (id) => {
      try {
        const response = await request.get('/api/wallet/transaction/detail', {
          params: { transactionId: id }
        });
        if (response && response.success && response.data) {
          const d = response.data;
          const detail = {
            id: d.id,
            type: d.type,
            amount: d.amount,
            fee: d.fee,
            createTime: d.createTime,
            inOrOut: d.inOrOut,
            status: d.status,
            fromAccount: d.from_account,
            toAccount: d.to_account,
            fromAccountName: d.from_account_name,
            toAccountName: d.to_account_name,
            feeRate: d.fee_rate,
            reason: d.reason
          };

          // 格式化显示详情
          const typeNames = { 0: '支付', 1: '收款', 2: '提现', 3: '充值', 4: '退款' };
          const inOutNames = { 0: '支出', 1: '收入' };
          const statusNames = { 0: '正常', 1: '冻结' };

          let detailInfo = `交易详情 #${detail.id}\n\n`;
          detailInfo += `交易类型：${typeNames[detail.type] || '未知'}\n`;
          detailInfo += `交易金额：${getAmountPrefix(detail.inOrOut)}¥${Math.abs(detail.amount).toFixed(2)}\n`;
          detailInfo += `资金流向：${inOutNames[detail.inOrOut] || '未知'}\n`;
          
          if (detail.fee > 0) {
            detailInfo += `手续费：¥${detail.fee.toFixed(2)}\n`;
          }
          if (detail.feeRate) {
            detailInfo += `手续费率：${(detail.feeRate * 100).toFixed(2)}%\n`;
          }
          
          detailInfo += `交易状态：${statusNames[detail.status] || '未知'}\n`;
          detailInfo += `交易时间：${formatTime(detail.createTime)}\n`;
          
          if (detail.reason) {
            detailInfo += `交易备注：${detail.reason}\n`;
          }
          
          if (detail.fromAccountName && detail.type !== 3) {
            detailInfo += `转出账户：${detail.fromAccountName}\n`;
          }
          if (detail.toAccountName && detail.type !== 2) {
            detailInfo += `转入账户：${detail.toAccountName}\n`;
          }

          alert(detailInfo);
        } else {
          toast.error('获取明细详情失败');
        }
      } catch (e) {
        console.error('获取明细详情失败:', e);
        toast.error('获取明细详情失败');
      }
    };

    // 统计信息计算
    const totalIncome = computed(() => {
      return transactions.value
        .filter(t => t.inOrOut === 1) // 收入
        .reduce((sum, t) => sum + t.amount, 0);
    });

    const totalExpense = computed(() => {
      return transactions.value
        .filter(t => t.inOrOut === 0) // 支出
        .reduce((sum, t) => sum + Math.abs(t.amount), 0);
    });

    // 获取交易类型名称
    const getTransactionTypeName = (type) => {
      const typeMap = {
        0: '支付',
        1: '收款',
        2: '提现',
        3: '充值',
        4: '退款'
      };
      return typeMap[type] || '未知';
    };

    // 获取交易类型图标
    const getTransactionIcon = (type) => {
      const iconMap = {
        0: 'fas fa-arrow-right', // 支付
        1: 'fas fa-arrow-left',  // 收款
        2: 'fas fa-minus-circle', // 提现
        3: 'fas fa-plus-circle',  // 充值
        4: 'fas fa-undo'          // 退款
      };
      return iconMap[type] || 'fas fa-exchange-alt';
    };

    // 获取交易类型样式类
    const getTransactionTypeClass = (type) => {
      const classMap = {
        0: 'type-payment',
        1: 'type-received',
        2: 'type-withdraw',
        3: 'type-recharge',
        4: 'type-refund'
      };
      return classMap[type] || 'type-default';
    };

    // 获取状态名称
    const getStatusName = (status) => {
      return status === 1 ? '冻结' : '正常';
    };

    // 获取状态样式类
    const getStatusClass = (status) => {
      return status === 1 ? 'status-frozen' : 'status-normal';
    };

    // 获取金额前缀
    const getAmountPrefix = (inOrOut) => {
      return inOrOut === 1 ? '+' : '-';
    };

    // 获取金额样式类
    const getAmountClass = (inOrOut) => {
      return inOrOut === 1 ? 'amount-income' : 'amount-expense';
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

    // 监听交易更新事件
    const handleTransactionUpdate = () => {
      console.log('收到交易更新事件，开始刷新交易明细');
      fetchTransactions();
    };

    onMounted(() => {
      // 默认不设置日期，加载所有交易记录
      fetchTransactions();
      // 监听交易更新事件
      eventBus.on('transactionUpdated', handleTransactionUpdate);
    });

    onUnmounted(() => {
      // 清理事件监听
      eventBus.off('transactionUpdated', handleTransactionUpdate);
    });

    return {
      loading,
      transactions,
      currentFilter,
      currentStatus,
      startDate,
      endDate,
      totalIncome,
      totalExpense,
      fetchTransactions,
      changeFilter,
      changeStatus,
      handleDateChange,
      clearDates,
      resetFilters,
      getTransactionTypeName,
      getTransactionIcon,
      getTransactionTypeClass,
      getStatusName,
      getStatusClass,
      getAmountPrefix,
      getAmountClass,
      formatTime,
      viewDetail
    };
  }
};
</script>

<style scoped>
.transactions-container {
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

.transactions-content {
  padding-top: 14vw;
}

.filter-section {
  background: white;
  padding: 3vw 4vw;
  margin-bottom: 2vw;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.date-filter {
  display: flex;
  gap: 2vw;
  margin-bottom: 3vw;
  padding-bottom: 3vw;
  border-bottom: 1px solid #f0f0f0;
  align-items: flex-end;
}

.date-input-group {
  flex: 1;
}

.date-input-group label {
  display: block;
  font-size: 3.2vw;
  color: #666;
  margin-bottom: 1vw;
}

.date-input {
  width: 100%;
  padding: 2.5vw;
  border: 1px solid #ddd;
  border-radius: 1.5vw;
  font-size: 3.4vw;
  box-sizing: border-box;
  position: relative;
}

/* 强制日期输入框显示 yyyy-mm-dd 格式 */
.date-input::-webkit-datetime-edit {
  color: transparent;
}

.date-input:focus::-webkit-datetime-edit,
.date-input:valid::-webkit-datetime-edit {
  color: #333;
}

.date-input::-webkit-datetime-edit-fields-wrapper {
  color: #333;
}

/* Firefox 样式 */
.date-input:invalid {
  color: transparent;
}

.date-input:valid {
  color: #333;
}

.clear-date-btn {
  padding: 2.5vw 3vw;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 1.5vw;
  font-size: 3.2vw;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 1vw;
  white-space: nowrap;
  flex-shrink: 0;
}

.clear-date-btn:active {
  background: #e0e0e0;
}

.filter-tabs {
  display: flex;
  gap: 2vw;
  margin-bottom: 3vw;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 1vw;
}

.filter-tab {
  padding: 2vw 3vw;
  border-radius: 4vw;
  font-size: 3.4vw;
  color: #666;
  background: #f5f7fa;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.filter-tab.active {
  background: #0097FF;
  color: white;
}

.status-filter {
  display: flex;
  gap: 2vw;
  margin-bottom: 3vw;
}

.status-tab {
  padding: 1.5vw 3vw;
  border-radius: 4vw;
  font-size: 3.2vw;
  color: #666;
  background: #f5f7fa;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.status-tab.active {
  background: #0097FF;
  color: white;
}

.action-buttons {
  display: flex;
  gap: 2vw;
}

.reset-btn {
  flex: 1;
  padding: 2.5vw;
  border: none;
  border-radius: 1.5vw;
  font-size: 3.4vw;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5vw;
  transition: all 0.3s;
  background: #f5f5f5;
  color: #666;
}

.reset-btn:active {
  transform: scale(0.98);
  background: #e0e0e0;
}

.stats-section {
  background: white;
  padding: 3vw 4vw;
  margin: 0 4vw 3vw;
  border-radius: 3vw;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1vw;
}

.stat-label {
  font-size: 3.2vw;
  color: #666;
}

.stat-value {
  font-size: 3.8vw;
  font-weight: bold;
}

.stat-value.income {
  color: #52c41a;
}

.stat-value.expense {
  color: #ff4d4f;
}

.transactions-list {
  padding: 0 4vw;
  padding-bottom: 20vw; 
}

.transaction-item {
  background: white;
  border-radius: 3vw;
  padding: 4vw;
  margin-bottom: 3vw;
  display: flex;
  align-items: flex-start;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.transaction-item:active {
  background: #f8f9fa;
  transform: scale(0.99);
}

.transaction-icon {
  width: 10vw;
  height: 10vw;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 3vw;
  flex-shrink: 0;
}

.transaction-icon i {
  font-size: 5vw;
  color: white;
}

.type-recharge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.type-withdraw {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.type-payment {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.type-received {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.type-refund {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.type-default {
  background: linear-gradient(135deg, #d299c2 0%, #fef9d7 100%);
}

.transaction-info {
  flex: 1;
  min-width: 0;
}

.transaction-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2vw;
}

.transaction-title {
  display: flex;
  align-items: center;
  gap: 2vw;
}

.transaction-type {
  font-size: 4vw;
  font-weight: 500;
  color: #333;
}

.transaction-status {
  font-size: 2.8vw;
  padding: 0.5vw 1.5vw;
  border-radius: 1vw;
  font-weight: normal;
}

.status-normal {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-frozen {
  background: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.transaction-amount {
  font-size: 4.5vw;
  font-weight: bold;
}

.amount-income {
  color: #52c41a;
}

.amount-expense {
  color: #ff4d4f;
}

.transaction-details {
  font-size: 3.2vw;
  color: #999;
  line-height: 1.6;
}

.transaction-reason {
  margin-bottom: 1.5vw;
  color: #666;
  display: flex;
  align-items: flex-start;
  gap: 1vw;
}

.reason-text {
  flex: 1;
  word-break: break-word;
}

.transaction-time {
  margin-bottom: 1vw;
  display: flex;
  align-items: center;
  gap: 1vw;
}

.transaction-time i {
  font-size: 3vw;
  color: #999;
}

.transaction-fee {
  margin-bottom: 1vw;
  color: #999;
}

.fee-amount {
  color: #ff4d4f;
  font-weight: 500;
}

.transaction-accounts {
  display: flex;
  flex-direction: column;
  gap: 0.5vw;
}

.account-info {
  color: #999;
  font-size: 3vw;
}

.label {
  color: #999;
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
</style>