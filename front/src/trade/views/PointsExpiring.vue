<template>
    <div class="back-btn-container">
        <BackButton style="margin-top: 2vw;"/>
    </div>
    <div class="details-container">
        <div class="header app-header-fixed">
            <h3>即将过期积分</h3>
        </div>

        <div class="content-body">
            
            <div class="summary-card card">
            <h4 class="summary-title">
                 近期有 <span class="total-expiring-points">{{ totalExpiringPoints }}</span> 积分即将过期！
                <br>
                <span class="record-count">{{ expiringList.length }} 笔记录</span>
            </h4>
            <p class="warning-text">
                <i class="fas fa-exclamation-triangle"></i> 
                请尽快使用这些积分，避免不必要的损失！
            </p>
            </div>

            <div class="expiring-list">
                <div 
                    v-for="item in expiringList" 
                    :key="item.id" 
                    class="list-item card-small"
                >
                    <div class="info">
                        <span class="reason">将在 <strong :class="{'urgent': item.daysUntilExpiration <= 30}">{{ item.daysUntilExpiration }}</strong> 天后过期</span>
                        <span class="date">
                            <i class="fas fa-clock"></i> 
                            过期日期：{{ item.expireDate }} 
                        </span>
                    </div>
                    <div class="points-value">
                        <span class="points-count">{{ item.pointsAmount }}</span> 积分
                    </div>
                </div>

                <div v-if="expiringList.length === 0" class="empty-state">
                    <i class="fas fa-leaf"></i> 恭喜！近期暂无即将过期的积分。
                </div>
            </div>

        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router'; 
import request from '@/trade/utils/tradeRequest'; 
import { toast } from '@/trade/utils/toast';

const router = useRouter();
const expiringList = ref([]); // 即将过期的积分列表数据

// 计算总共即将过期的积分数
const totalExpiringPoints = computed(() => {
    return expiringList.value.reduce((sum, item) => sum + (item.pointsAmount || 0), 0);
});


// 【主要改动】：接入 /api/points/expiring 接口
const fetchExpiringList = async () => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (!token) {
        toast.error('登录已过期，请重新登录！');
        router.push({ path: '/trade/login' });
        return;
    }

    try {
        // 使用正确的 API 接口：GET /api/points/expiring
        const response = await request.get(`/api/points/expiring`, {
            headers: { 'Authorization': `Bearer ${token}` },
            // 不需要额外的 params，接口文档说明查询的是当前用户所有即将过期的记录
        });
        
        if (response && response.success && Array.isArray(response.data)) {
            // 匹配接口返回的字段名：pointsAmount, expireDate, daysUntilExpiration
            expiringList.value = response.data.filter(item => item.isExpired === false); 
            
            // 可以在此根据 expireTime/expireDate 再次进行排序，确保数据按过期时间升序
            expiringList.value.sort((a, b) => new Date(a.expireTime) - new Date(b.expireTime));

        } else {
            toast.error('获取过期积分列表失败: ' + (response ? response.message : '未知错误'));
            expiringList.value = [];
        }
    } catch (e) {
        console.error('获取过期积分列表异常', e);
        toast.error('网络异常，无法加载过期积分数据');
        expiringList.value = [];
    }
};

onMounted(() => {
    fetchExpiringList();
});
</script>

<style scoped>
/* 样式部分 */
/* .expiring-points-container {
    padding: 0;
    max-width: 600px;
    margin: 0 auto;
    background-color: #f4f7f9;
    min-height: 100vh;
} */

/* 头部样式：移除返回按钮后，header 居中更简单 */

/* .header {
    background-color: #fff;
    padding: 15px 20px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    position: sticky;
    top: 0;
    z-index: 10;
    text-align: center;
    display: block; 
} */

/* .header h3 {
    margin: 0; 
    font-size: 1.2rem;
    color: #333;
    
} */
.content-body {
    padding: 6px;
}

/* 公共卡片样式 */
.card {
    background-color: #fff;
    border-radius: 12px;
    padding: 15px;
    margin-bottom: 15px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.card-small {
    padding: 10px 15px;
    border-radius: 8px;
    margin-bottom: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    background-color: #fff;
}

/* 总结卡片 */
.summary-card {
    background-color: #ffe0b2; 
    border: 1px solid #ffb74d;
    padding: 20px;
    text-align: center; /* **新增：使 h4 内容居中** */
}
.summary-card h4.summary-title { /* 增加 class 以区分 */
    color: #e65100;
    font-size: 1.05rem;
    margin-bottom: 10px;
    font-weight: 600;
}
.summary-card .total-expiring-points {
    color: #f4511e;
    font-size: 1.3rem;
    font-weight: 700;
}
/* 调整记录数样式，确保居中显示 */
.summary-card .record-count {
    display: inline-block; /* 保持在同一行内的特性 */
    font-size: 0.85rem;
    color: #e65100;
    font-weight: 400;
    margin-top: 5px; /* 添加一点间距 */
}
.summary-card .warning-text {
    font-size: 0.9rem;
    color: #f4511e;
}
.summary-card .warning-text i {
    margin-right: 5px;
    color: #ff9800;
}

.back-btn-container {
    position: fixed; /* 固定定位，不随滚动移动 */
    left: 0vw; /* 距离左侧的距离，可根据需求调整 */
    top: 0vw; /* 距离顶部的距离，与 header 高度（12vw）适配，确保垂直居中 */
    z-index: 1001; /* 比 header 的 z-index:1000 高，避免被遮挡 */
}

/* 列表样式 */
.expiring-list {
    margin-top: 15px;
}
.list-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-left: 5px solid #ffcc80; 
}
.list-item .info {
    display: flex;
    flex-direction: column;
}
.list-item .reason {
    font-size: 1rem;
    font-weight: 500;
    color: #333;
}
.list-item .reason strong {
    font-weight: 700;
    color: #ff9800;
}
.list-item .reason strong.urgent {
    color: #f44336; /* 30天内使用更紧急的红色 */
}
.list-item .date {
    font-size: 0.8rem;
    color: #999;
    margin-top: 3px;
}
.list-item .date i {
    margin-right: 4px;
    color: #ff9800;
}
.list-item .points-value {
    font-size: 0.9rem;
    color: #f44336;
    font-weight: 600;
    text-align: right;
}
.list-item .points-count {
    font-size: 1.2rem;
    font-weight: 700;
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 40px 20px;
    color: #999;
    font-size: 1rem;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.empty-state i {
    display: block;
    font-size: 2rem;
    margin-bottom: 10px;
    color: #a5d6a7;
}
</style>