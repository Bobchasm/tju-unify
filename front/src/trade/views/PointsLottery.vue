<template>
 <div class="back-btn-container">
    <BackButton style="margin-top: 2vw;"/>
  </div>
 <div class="details-container">
  <div class="header app-header-fixed">
   <h3>积分抽奖</h3>
  </div>
  <div class="points-balance-card card">
   <div class="points-info">
    <div class="points-row">
     <span class="label">当前积分</span>
     <span class="value">{{ formatPoints(lotteryInfo.totalPoints || 0) }}</span>
    </div>
    <div class="points-row">
     <span class="label">可用积分</span>
     <span class="value">{{ formatPoints(lotteryInfo.availablePoints || 0) }}</span>
    </div>
   </div>
   <button class="earn-btn" @click="goToPointsPage">更多抽奖机会</button>
  </div>

  <div class="member-info-card card">
   <div class="member-text">
    <p class="member-name">当前等级：<strong>{{ lotteryInfo.memberLevelName || '加载中...' }}</strong></p>
    <p class="chances-text">
     本月免费机会：
     <span class="chances-count">{{ lotteryInfo.remainingChances }}</span> 次 / {{ lotteryInfo.monthlyLimit }}
    </p>
   </div>
   </div>

  <div class="lottery-area card">
   <h4>🎉 专属奖池</h4>
   
   <div class="rules-link" @click="openRules">活动规则</div>

   <div class="loading-overlay" v-if="loading.info || loading.draw">
    <div class="spinner"></div>
    <p v-if="loading.info">加载抽奖信息...</p>
    <p v-else-if="loading.draw">抽奖进行中，请稍候...</p>
   </div>
   
   <div v-else-if="errorMessage" class="error-state">
    <p>⚠️ {{ errorMessage }}</p>
    <button @click="fetchInitialData">重试</button>
   </div>

   <div v-else-if="!loading.info" class="lottery-content">
    <div class="prize-pool-display">
     <span v-for="(item, index) in simplePrizes" :key="index" class="prize-tag">
      {{ item.name }}
     </span>
    </div>

    <div class="lottery-grid-container">
     <div 
      v-for="(prize, index) in prizeGrid" 
      :key="index" 
      class="grid-item"
      :class="{ 
       'center-button': prize.isButton,
       'active-prize': prize.index === activeIndex,
       'winning-flash': prize.index === finalPrizeIndex && !loading.draw
      }"
      @click="prize.isButton ? startLottery() : null"
     >
      <template v-if="prize.isButton">
       <div 
        class="draw-center-button"
        :class="{ 'disabled': !canStartLottery || loading.draw }"
       >
        <span>点击抽奖</span>
        <span class="cost-text">{{ drawCost }}</span>
       </div>
      </template>
      <template v-else>
       <div class="prize-content">
        <span class="prize-icon">{{ getPrizeIcon(prize.lotteryType) }}</span>
        <span class="prize-name">{{ prize.name || '神秘奖品' }}</span>
        <span class="prize-points" v-if="prize.pointsReward > 0">
         {{ formatPoints(prize.pointsReward) }} 积分
        </span>
       </div>
      </template>
     </div>
    </div>
   </div>
  </div>

  <div class="history-section card">
   <h4>📋 中奖记录 (近 {{ lotteryRecords.length }} 条)</h4>
   <p v-if="loading.records" class="empty-state">加载中...</p>
   <ul class="prize-list" v-else>
    <li v-for="record in lotteryRecords" :key="record.id">
     <span :class="{ 'prize-win': record.lotteryType !== 0 }">
      {{ formatRecord(record) }}
     </span>
     <span class="time">{{ formatTime(record.createTime) }}</span>
    </li>
    <li class="empty-state" v-if="lotteryRecords.length === 0">暂无中奖记录</li>
   </ul>
  </div>
  
  <teleport to="body">
   <div v-if="showRules" class="modal-mask" @click="showRules = false">
    <div class="modal-wrapper">
     <div class="modal-container" @click.stop>
      <div class="modal-header">
       <h3>活动规则</h3>
       <button class="close-btn" @click="showRules = false">×</button>
      </div>
      <div class="modal-body">
       <p>1. <span class="rule-title">会员免费机会：</span></p>
       <p class="rule-content" v-html="memberRules"></p>

       <p>2. <span class="rule-title">当前奖池：</span>{{ prizeDescriptions }}。</p>

       <p>4. <span class="rule-title">积分有效期：</span>活动中获得的积分有效期为 15 天。</p>
       <p>5. <span class="rule-title">其他：</span>中奖记录仅显示最近 5 条。最终解释权归本公司所有。</p>

       <p class="level-info">当前等级：<span style="color: var(--primary-color);">{{ lotteryInfo.memberLevelName || '加载中...' }}</span></p>
      </div>
     </div>
    </div>
   </div>
  </teleport>

  <!-- 钱包开通弹窗 -->
  <teleport to="body">
    <div v-if="showWalletActivateModal" class="modal-mask" @click="showWalletActivateModal = false">
      <div class="modal-wrapper">
        <div class="modal-container wallet-activate-modal" @click.stop>
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
  </teleport>
 </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router'; 
// 假设这是您的工具/请求函数，请确保路径正确
import request from '@/trade/utils/tradeRequest'; 
import { toast } from '@/trade/utils/toast';
import BackButton from '../components/BackButton.vue'; 

const WIN_PRIZE_INDICES = [0, 1, 2, 5, 8, 7, 6, 3].filter(i => i !== 4); // 中奖格子索引集合
const NO_WIN_PRIZE_INDICES = [6, 8]; // 非中奖格子索引集合 (左下和右下)
const GRID_SEQUENCE = [0, 1, 2, 5, 8, 7, 6, 3]; 

// --- 状态管理 ---
const showRules = ref(false); 
const loading = ref({
  info: true, 
  records: true, 
  draw: false,
});
const errorMessage = ref('');
const activeIndex = ref(-1); 
const finalPrizeIndex = ref(-1); 
const prizeGrid = ref([]);
const lotteryRecords = ref([]);
const winningResult = ref(null); 

const lotteryInfo = ref({
  totalPoints: 0, 
  availablePoints: 0, 
  memberLevel: 0,
  memberLevelName: '普通用户',
  monthlyLimit: 0,
  usedChances: 0,
  remainingChances: 0,
  canLottery: false,
  prizes: [], // 存储后端返回的奖品列表
});

// --- 计算属性 (规则和抽奖状态) ---
const canStartLottery = computed(() => {
  // 仅允许在有免费机会时抽奖
  return lotteryInfo.value.remainingChances > 0;
});

const drawCost = computed(() => {
  // 仅显示免费机会
  return '免费机会'; 
});

const simplePrizes = computed(() => {
  // 奖池概览只展示非谢谢参与的奖品，取前三个
  return lotteryInfo.value.prizes
    .filter(p => p.lotteryType !== 0 && p.lotteryTypeName !== '没中奖')
    .slice(0, 3)
    .map(p => ({ name: p.lotteryTypeName }));
});

// 规则弹窗-会员机会描述
const memberRules = computed(() => {
  const level = lotteryInfo.value.memberLevel;
  let limitText = '暂无免费抽奖机会';
  switch (level) {
    case 1: limitText = '每月有 1 次免费抽奖机会'; break;
    case 2: limitText = '每月有 2 次免费抽奖机会'; break;
    case 3: limitText = '每月有 3 次免费抽奖机会'; break;
  }
  return limitText + '。免费机会每月重置，请及时使用。';
});

// 规则弹窗-奖池描述
const prizeDescriptions = computed(() => {
  const level = lotteryInfo.value.memberLevel;
  let pool = '';
  
  switch (level) {
    case 1: 
      pool = '没中奖、+20积分、+50积分、+100积分'; 
      break;
    case 2: 
      pool = '没中奖、+50积分、+100积分、积分翻倍'; 
      break;
    case 3: 
      pool = '没中奖、+100积分、+200积分、积分翻倍'; 
      break;
    case 0:
    default: 
      pool = '没中奖。'; 
      break;
  }
  
  return `当前奖池包含：${pool} `;
});

const router = useRouter(); 
const showWalletActivateModal = ref(false);

// 检测并创建钱包（复用个人信息页面的逻辑）
const checkAndCreateWallet = async () => {
  try {
    const response = await request.get('/api/wallet/message');
    if (response && response.success) {
      router.push({ path: '/trade/wallet' });
    } else {
      // 检查是否是钱包不存在的错误
      if (response && (response.code === 'VIRTUAL_WALLET_MISSED' || 
          (response.message && (response.message.includes('不存在') || response.message.includes('未开通'))))) {
        showWalletActivateModal.value = true;
      } else {
        toast.error('获取钱包信息失败');
      }
    }
  } catch (error) {
    // 检查是否是钱包不存在的错误
    const errorData = error.response?.data || error;
    const errorCode = errorData?.code || error.code;
    const errorMessage = errorData?.message || error.message || '';
    
    if (error.response?.status === 404 || 
        errorCode === 'VIRTUAL_WALLET_MISSED' ||
        errorMessage.includes('不存在') ||
        errorMessage.includes('未开通') ||
        errorMessage.includes('您还未开通钱包')) {
      showWalletActivateModal.value = true;
    } else {
      console.error('检查钱包失败:', error);
      toast.error('检查钱包状态失败');
    }
  }
};

// 激活钱包
const activateWallet = async () => {
  try {
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

const goToPointsPage = () => {
  checkAndCreateWallet();
};

const openRules = () => {
  showRules.value = true;
};

// --- 数据获取函数 (保持不变) ---

const fetchPointsAccount = async () => {
  try {
    const res = await request.get('/api/points/account'); 
    if (res.success && res.data) {
      lotteryInfo.value.totalPoints = res.data.totalPoints || 0; 
      lotteryInfo.value.availablePoints = res.data.availablePoints || 0; 
    } else {
      console.warn('获取积分账户失败:', res.message);
    }
  } catch (err) {
    console.error('获取积分账户网络错误:', err);
  }
};

/**
* 将后端返回的奖品数据格式化并填充到九宫格中。
* @param {Array} apiPrizes 后端返回的奖品列表
*/
const formatPrizesToGrid = (apiPrizes) => {
  const grid = [];
  // 九宫格的非按钮格子顺序 (顺时针，从 0 开始)
  const GRID_SEQUENCE_MAP = [0, 1, 2, 5, 8, 7, 6, 3]; 
  
  // 优化后的默认占位奖品模板（用于填充不足 8 个的情况）
  const defaultPrizeTemplates = [
    { id: 'EMPTY_1', lotteryTypeName: '再试试吧', lotteryType: 0, pointsReward: 0 },
    { id: 'EMPTY_2', lotteryTypeName: '幸运加持', lotteryType: 0, pointsReward: 0 },
    { id: 'EMPTY_3', lotteryTypeName: '谢谢参与', lotteryType: 0, pointsReward: 0 },
    { id: 'EMPTY_4', lotteryTypeName: '今日好运', lotteryType: 0, pointsReward: 0 },
  ];
  
  // 准备 8 个奖品数据 (优先使用后端返回的，不足则使用默认占位)
  let prizesToUse = [...apiPrizes];
  let defaultIndex = 0;
  while (prizesToUse.length < 8) {
    prizesToUse.push(defaultPrizeTemplates[defaultIndex % defaultPrizeTemplates.length]);
    defaultIndex++;
  }

  // 填充九宫格
  let prizeDataIndex = 0;
  for (let i = 0; i < 9; i++) {
    if (i === 4) {
      // 中心格：抽奖按钮
      grid.push({ index: 4, isButton: true, name: '抽奖' }); 
    } else {
      // 根据当前格子的索引 i，找到它在 GRID_SEQUENCE_MAP 中的位置，
      const sequenceIndex = GRID_SEQUENCE_MAP.findIndex(seq => seq === i);
      const finalPrizeData = prizesToUse[sequenceIndex]; 

      // 优化：根据奖励类型优化显示名称
      let prizeName = finalPrizeData?.lotteryTypeName || '神秘奖品';
      if (finalPrizeData?.lotteryType === 1 && finalPrizeData?.pointsReward > 0) {
        prizeName = `+${formatPoints(finalPrizeData.pointsReward)} 积分`;
      } else if (finalPrizeData?.lotteryType === 2) {
        prizeName = '积分翻倍';
      }
      
      grid.push({
        index: i,
        isButton: false,
        name: prizeName, // 确保这里使用 prizeName
        lotteryType: finalPrizeData?.lotteryType,
        pointsReward: finalPrizeData?.pointsReward || 0,
        prizeId: finalPrizeData?.id,
      });
      prizeDataIndex++;
    }
  }
  prizeGrid.value = grid;
};


const fetchInitialData = async () => {
  errorMessage.value = '';
  
  await fetchPointsAccount(); 
  
  loading.value.info = true;
  try {
    const res = await request.get('/api/points/lottery/info');
    if (res.success && res.data) {
      Object.assign(lotteryInfo.value, res.data);
      
      // 确保积分不被丢失（如果 /lottery/info 接口不返回积分）
      if (!res.data.totalPoints) lotteryInfo.value.totalPoints = lotteryInfo.value.totalPoints;
      if (!res.data.availablePoints) lotteryInfo.value.availablePoints = lotteryInfo.value.availablePoints;
      
      formatPrizesToGrid(res.data.prizes || []);
    } else {
      errorMessage.value = res.message || '获取抽奖信息失败';
      toast.error(errorMessage.value);
    }
  } catch (err) {
    console.error('获取抽奖信息失败:', err);
    errorMessage.value = '网络错误，无法获取抽奖信息';
  } finally {
    loading.value.info = false;
  }

  loading.value.records = true;
  try {
    const res = await request.get('/api/points/lottery/records', { params: { limit: 5 } });
    if (res.success && res.data) {
      lotteryRecords.value = res.data;
    } else {
      console.warn('获取抽奖记录失败:', res.message);
    }
  } catch (err) {
    console.error('获取抽奖记录失败:', err);
  } finally {
    loading.value.records = false;
  }
};


/**
* 简化后的抽奖逻辑：
*/
const startLottery = async () => {
  if (!canStartLottery.value) {
    // 【修改点】解决 TypeError 并修改提示文案
    toast.error('暂时没有可用积分'); 
    return;
  }
  if (loading.value.draw) return;

  loading.value.draw = true;
  activeIndex.value = 4; // 选中中心按钮
  finalPrizeIndex.value = -1; 

  try {
    const res = await request.post('/api/points/lottery/draw');

    if (res.success && res.data) {
      const result = res.data;
      let finalGridIndex;
      let targetPool;
      
      // 核心逻辑：根据 lotteryType 决定目标格子池
      if (result.lotteryType === 0) {
        targetPool = NO_WIN_PRIZE_INDICES;
      } else {
        targetPool = WIN_PRIZE_INDICES;
      }
      
      // 随机选择终点格子
      const randomIndex = Math.floor(Math.random() * targetPool.length);
      finalGridIndex = targetPool[randomIndex];
      
      // 设置动画终点和闪烁目标
      activeIndex.value = finalGridIndex;
      finalPrizeIndex.value = finalGridIndex; // 触发 CSS 闪烁

      winningResult.value = result;
      
      // 查找实际的格子名称用于提示
      const actualPrize = prizeGrid.value.find(p => p.index === finalGridIndex);
      
      
      // 将 toast.success 移入 setTimeout 中
      setTimeout(() => {
        // 1. 弹出中奖提示（在动画结束后执行）
        toast.success(`🎉 抽奖结果：${result.description || actualPrize?.name || '获得奖品'}`);
        
        // 2. 清理动画状态并刷新数据
        activeIndex.value = -1;
        fetchInitialData(); // 刷新积分和机会
        loading.value.draw = false;
      }, 1800); // 1800ms 对应 CSS 动画的持续时间

    } else {
      // 抽奖失败，重置状态
      activeIndex.value = -1; 
      finalPrizeIndex.value = -1;
      toast.error(res.message || '抽奖失败，请稍后重试。');
      loading.value.draw = false; 
    }

  } catch (err) {
    activeIndex.value = -1; 
    finalPrizeIndex.value = -1;
    console.error('抽奖请求失败:', err);
    toast.error('网络错误或系统繁忙，抽奖失败。');
    loading.value.draw = false;
  } 
};

// --- 辅助函数 (保持不变) ---
const formatPoints = (points) => {
  return (points || 0).toLocaleString('en-US'); 
};
const getPrizeIcon = (type) => {
  switch(type) {
    case 1: return '💰'; // 积分
    case 2: return '✨'; // 翻倍卡
    case 0: 
    default: return '🍀'; // 没中奖/占位
  }
};
const formatRecord = (record) => {
  if (record.lotteryType === 1) { 
    return `🎉 恭喜获得 ${formatPoints(record.pointsReward)} 积分`;
  } else if (record.lotteryType === 2) { 
    return `🌟 获得积分翻倍卡 (原积分 ${formatPoints(record.originalPoints || 0)})`;
  } else { 
    return `😢 遗憾，${record.lotteryTypeName}`;
  }
};
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' }); 
};

// --- 生命周期 (保持不变) ---
onMounted(() => {
  fetchInitialData();
});
</script>

<style scoped>
/* ==================================== */
/* CSS 样式 (已包含头部适配和规则链接修改) */
/* ==================================== */
:root {
 --primary-color: #ff4d4f; /* 核心红色/抽奖按钮 */
 --accent-color: #ffc53d; /* 金色/积分/奖品高亮 */
 --bg-color: #f4f7f9;
 --card-bg: #fff;
}

.back-btn-container {
  position: fixed; /* 固定定位，不随滚动移动 */
  left: 0vw; /* 距离左侧的距离，可根据需求调整 */
  top: 0vw; /* 距离顶部的距离，与 header 高度（12vw）适配，确保垂直居中 */
  z-index: 1001; /* 比 header 的 z-index:1000 高，避免被遮挡 */
}

.details-container {
  /* 为固定头部留出空间。假设头部高度为 12vw，下方卡片有 15px margin */
  padding-top: calc(6vw + 15px); 
  background-color: var(--bg-color);
  min-height: 100vh;
}


/* 【修改】活动规则链接样式 - 移入卡片内定位 */
.rules-link { 
  position: absolute;
  top: 10px; /* 距离卡片顶部 */
  right: 15px; /* 距离卡片右侧 */
  
  font-size: 0.8rem; 
  color: var(--primary-color); 
  font-weight: 600;
  cursor: pointer; 
  padding: 3px 8px; 
  border: 1px solid var(--primary-color);
  border-radius: 12px;
  background-color: #ffe283fd; /* 浅色背景区分 */
  transition: all 0.2s;
  z-index: 15; 
}

.rules-link:hover {
  background-color: var(--primary-color);
  color: white;
}


/* 卡片通用样式 */
.card {
 background-color: var(--card-bg);
 border-radius: 12px;
 padding: 15px;
 margin: 15px;
 box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); 
 position: relative; /* 确保 rules-link 相对它定位 */
}
.card h4 {
 color: #333;
 font-size: 1rem;
 font-weight: 600;
 margin-bottom: 10px;
 border-left: 4px solid var(--primary-color);
 padding-left: 10px;
}

/* 【修改】Lottery Area 样式 */
.lottery-area.card {
  position: relative; 
  /* 增加顶部填充，为 rules-link 留出空间 (约 30px + 15px) */
  padding-top: 45px; 
}
.lottery-area.card h4 {
  /* 调整 h4 的位置，从 padding-top 算起 */
  position: absolute;
  top: 15px;
  left: 15px;
  margin: 0;
}


/* 积分余额卡片 */
.points-balance-card {
 background: linear-gradient(90deg, #ffc53d, #ff7a45); 
 color: white;
 display: flex;
 align-items: center;
 justify-content: space-between;
}
.points-info {
 display: flex;
 flex-direction: column;
}
.points-row {
 display: flex;
 justify-content: space-between;
 align-items: center;
 margin-bottom: 10px;
}
.points-row:last-child {
 margin-bottom: 0;
}
.points-row .label { font-size: 0.9rem; }
.points-row .value {
  font-size: 1.8rem;
  font-weight: 900;
  min-width: 80px;
}
.earn-btn {
 background-color: white;
 color: #000; 
 border: none;
 padding: 8px 18px;
 border-radius: 20px;
 font-weight: bold;
 box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
 cursor: pointer;
 transition: all 0.2s;
}

/* 机会信息卡片 */
.member-info-card {
 margin-top: -5px; 
 border: 1px solid #ffbb96; 
}
.member-text .member-name, .chances-text { margin: 5px 0; font-size: 0.95rem; }
.chances-count { color: var(--primary-color); font-weight: bold; font-size: 1.1rem; }

/* 奖池概览 */
.prize-pool-display {
 display: flex;
 flex-wrap: wrap;
 gap: 8px;
 margin-bottom: 15px;
 justify-content: center;
}
.prize-tag {
 padding: 5px 10px;
 border-radius: 20px;
 font-size: 0.8rem;
 background-color: #ffe7ba; 
 color: #fa8c16;
 font-weight: 500;
}

/* 抽奖网格布局 */
.lottery-grid-container {
 display: grid;
 grid-template-columns: repeat(3, 1fr);
 grid-template-rows: repeat(3, 1fr);
 width: 70vw; 
 height: 70vw; 
 max-width: 300px;
 max-height: 300px;
 margin: 20px auto;
 border: 4px solid var(--primary-color); 
 border-radius: 8px;
 overflow: hidden;
}

.grid-item {
 border: 1px solid #ffe0b2; 
 display: flex;
 justify-content: center;
 align-items: center;
 text-align: center;
 font-size: 0.8rem;
 background-color: #fff9e6; 
 cursor: default;
 transition: background-color 0.1s;
}

.prize-content {
 display: flex;
 flex-direction: column;
 align-items: center;
 padding: 5px;
}
.prize-icon {
  font-size: 1.5rem;
  margin-bottom: 2px;
}
.prize-name { font-weight: 600; color: #333; font-size: 0.8rem; line-height: 1.2;}
.prize-points { 
  font-size: 0.7rem;
  color: #ff7a45; 
  font-weight: bold;
  margin-top: 2px;
}

/* 中奖高亮样式 */
.grid-item.active-prize {
 background-color: var(--accent-color); 
 transform: scale(1.05);
 box-shadow: 0 0 10px rgba(255, 197, 61, 0.8);
 z-index: 5;
}

/* 优化：中奖闪烁效果 */
.grid-item.winning-flash {
  animation: flash-border 0.3s ease-in-out 6 alternate; 
}

@keyframes flash-border {
 0% { 
  background-color: var(--accent-color); 
  box-shadow: 0 0 5px rgba(255, 197, 61, 0.5);
 }
 100% { 
  background-color: #ffaa00; 
  box-shadow: 0 0 15px var(--accent-color);
 }
}

/* 中心抽奖按钮 */
.center-button {
 background: var(--primary-color);
 cursor: pointer;
 color: white;
 border: none;
 font-size: 1.1rem;
}
.draw-center-button {
 display: flex;
 flex-direction: column;
 font-weight: bold;
 padding: 10px;
 line-height: 1.2;
 width: 100%;
 height: 100%;
 justify-content: center;
 background-color: #ff4d4f; 
}
/* 禁用状态优化 */
.draw-center-button.disabled {
  background-color: #ccc !important; 
  cursor: not-allowed !important;
  pointer-events: none !important;
  color: #666 !important;
}

/* 历史记录 */
.prize-list {
 list-style: none;
 padding: 0;
}
.prize-list li {
 padding: 8px 0;
 border-bottom: 1px dotted #eee;
 font-size: 0.9rem;
 display: flex;
 justify-content: space-between;
}
.prize-list .prize-win { color: #2e7d32; font-weight: 600; }
.prize-list .time { color: #999; font-size: 0.8rem; }
.empty-state { text-align: center; color: #999; padding: 10px 0; }

/* 加载和错误状态 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  z-index: 10;
  border-radius: 12px;
  font-size: 1rem;
  color: #666;
}
.error-state {
  text-align: center;
  padding: 20px;
  color: var(--primary-color);
}
.error-state button {
  margin-top: 10px;
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}
</style>

<style>
/* ==================================== */
/* 全局样式 (用于头部和 Teleported Modal) */
/* ==================================== */

/* 1. 全局固定头部样式 (解决多页面统一问题) */
.header.app-header-fixed {
  position: fixed;
  top: 0;
  /* 实现居中 */
  left: 50%; 
  transform: translateX(-50%);
  
  width: 100%;
  max-width: 600px; /* 限制最大宽度 */
  
  height: 12vw;
  background-color: #0097FF;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header.app-header-fixed h3,
.header.app-header-fixed h1 {
  font-size: 4.8vw;
  margin: 0;
  font-weight: 500;
  color: #fff; /* 确保标题颜色为白色 */
}

/* 2. 规则弹窗 Modal 样式 - 放在非 scoped 块中，因为使用了 Teleport */
.modal-mask {
 position: fixed;
 z-index: 9998;
 top: 0;
 left: 0;
 width: 100%;
 height: 100%;
 background-color: rgba(0, 0, 0, 0.5);
 display: flex;
 align-items: center;
 justify-content: center;
 transition: opacity 0.3s ease;
}
.modal-wrapper {
 width: 100%;
 display: flex;
 justify-content: center;
}
.modal-container {
 width: 80%;
 max-width: 400px;
 background-color: #fff;
 border-radius: 12px;
 box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
 transition: all 0.3s ease;
 overflow: hidden;
}
.modal-header {
 padding: 15px;
 border-bottom: 1px solid #eee;
 display: flex;
 justify-content: space-between;
 align-items: center;
}
.modal-header h3 {
 margin: 0;
 font-size: 1.1rem;
 color: var(--primary-color); /* 使用主题色 */
}
.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: #999;
  cursor: pointer;
}
.modal-body {
 padding: 15px;
 max-height: 70vh;
 overflow-y: auto;
}

.modal-body p {
  font-size: 0.9rem;
  line-height: 1.5;
  color: #555;
  margin-bottom: 8px;
}
/* 规则说明的标题和内容样式，使规则更清晰 */
.modal-body .rule-title {
  font-weight: bold;
  color: #333;
}
.modal-body .rule-content {
  margin-left: 10px;
  color: #666;
  margin-bottom: 12px;
}

.modal-body .level-info {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px dashed #ffbb96;
  font-weight: bold;
  color: #888;
}


/* 弹窗过渡动画 */
.modal-enter-active,
.modal-leave-active {
 transition: all 0.3s ease;
}
.modal-enter-from,
.modal-leave-to {
 opacity: 0;
}
.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
 -webkit-transform: scale(1.1);
 transform: scale(1.1);
}

/* 钱包开通弹窗样式 */
.wallet-activate-modal {
  text-align: center;
  max-width: 350px;
  position: relative;
}

.wallet-activate-modal .modal-icon {
  font-size: 3rem;
  color: #ff6b6b;
  margin-bottom: 15px;
}

.wallet-activate-modal .modal-icon i {
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

.wallet-activate-modal h3 {
  margin-top: 0;
  color: #2c3e50;
  margin-bottom: 20px;
}

.wallet-activate-modal .modal-message {
  color: #666;
  font-size: 1rem;
  margin: 15px 0 25px 0;
  line-height: 1.5;
}

.wallet-activate-modal .modal-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.wallet-activate-modal .apply-btn {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e) !important;
  color: white !important;
  font-weight: 600;
  padding: 12px 24px !important;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.wallet-activate-modal .apply-btn:hover {
  background: linear-gradient(135deg, #ff5252, #ff7979) !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.wallet-activate-modal .cancel-btn {
  background: #e0e0e0 !important;
  color: #666 !important;
  font-weight: 500;
  padding: 12px 24px !important;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.wallet-activate-modal .cancel-btn:hover {
  background: #d0d0d0 !important;
  transform: translateY(-2px);
}
</style>