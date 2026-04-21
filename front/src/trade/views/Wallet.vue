<template>
  <div class="wallet-container">
    <BackButton style="margin-top: 2vw;" />
    <div class="header">
      <h1>虚拟钱包</h1>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 钱包信息 -->
    <div v-else class="wallet-content">
      <!-- 余额卡片 -->
      <div class="balance-card">
        <div class="balance-label">账户余额</div>
        <div class="balance-amount">¥{{ walletInfo.balance?.toFixed(2) || '0.00' }}</div>
        <div class="balance-actions">
          <button class="action-btn recharge-btn" @click="showRechargeModal = true">
            <i class="fas fa-plus-circle"></i>
            充值
          </button>
          <button class="action-btn withdraw-btn" @click="showWithdrawModal = true">
            <i class="fas fa-minus-circle"></i>
            提现
          </button>
        </div>
      </div>

      <!-- 非VIP用户提示 -->
      <div class="vip-promotion-card">
        <div class="vip-promotion-content">
          <i class="fas fa-crown vip-icon"></i>
          <div class="vip-text">
            <div class="vip-title">
              <span v-if="!walletInfo.isVip">成为VIP享受透支额度</span>
              <span v-else>升级VIP享受更多透支额度</span>
            </div>
          </div>
        </div>
        <button class="vip-btn" @click="showVipModal = true">
          <i class="fas fa-star"></i>
            <span v-if="!walletInfo.isVip">去成为VIP</span>
            <span v-else>去升级VIP</span>
        </button>
      </div>

      <!-- VIP用户透支信息 -->
      <div v-if="walletInfo.isVip && walletInfo.overdraftLimit > 0" class="overdraft-card">
        <div class="overdraft-header">
          <span class="overdraft-label">可透支额度</span>
          <span class="vip-badge">{{walletInfo.vipName}}</span>
        </div>
        <div class="overdraft-info">
          <!-- <div class="overdraft-item">
            <span>剩余可透支：</span>
            <span class="overdraft-amount">¥{{ (walletInfo.overdraftLimit - walletInfo.usedOverdraft).toFixed(2) }}</span>
          </div> -->
          <div class="overdraft-item">
            <span>可透支总额：</span>
            <span class="overdraft-total">¥{{ walletInfo.overdraftLimit?.toFixed(2) || '0.00' }}</span>
          </div>
          <div class="overdraft-item">
            <span>已使用透支：</span>
            <span class="overdraft-used">¥{{ walletInfo.usedOverdraft?.toFixed(2) || '0.00' }}</span>
          </div>
        </div>
        <button v-if="walletInfo.usedOverdraft > 0" class="repay-btn" @click="navigateToLoans">
          <i class="fas fa-credit-card"></i>
          去还款
        </button>
      </div>

      <!-- 功能入口 -->
      <div class="function-section">
        <div class="function-item" @click="navigateToTransactions">
          <div class="function-icon">
            <i class="fas fa-list-alt"></i>
          </div>
          <span class="function-text">交易明细</span>
          <i class="fas fa-chevron-right"></i>
        </div>
        <div class="function-item" @click="navigateToLoans">
          <div class="function-icon">
            <i class="fas fa-hand-holding-usd"></i>
          </div>
          <span class="function-text">贷款记录</span>
          <i class="fas fa-chevron-right"></i>
        </div>
      </div>
    </div>

    <!-- 充值弹窗 -->
    <div v-if="showRechargeModal" class="modal-overlay" @click.self="showRechargeModal = false">
      <div class="modal-content recharge-modal">
        <div class="modal-header">
          <h3>充值</h3>
          <i class="fas fa-times close-btn" @click="showRechargeModal = false"></i>
        </div>
        <div class="modal-body">
          <div class="input-group">
            <label>充值金额</label>
            <input type="number" v-model.number="rechargeAmount" placeholder="请输入充值金额" min="0.01" step="0.01" />
          </div>

          <!-- 支付方式选择 -->
          <div class="payment-methods">
            <div class="method-title">选择支付方式</div>
            <div class="payment-options">
              <!-- 微信支付 -->
              <div 
                class="payment-option" 
                :class="{ active: rechargePaymentMethod === 'wechat' }"
                @click="rechargePaymentMethod = 'wechat'"
              >
                <div class="option-content">
                  <i class="fab fa-weixin" style="color: #09BB07;"></i>
                  <div class="option-info">
                    <div class="option-name">微信支付</div>
                    <div class="option-desc">使用微信进行支付</div>
                  </div>
                </div>
                <i class="fas fa-check-circle check-icon"></i>
              </div>
              
              <!-- 支付宝支付 -->
              <div 
                class="payment-option" 
                :class="{ active: rechargePaymentMethod === 'alipay' }"
                @click="rechargePaymentMethod = 'alipay'"
              >
                <div class="option-content">
                  <i class="fab fa-alipay" style="color: #1677FF;"></i>
                  <div class="option-info">
                    <div class="option-name">支付宝支付</div>
                    <div class="option-desc">使用支付宝进行支付</div>
                  </div>
                </div>
                <i class="fas fa-check-circle check-icon"></i>
              </div>
            </div>
          </div>

          <div class="preview-info" v-if="rechargeAmount && rechargeAmount > 0">
            <div class="info-section-title">充值预览</div>
            <div class="info-item">
              <span class="info-label">充值金额：</span>
              <span class="info-value">¥{{ (rechargeAmount || 0).toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">支付方式：</span>
              <span class="info-value payment-method-display">
                <i v-if="rechargePaymentMethod === 'wechat'" class="fab fa-weixin" style="color: #09BB07;"></i>
                <i v-if="rechargePaymentMethod === 'alipay'" class="fab fa-alipay" style="color: #1677FF;"></i>
                {{ rechargePaymentMethod === 'wechat' ? '微信支付' : '支付宝支付' }}
              </span>
            </div>
            <div class="info-item" v-if="rechargePreview">
              <span class="info-label">奖励金额：</span>
              <span class="highlight reward-amount">¥{{ (rechargePreview.fee || 0).toFixed(2) }}</span>
            </div>
            <div class="info-item" v-if="rechargePreview">
              <span class="info-label">奖励率：</span>
              <span class="highlight">{{ ((rechargePreview.feeRate || 0) * 100).toFixed(2) }}%</span>
            </div>
            <div class="info-item total" v-if="rechargePreview">
              <span class="info-label">实际到账：</span>
              <span class="total-amount">¥{{ (rechargePreview.total || 0).toFixed(2) }}</span>
            </div>
            <div class="loading-text" v-if="!rechargePreview && rechargeAmount > 0">
              正在计算奖励...
            </div>
          </div>
          <div class="rules-link">
            <a href="#" @click.prevent="showRechargeRules = true">
              <i class="fas fa-info-circle"></i>
              查看充值与奖励规则
            </a>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showRechargeModal = false">取消</button>
          <button class="confirm-btn" @click="handleRecharge" :disabled="!rechargeAmount || rechargeAmount <= 0">
            确认充值
          </button>
        </div>
      </div>
    </div>

    <!-- 提现弹窗 -->
    <div v-if="showWithdrawModal" class="modal-overlay" @click.self="showWithdrawModal = false">
      <div class="modal-content withdraw-modal">
        <div class="modal-header">
          <h3>提现</h3>
          <i class="fas fa-times close-btn" @click="showWithdrawModal = false"></i>
        </div>
        <div class="modal-body">
          <div class="input-group">
            <label>提现金额</label>
            <input type="number" v-model.number="withdrawAmount" placeholder="请输入提现金额" min="0.01" step="0.01" />
          </div>

          <!-- 提现方式选择 -->
          <div class="payment-methods">
            <div class="method-title">选择提现方式</div>
            <div class="payment-options">
              <!-- 提现到微信 -->
              <div 
                class="payment-option" 
                :class="{ active: withdrawPaymentMethod === 'wechat' }"
                @click="withdrawPaymentMethod = 'wechat'"
              >
                <div class="option-content">
                  <i class="fab fa-weixin" style="color: #09BB07;"></i>
                  <div class="option-info">
                    <div class="option-name">提现到微信</div>
                    <div class="option-desc">提现到微信钱包</div>
                  </div>
                </div>
                <i class="fas fa-check-circle check-icon"></i>
              </div>
              
              <!-- 提现到支付宝 -->
              <div 
                class="payment-option" 
                :class="{ active: withdrawPaymentMethod === 'alipay' }"
                @click="withdrawPaymentMethod = 'alipay'"
              >
                <div class="option-content">
                  <i class="fab fa-alipay" style="color: #1677FF;"></i>
                  <div class="option-info">
                    <div class="option-name">提现到支付宝</div>
                    <div class="option-desc">提现到支付宝账户</div>
                  </div>
                </div>
                <i class="fas fa-check-circle check-icon"></i>
              </div>
            </div>
          </div>

          <div class="preview-info" v-if="withdrawAmount && withdrawAmount > 0">
            <div class="info-section-title">提现预览</div>
            <div class="info-item">
              <span class="info-label">提现金额：</span>
              <span class="info-value">¥{{ (withdrawAmount || 0).toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">提现方式：</span>
              <span class="info-value payment-method-display">
                <i v-if="withdrawPaymentMethod === 'wechat'" class="fab fa-weixin" style="color: #09BB07;"></i>
                <i v-if="withdrawPaymentMethod === 'alipay'" class="fab fa-alipay" style="color: #1677FF;"></i>
                {{ withdrawPaymentMethod === 'wechat' ? '微信钱包' : '支付宝账户' }}
              </span>
            </div>
            <div class="info-item" v-if="withdrawPreview">
              <span class="info-label">手续费：</span>
              <span class="highlight fee-amount">¥{{ (withdrawPreview.fee || 0).toFixed(2) }}</span>
            </div>
            <div class="info-item" v-if="withdrawPreview">
              <span class="info-label">手续费率：</span>
              <span class="highlight">{{ ((withdrawPreview.feeRate || 0) * 100).toFixed(2) }}%</span>
            </div>
            <div class="info-item total" v-if="withdrawPreview">
              <span class="info-label">实际到账：</span>
              <span class="total-amount">¥{{ (withdrawPreview.total || 0).toFixed(2) }}</span>
            </div>
            <div class="balance-info" v-if="withdrawPreview">
              <span class="info-label">当前余额：</span>
              <span class="info-value">¥{{ walletInfo.balance.toFixed(2) }}</span>
            </div>
            <div class="loading-text" v-if="!withdrawPreview && withdrawAmount > 0">
              正在计算手续费...
            </div>
          </div>
          <div class="rules-link">
            <a href="#" @click.prevent="showWithdrawRules = true">
              <i class="fas fa-info-circle"></i>
              查看充值与奖励规则
            </a>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showWithdrawModal = false">取消</button>
          <button class="confirm-btn" @click="handleWithdraw" :disabled="!withdrawAmount || withdrawAmount <= 0">
            确认提现
          </button>
        </div>
      </div>
    </div>

    <!-- 充值规则弹窗 -->
    <div v-if="showRechargeRules" class="modal-overlay" @click.self="closeRulesModal">
      <div class="modal-content rules-modal">
        <div class="modal-header">
          <h3>充值与奖励规则</h3>
          <i class="fas fa-times close-btn" @click="closeRulesModal"></i>
        </div>
        <div class="modal-body rules-content">
          <div class="rule-text" v-if="rulesText">
            <div v-html="rulesText"></div>
          </div>
          <div class="rule-text" v-else>
            <p>1. 充值金额需大于0.01元</p>
            <p>2. 充值奖励率：{{ rechargeRules ? (rechargeRules.rewardRate * 100).toFixed(2) + '%' : '待配置' }}</p>
            <p>3. 奖励金额将在充值成功后立即到账</p>
            <p>4. 提现时将扣除之前获得的充值奖励</p>
            <p>5. 提现手续费率：{{ withdrawRules ? (withdrawRules.feeRate * 100).toFixed(2) + '%' : '待配置' }}</p>
            <p>6. 具体规则以平台公告为准</p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="confirm-btn" @click="closeRulesModal">返回</button>
        </div>
      </div>
    </div>

    <!-- 提现规则弹窗 -->
    <div v-if="showWithdrawRules" class="modal-overlay" @click.self="closeRulesModal">
      <div class="modal-content rules-modal">
        <div class="modal-header">
          <h3>充值与奖励规则</h3>
          <i class="fas fa-times close-btn" @click="closeRulesModal"></i>
        </div>
        <div class="modal-body rules-content">
          <div class="rule-text" v-if="rulesText">
            <div v-html="rulesText"></div>
          </div>
          <div class="rule-text" v-else>
            <p>1. 提现金额需大于0.01元</p>
            <p>2. 提现手续费率：{{ withdrawRules ? (withdrawRules.feeRate * 100).toFixed(2) + '%' : '待配置' }}</p>
            <p>3. 提现时将扣除之前获得的充值奖励</p>
            <p>4. 充值奖励率：{{ rechargeRules ? (rechargeRules.rewardRate * 100).toFixed(2) + '%' : '待配置' }}</p>
            <p>5. 具体规则以平台公告为准</p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="confirm-btn" @click="closeRulesModal">返回</button>
        </div>
      </div>
    </div>

    <!-- VIP信息弹窗 -->
    <div v-if="showVipModal" class="modal-overlay" @click.self="showVipModal = false">
      <div class="modal-content vip-modal">
        <div class="modal-header">
          <h3>VIP会员信息</h3>
          <i class="fas fa-times close-btn" @click="showVipModal = false"></i>
        </div>
        <div class="modal-body vip-content">
          <div class="vip-levels">
            <div 
              v-for="level in vipLevels" 
              :key="level.id" 
              class="vip-level-item"
              :class="{ active: selectedVipLevel?.id === level.id }"
              @click="selectedVipLevel = level"
            >
              <div class="level-header">
                <i class="fas fa-crown" :style="{ color: level.color }"></i>
                <span class="level-name">{{ level.name }}</span>
              </div>
              
              <!-- 贷款额度显示 -->
              <div class="loan-amount-section">
                <div class="loan-title">
                  <i class="fas fa-credit-card"></i>
                  <span>可贷款额度</span>
                </div>
                <div class="loan-amount">¥{{ (level.loanAmount || 0).toLocaleString() }}</div>
              </div>
              
              <div class="level-benefits">
                <span>{{ level.description }}</span>
              </div>
              <div class="level-price">¥{{ level.price }}/月</div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showVipModal = false">取消</button>
          <button class="confirm-btn" @click="handleVipUpgrade" :disabled="!selectedVipLevel">
            选择此等级
          </button>
        </div>
      </div>
    </div>

    <!-- VIP支付方式选择弹窗 -->
    <div v-if="showVipPaymentModal" class="modal-overlay" @click.self="cancelVipPayment">
      <div class="modal-content payment-modal">
        <div class="modal-header">
          <h3>VIP升级支付</h3>
          <i class="fas fa-times close-btn" @click="cancelVipPayment"></i>
        </div>
        <div class="modal-body">
          <div class="vip-summary" v-if="selectedVipLevel">
            <div class="summary-item">
              <span class="summary-label">选择等级：</span>
              <span class="summary-value">{{ selectedVipLevel.name }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">升级费用：</span>
              <span class="summary-value price">¥{{ selectedVipLevel.price }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">可贷款额度：</span>
              <span class="summary-value loan-highlight">¥{{ (selectedVipLevel.loanAmount || 0).toLocaleString() }}</span>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="cancel-btn" @click="cancelVipPayment">取消</button>
          <button 
            class="confirm-btn" 
            @click="confirmVipPayment"
          >
            确认支付
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/trade/utils/tradeRequest';
import { toast } from '@/trade/utils/toast';
import BackButton from '../components/BackButton.vue';
import eventBus from '../utils/eventBus';

export default {
  name: 'Wallet',
  components: {
    BackButton
  },
  setup() {
    const router = useRouter();

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

    // 移除localStorage相关函数，只使用后端真实数据
    const loading = ref(true);
    const walletInfo = ref({
      balance: 0,
      isVip: false,
      overdraftLimit: 0,
      usedOverdraft: 0
    });
    const showRechargeModal = ref(false);
    const showWithdrawModal = ref(false);
    const showRechargeRules = ref(false);
    const showWithdrawRules = ref(false);
    const showVipModal = ref(false);
    const rechargeAmount = ref(null);
    const withdrawAmount = ref(null);
    const rechargeRules = ref(null);
    const withdrawRules = ref(null);
    const selectedVipLevel = ref(null);
    const rulesText = ref(null); // 规则文本，可以从后端获取
    const showVipPaymentModal = ref(false); // VIP支付方式选择弹窗
    const vipPaymentMethod = ref('wallet'); // VIP支付方式：wallet/thirdparty
    const vipLevels = ref([]); // 从后端获取VIP等级数据
    const rechargePreview = ref(null); // 充值预览数据
    const withdrawPreview = ref(null); // 提现预览数据
    const rechargePaymentMethod = ref('wechat'); // 充值支付方式：wechat/alipay，默认微信
    const withdrawPaymentMethod = ref('wechat'); // 提现方式：wechat/alipay，默认微信

    // WebSocket相关
    const webSocket = ref(null);
    const isConnected = ref(false);

    // 获取钱包信息（后端优先；本地模拟仅注释保留）
    const fetchWalletInfo = async () => {
      try {
        loading.value = true;
        // 前端模拟备用：
        // const savedWalletInfo = localStorage.getItem(getWalletInfoKey());
        // if (savedWalletInfo) { walletInfo.value = JSON.parse(savedWalletInfo); return; }

        // 后端调用（Apifox: GET /api/wallet/message）
        const response = await request.get('/api/wallet/message');
        
        if (response && response.success) {
          const d = response.data || {};
          walletInfo.value = {
            id: d.id,
            userId: d.userId,
            createTime: d.createTime,
            status: d.status, // 钱包状态 0-正常 1-冻结
            vipLevel: d.vipLevel ?? 0, // vip级别 0-非vip
            balance: d.balance ?? 0, // 余额
            overdraftAmount: d.overdraftAmount ?? 0, // 可透支金额
            overdrawnAmount: d.overdrawnAmount ?? 0, // 已透支金额
            username: d.username, // 所属用户名
            vipName: d.vipName, // vip名
            vipDescription: d.vipDescription, // vip描述
            overdraftLimit: d.overdraftAmount ?? 0, // 可透支金额
            // 兼容旧版本字段
            isVip: (d.vipLevel ?? 0) > 0,
            usedOverdraft: d.overdrawnAmount ?? 0
          };
        } else {
          // 检查是否是钱包不存在的错误
          if (response && (response.code === 'VIRTUAL_WALLET_MISSED' || 
              (response.message && response.message.includes('不存在')))) {
            // 钱包不存在，需要开通
            console.log('钱包不存在，需要开通');
            // 这里不自动开通，让用户手动选择
          } else {
            toast.error('获取钱包信息失败');
          }
        }
      } catch (error) {
        console.error('获取钱包信息失败:', error);
        if (error.response?.status === 404) {
          await createWallet();
        } else {
          toast.error('获取钱包信息失败');
        }
      } finally {
        loading.value = false;
      }
    };

    // 获取VIP规则（后端）
const fetchVipRules = async () => {
  try {
    const response = await request.get('/api/wallet/vip/rule');
    console.log('VIP规则接口响应:', response);
    
    if (response && response.success && Array.isArray(response.data)) {
      const palette = ['#FFD700', '#FF6B6B', '#9B59B6', '#3498DB'];
      vipLevels.value = response.data.map((it, idx) => {
        // 使用后端返回的实际数据
        const vipId = it.id ?? idx + 1;
        
        return {
          id: vipId,
          name: it.name ?? `VIP${vipId}`,
          price: it.cost ?? 0,
          color: palette[idx % palette.length],
          loanAmount: it.overdraftLimit ?? 0, // 使用后端的overdraftLimit字段
          // benefits: it.description
          //   ? it.description.split(/[,，；;]/).map(s => s.trim()).filter(Boolean)
          //   : [`透支额度：¥${(it.overdraftLimit || 0).toLocaleString()}`, 'VIP专属服务', '优先客服支持'],
          description: it.description // 保存原始描述
        };
      });
      
      // 如果有VIP等级数据，默认选择第一个
      if (vipLevels.value.length > 0) {
        selectedVipLevel.value = vipLevels.value[0];
      }
    } else {
      console.warn('VIP规则接口返回数据格式异常:', response);
      // 如果接口异常，使用默认数据避免页面空白
      vipLevels.value = getDefaultVipLevels();
    }
  } catch (error) {
    console.error('获取VIP规则失败:', error);
    toast.error('获取VIP信息失败');
    // 出错时使用默认数据
    vipLevels.value = getDefaultVipLevels();
  }
};

    // 获取钱包贷款列表
    const fetchWalletLoans = async () => {
      try {
        const response = await request.get('/api/wallet/loan/list');
        if (response && response.success && Array.isArray(response.data)) {
          return response.data.map(loan => ({
            id: loan.id,
            walletId: loan.walletId,
            createTime: loan.createTime,
            repayTime: loan.repayTime,
            loanAmount: loan.loanAmount,
            status: loan.repayTime ? '已还款' : '未还款'
          }));
        }
        return [];
      } catch (error) {
        console.error('获取钱包贷款列表失败:', error);
        toast.error('获取贷款信息失败');
        return [];
      }
    };

    // 查看贷款详情
    const viewLoanDetails = async () => {
      try {
        const loans = await fetchWalletLoans();
        if (loans.length === 0) {
          toast.info('暂无贷款记录');
          return;
        }
        
        // 简单展示贷款信息（可以后续改为弹窗或跳转到详情页）
        const loanInfo = loans.map(loan => 
          `贷款ID: ${loan.id}\n金额: ¥${loan.loanAmount}\n创建时间: ${loan.createTime}\n状态: ${loan.status}`
        ).join('\n\n');
        
        alert(`贷款记录:\n\n${loanInfo}`);
      } catch (error) {
        console.error('查看贷款详情失败:', error);
        toast.error('查看贷款详情失败');
      }
    };

    // 创建钱包（后端；本地模拟以注释保留）
    const createWallet = async () => {
      try {
        // 前端模拟备用：
        // const newWalletInfo = { balance: 0, isVip: false, overdraftLimit: 0, usedOverdraft: 0 };
        // walletInfo.value = newWalletInfo;
        // localStorage.setItem(getWalletInfoKey(), JSON.stringify(newWalletInfo));
        // addTransactionRecord({ transactionType: 'create', amount: 0, reason: '钱包开通成功' });
        // toast.success('钱包已激活'); return;

        const response = await request.get('/api/wallet/open');
        if (response && response.success) {
          // 响应数据中的data字段是integer类型，表示钱包ID或状态
          const walletId = response.data;
          toast.success('钱包开通成功');
          // 开通成功后重新获取钱包信息
          await fetchWalletInfo();
        } else {
          toast.error('钱包开通失败：' + (response.message || '未知错误'));
        }
      } catch (error) {
        console.error('开通钱包失败:', error);
        toast.error('钱包开通失败，请重试');
      }
    };

    // 移除假数据记录函数，只使用后端真实数据

    // 获取充值/提现规则（后端优先）
    const fetchRules = async () => {
      try {
        // 前端模拟备用：
        // rechargeRules.value = { rewardRate: 0.1 };
        // withdrawRules.value = { feeRate: 0.1 };
        // rulesText.value = null; return;

        const response = await request.get('/api/wallet/rule');
        if (response && response.success) {
          // 根据接口规范，data字段是string类型，包含规则文本
          const rulesString = response.data || '';
          rulesText.value = rulesString;
          
          // 尝试解析规则文本中的数值，或使用默认值
          // 这里可以根据实际返回的规则文本格式进行解析
          try {
            // 如果规则文本是JSON格式，尝试解析
            const rulesObj = JSON.parse(rulesString);
            rechargeRules.value = rulesObj.rechargeRules || { rewardRate: rulesObj.recharge_rate ?? 0.1 };
            withdrawRules.value = rulesObj.withdrawRules || { feeRate: rulesObj.withdraw_fee ?? 0.1 };
          } catch (parseError) {
            // 如果不是JSON格式，使用默认规则
            console.log('规则文本不是JSON格式，使用默认规则');
            rechargeRules.value = { rewardRate: 0.1 };
            withdrawRules.value = { feeRate: 0.1 };
          }
        }
      } catch (error) {
        console.error('获取规则失败:', error);
        rechargeRules.value = { rewardRate: 0.1 };
        withdrawRules.value = { feeRate: 0.1 };
        rulesText.value = '暂无规则信息';
      }
    };

    // 关闭规则弹窗
    const closeRulesModal = () => {
      showRechargeRules.value = false;
      showWithdrawRules.value = false;
    };

    // 获取充值/提现预览信息
    const getPreview = async (amount, option) => {
      try {
        const response = await request.get('/api/wallet/preview', {
          params: { 
            amount: amount,
            option: option // 0-充值 1-提现
          }
        });
        if (response && response.success && response.data) {
          return {
            amount: response.data.amount,
            fee: response.data.fee,
            total: response.data.total,
            feeRate: response.data.feeRate,
            isOver: response.data.isOver || false
          };
        }
        return null;
      } catch (error) {
        console.error('获取预览信息失败:', error);
        return null;
      }
    };

    // 处理充值
    const handleRecharge = async () => {
      if (!rechargeAmount.value || rechargeAmount.value <= 0) {
        toast.warning('请输入有效的充值金额');
        return;
      }
      
      try {
        const paymentMethodName = rechargePaymentMethod.value === 'wechat' ? '微信支付' : '支付宝支付';
        console.log('发起充值请求:', rechargeAmount.value, '支付方式:', paymentMethodName);
        
        const response = await request.get('/api/wallet/recharge', {
          params: { amount: rechargeAmount.value }
        });
        console.log('充值接口响应:', response);
        
        if (response && response.success) {
          toast.success(`充值成功！已通过${paymentMethodName}充值`);
          showRechargeModal.value = false;
          rechargeAmount.value = null;
          await fetchWalletInfo();
          // 通知交易明细页面刷新
          console.log('充值成功，触发交易明细刷新');
          eventBus.emit('transactionUpdated');
        } else {
          console.log('充值失败:', response);
          toast.error(response?.message || '充值失败');
        }
      } catch (error) {
        toast.error('充值失败，请重试');
      }
    };

    // 处理提现
    const handleWithdraw = async () => {
      if (!withdrawAmount.value || withdrawAmount.value <= 0) {
        toast.warning('请输入有效的提现金额');
        return;
      }
      
      try {
        const withdrawMethodName = withdrawPaymentMethod.value === 'wechat' ? '微信钱包' : '支付宝账户';
        console.log('发起提现请求:', withdrawAmount.value, '提现方式:', withdrawMethodName);
        
        const response = await request.get('/api/wallet/withdrawal', {
          params: { amount: withdrawAmount.value }
        });
        
        if (response && response.success) {
          toast.success(`提现成功！将提现至${withdrawMethodName}`);
          showWithdrawModal.value = false;
          withdrawAmount.value = null;
          await fetchWalletInfo();
          // 通知交易明细页面刷新
          eventBus.emit('transactionUpdated');
        } else {
          toast.error(response?.message || '提现失败');
        }
      } catch (error) {
        toast.error('提现失败，请重试');
      }
    };

    // 跳转到交易明细
    const navigateToTransactions = () => {
      router.push('/trade/wallet/transactions');
    };

    // 跳转到贷款记录
    const navigateToLoans = () => {
      router.push('/trade/wallet/loans');
    };

    // 处理VIP升级 - 第一步：选择支付方式
    const handleVipUpgrade = async () => {
      if (!selectedVipLevel.value) {
        toast.warning('请选择VIP等级');
        return;
      }
      
      // 显示支付方式选择弹窗
      showVipModal.value = false;
      showVipPaymentModal.value = true;
    };

    // 确认VIP支付
    const confirmVipPayment = async () => {
      if (!selectedVipLevel.value) {
        toast.warning('请选择VIP等级');
        return;
      }

      try {
        const response = await request.get('/api/wallet/vip/apply', {
          params: { vipLevel: selectedVipLevel.value.id }
        });
        
        if (response && response.success) {
          toast.success('VIP升级成功！');
          showVipPaymentModal.value = false;
          selectedVipLevel.value = null;
          await fetchWalletInfo();
        } else {
          toast.error(response?.message || 'VIP升级失败');
        }
      } catch (error) {
        toast.error('VIP升级失败，请重试');
      }
    };

    // 取消VIP支付
    const cancelVipPayment = () => {
      showVipPaymentModal.value = false;
      selectedVipLevel.value = null;
      vipPaymentMethod.value = 'wallet';
    };

    // 监听充值金额变化，自动获取预览
    watch(rechargeAmount, async (newAmount) => {
      if (newAmount && newAmount > 0) {
        rechargePreview.value = await getPreview(newAmount, 0);
      } else {
        rechargePreview.value = null;
      }
    });

    // 监听提现金额变化，自动获取预览
    watch(withdrawAmount, async (newAmount) => {
      if (newAmount && newAmount > 0) {
        withdrawPreview.value = await getPreview(newAmount, 1);
      } else {
        withdrawPreview.value = null;
      }
    });

    // 初始化WebSocket
    const initWebSocket = () => {
      const user = getCurrentUser();
      if (!user || !user.id) {
        console.warn('WebSocket初始化失败: 用户信息不存在');
        return;
      }

      try {
        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${wsProtocol}//localhost:8086/ws/${user.id}`;
        
        webSocket.value = new WebSocket(wsUrl);

        webSocket.value.onopen = () => {
          console.log('Wallet WebSocket连接成功');
          isConnected.value = true;
        };

        webSocket.value.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data);
            handleWebSocketMessage(message);
          } catch (err) {
            console.error('解析WebSocket消息失败:', err);
          }
        };

        webSocket.value.onclose = (event) => {
          console.log('Wallet WebSocket连接关闭', event.code);
          isConnected.value = false;
          if (event.code !== 1000) {
            setTimeout(initWebSocket, 3000);
          }
        };

        webSocket.value.onerror = (err) => {
          console.error('Wallet WebSocket错误:', err);
          isConnected.value = false;
        };
      } catch (err) {
        console.error('初始化WebSocket失败:', err);
      }
    };

    // 处理WebSocket消息
    const handleWebSocketMessage = (message) => {
      console.log('收到Wallet WebSocket消息:', message);
      
      if (message.type === 'wallet_opened') {
        toast.success('钱包开通成功！');
        // 重新获取钱包信息
        fetchWalletInfo();
      }
    };

    onMounted(() => {
      fetchWalletInfo();
      fetchRules();
      fetchVipRules();
      initWebSocket(); // 初始化WebSocket
    });

    onUnmounted(() => {
      if (webSocket.value) {
        webSocket.value.close();
      }
    });

    return {
      loading,
      walletInfo,
      showRechargeModal,
      showWithdrawModal,
      showRechargeRules,
      showWithdrawRules,
      rechargeAmount,
      withdrawAmount,
      rechargeRules,
      withdrawRules,
      handleRecharge,
      handleWithdraw,
      navigateToTransactions,
      navigateToLoans,
      showVipModal,
      selectedVipLevel,
      vipLevels,
      handleVipUpgrade,
      rulesText,
      closeRulesModal,
      fetchWalletLoans,
      viewLoanDetails,
      getPreview,
      showVipPaymentModal,
      vipPaymentMethod,
      confirmVipPayment,
      cancelVipPayment,
      rechargePreview,
      withdrawPreview,
      rechargePaymentMethod,
      withdrawPaymentMethod
    };
  }
};
</script>

<style scoped>
.wallet-container {
  max-width: 600px;
  margin: 0 auto;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 20px;
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

.wallet-content {
  padding-top: 14vw;
  padding: 14vw 4vw 4vw;
}

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4vw;
  padding: 6vw;
  margin-bottom: 4vw;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.balance-label {
  font-size: 3.6vw;
  opacity: 0.9;
  margin-bottom: 2vw;
}

.balance-amount {
  font-size: 10vw;
  font-weight: bold;
  margin-bottom: 4vw;
}

.balance-actions {
  display: flex;
  gap: 3vw;
}

.action-btn {
  flex: 1;
  padding: 3vw;
  border: none;
  border-radius: 2vw;
  font-size: 3.6vw;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5vw;
  transition: all 0.3s;
}

.recharge-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
}

.recharge-btn:active {
  background: rgba(255, 255, 255, 0.3);
}

.withdraw-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
}

.withdraw-btn:active {
  background: rgba(255, 255, 255, 0.3);
}

.overdraft-card {
  background: white;
  border-radius: 4vw;
  padding: 4vw;
  margin-bottom: 4vw;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.overdraft-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3vw;
}

.overdraft-label {
  font-size: 4vw;
  font-weight: 500;
  color: #333;
}

.vip-badge {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 1vw 2vw;
  border-radius: 1vw;
  font-size: 2.8vw;
  font-weight: bold;
}

.overdraft-info {
  margin-bottom: 3vw;
}

.overdraft-item {
  display: flex;
  justify-content: space-between;
  padding: 2vw 0;
  font-size: 3.6vw;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}

.overdraft-item:last-child {
  border-bottom: none;
}

.overdraft-amount {
  color: #0097FF;
  font-weight: 500;
}

.overdraft-total {
  color: #666;
}

.overdraft-used {
  color: #ff6b6b;
  font-weight: 500;
}

.repay-btn {
  width: 100%;
  padding: 3vw;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  border: none;
  border-radius: 2vw;
  font-size: 3.6vw;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5vw;
  transition: all 0.3s;
}

.repay-btn:active {
  transform: scale(0.98);
}

.function-section {
  background: white;
  border-radius: 4vw;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.function-item {
  display: flex;
  align-items: center;
  padding: 4vw;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}

.function-item:last-child {
  border-bottom: none;
}

.function-item:active {
  background: #f5f7fa;
}

.function-icon {
  width: 8vw;
  height: 8vw;
  background: #e6f0ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 3vw;
}

.function-icon i {
  font-size: 4vw;
  color: #0097FF;
}

.function-text {
  flex: 1;
  font-size: 4vw;
  color: #333;
}

.function-item i.fa-chevron-right {
  color: #ccc;
  font-size: 3.6vw;
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

/* 规则弹窗应该在充值/提现弹窗之上 */
.rules-modal {
  z-index: 2001;
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

.input-group {
  margin-bottom: 4vw;
}

.input-group label {
  display: block;
  font-size: 3.6vw;
  color: #666;
  margin-bottom: 2vw;
}

.input-group input {
  width: 100%;
  padding: 3vw;
  border: 1px solid #ddd;
  border-radius: 2vw;
  font-size: 4vw;
  box-sizing: border-box;
}

.reward-info,
.fee-info,
.repay-info {
  background: #f5f7fa;
  padding: 3vw;
  border-radius: 2vw;
  margin-bottom: 3vw;
}

.info-section-title {
  font-size: 4vw;
  font-weight: 600;
  color: #333;
  margin-bottom: 3vw;
  padding-bottom: 2vw;
  border-bottom: 1px solid #e0e0e0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2.5vw 0;
  font-size: 3.6vw;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item.total-item {
  margin-top: 2vw;
  padding-top: 3vw;
  border-top: 2px solid #e0e0e0;
  border-bottom: none;
  font-weight: 600;
}

.info-label {
  color: #666;
  flex: 1;
}

.info-value {
  color: #333;
  font-weight: 500;
}

.highlight {
  color: #0097FF;
  font-weight: 500;
}

.reward-amount {
  color: #52c41a;
  font-weight: 600;
}

.fee-amount {
  color: #ff4d4f;
  font-weight: 600;
}

.total-amount {
  color: #0097FF;
  font-size: 4.2vw;
  font-weight: 700;
}

.rules-link {
  text-align: center;
  margin-top: 3vw;
  padding-top: 3vw;
  border-top: 1px dashed #e0e0e0;
}

.rules-link a {
  font-size: 3.2vw;
  color: #0097FF;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 1vw;
  transition: color 0.3s;
}

.rules-link a:hover {
  color: #0080e6;
}

.rules-link a i {
  font-size: 3.6vw;
}

.repay-method {
  margin-top: 4vw;
}

.method-label {
  display: block;
  font-size: 3.6vw;
  color: #666;
  margin-bottom: 2vw;
  font-weight: 500;
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
  border: 2px solid #ddd;
  border-radius: 2vw;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.method-option:hover {
  border-color: #0097FF;
  background: #f0f8ff;
}

.method-option.active {
  border-color: #0097FF;
  background: #e6f4ff;
}

.method-option input {
  margin-right: 2vw;
  width: 4vw;
  height: 4vw;
  cursor: pointer;
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

.method-content span {
  font-size: 3.6vw;
  color: #333;
}

.method-tip {
  margin-top: 2vw;
  padding: 2vw;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 2vw;
  display: flex;
  align-items: center;
  gap: 1.5vw;
  font-size: 3.2vw;
  color: #d46b08;
}

.method-tip i {
  font-size: 3.6vw;
  color: #fa8c16;
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

.rules-content {
  max-height: 50vh;
  overflow-y: auto;
}

.rule-text {
  font-size: 3.6vw;
  line-height: 1.8;
  color: #666;
}

.rule-text p {
  margin: 2vw 0;
}

/* VIP相关样式 */
.vip-promotion-card {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 4vw;
  padding: 4vw;
  margin-bottom: 4vw;
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
}

.vip-promotion-content {
  display: flex;
  align-items: center;
  margin-bottom: 3vw;
}

.vip-icon {
  font-size: 8vw;
  color: #FFD700;
  margin-right: 3vw;
}

.vip-text {
  flex: 1;
  color: white;
}

.vip-title {
  font-size: 4.5vw;
  font-weight: bold;
  margin-bottom: 1vw;
}

.vip-desc {
  font-size: 3.2vw;
  opacity: 0.9;
}

.vip-btn {
  width: 100%;
  padding: 3vw;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 2vw;
  font-size: 3.6vw;
  font-weight: 500;
  cursor: pointer;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
}

.vip-btn:active {
  background: rgba(255, 255, 255, 0.3);
}

.vip-modal {
  max-height: 85vh;
}

.vip-content {
  max-height: 60vh;
  overflow-y: auto;
}

.vip-levels {
  display: flex;
  flex-direction: column;
  gap: 3vw;
}

.vip-level-item {
  border: 2px solid #e0e0e0;
  border-radius: 3vw;
  padding: 4vw;
  cursor: pointer;
  transition: all 0.3s;
}

.vip-level-item.active {
  border-color: #0097FF;
  background: #f0f8ff;
}

.level-header {
  display: flex;
  align-items: center;
  margin-bottom: 3vw;
}

.level-header i {
  font-size: 5vw;
  margin-right: 2vw;
}

.level-name {
  font-size: 4.5vw;
  font-weight: bold;
  color: #333;
}

.level-benefits {
  margin-bottom: 2vw;
}

.benefit-item {
  display: flex;
  align-items: center;
  margin-bottom: 1.5vw;
  font-size: 3.6vw;
  color: #666;
}

.benefit-item i {
  color: #52c41a;
  margin-right: 2vw;
  font-size: 3.2vw;
}

.level-price {
  text-align: right;
  font-size: 4.5vw;
  font-weight: bold;
  color: #0097FF;
}

/* VIP支付弹窗样式 */
.payment-modal {
  max-height: 85vh;
}

.vip-summary {
  background: #f8f9fa;
  border-radius: 2vw;
  padding: 4vw;
  margin-bottom: 4vw;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2vw;
  font-size: 3.6vw;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-label {
  color: #666;
}

.summary-value {
  color: #333;
  font-weight: 500;
}

.summary-value.price {
  color: #0097FF;
  font-weight: bold;
  font-size: 4vw;
}

.summary-value.loan-highlight {
  color: #52c41a;
  font-weight: bold;
  font-size: 4vw;
}

.payment-methods {
  margin-bottom: 4vw;
}

.method-title {
  font-size: 4vw;
  font-weight: 500;
  color: #333;
  margin-bottom: 3vw;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 3vw;
}

.payment-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4vw;
  border: 2px solid #e9ecef;
  border-radius: 2vw;
  cursor: pointer;
  transition: all 0.3s;
}

.payment-option.active {
  border-color: #0097FF;
  background: rgba(0, 151, 255, 0.05);
}

.option-content {
  display: flex;
  align-items: center;
  gap: 3vw;
}

.option-content i {
  font-size: 5vw;
  color: #0097FF;
}

.option-info {
  display: flex;
  flex-direction: column;
}

.option-name {
  font-size: 4vw;
  font-weight: 500;
  color: #333;
  margin-bottom: 1vw;
}

.option-desc {
  font-size: 3.2vw;
  color: #666;
}

.check-icon {
  font-size: 5vw;
  color: #0097FF;
  opacity: 0;
  transition: opacity 0.3s;
}

.payment-option.active .check-icon {
  opacity: 1;
}

.payment-notice {
  display: flex;
  align-items: flex-start;
  gap: 2vw;
  padding: 3vw;
  background: #e6f7ff;
  border-radius: 2vw;
  font-size: 3.4vw;
  color: #0050b3;
}

.payment-notice i {
  font-size: 4vw;
  margin-top: 0.5vw;
}

.payment-notice .insufficient {
  color: #ff4d4f;
}

/* 支付方式显示样式 */
.payment-method-display {
  display: flex;
  align-items: center;
  gap: 1vw;
}

.payment-method-display i {
  font-size: 4vw;
}

/* 预览信息样式 */
.preview-info {
  background: #f8f9fa;
  border-radius: 2vw;
  padding: 4vw;
  margin: 4vw 0;
  border: 1px solid #e9ecef;
}

.info-section-title {
  font-size: 4vw;
  font-weight: 600;
  color: #333;
  margin-bottom: 3vw;
  text-align: center;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2vw;
  font-size: 3.6vw;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #666;
}

.info-value {
  color: #333;
  font-weight: 500;
}

.highlight {
  color: #0097FF;
  font-weight: 600;
}

.reward-amount {
  color: #52c41a;
}

.fee-amount {
  color: #ff4d4f;
}

.total-amount {
  color: #333;
  font-weight: bold;
  font-size: 4vw;
}

.info-item.total {
  border-top: 1px solid #e9ecef;
  padding-top: 2vw;
  margin-top: 2vw;
}

.balance-info {
  background: #e6f7ff;
  padding: 3vw;
  border-radius: 1.5vw;
  margin-top: 2vw;
  font-size: 3.4vw;
}

.overdraft-info {
  color: #0097FF;
  font-weight: 500;
}

.loading-text {
  text-align: center;
  color: #999;
  font-size: 3.4vw;
  padding: 2vw 0;
}

/* 贷款额度样式 */
.loan-amount-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2vw;
  padding: 3vw;
  margin: 3vw 0;
  color: white;
  text-align: center;
}

.loan-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5vw;
  font-size: 3.4vw;
  margin-bottom: 2vw;
  opacity: 0.9;
}

.loan-title i {
  font-size: 3.8vw;
}

.loan-amount {
  font-size: 5.5vw;
  font-weight: bold;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}
</style>