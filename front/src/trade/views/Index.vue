<template>
    <!-- 登录、注册部分 -->
    <div class="wrapper">
        <!-- header部分 -->
        <header>
            <div class="icon-location-box">
                <i class="fas fa-map-marker-alt"></i>
            </div>
            <div class="location-text" @click="showLocationPicker">
                <span class="location-display">{{ displayLocation }}</span>
                <i class="fa fa-caret-down"></i>
            </div>

            
            <transition name="fade">
                <div v-if="showPicker" class="location-modal" @click.self="hideLocationPicker">
                    <div class="modal-container">
                        <div class="modal-header">
                            <h3>选择位置</h3>
                            <button class="close-btn" @click="hideLocationPicker">
                                <i class="fa fa-times"></i>
                            </button>
                        </div>

                        <div class="modal-content">
                            <!-- 位置层级导航 -->
                            <div class="location-nav">
                                <div v-for="(level, index) in locationLevels" :key="index"
                                    :class="['nav-item', { active: currentLevel === index, disabled: index > currentLevel }]"
                                    @click="switchLevel(index)">
                                    {{ level }}
                                </div>
                            </div>

                            <!-- 位置列表 -->
                            <div class="location-list-container">
                                <div v-if="loading" class="loading-state">
                                    <i class="fa fa-spinner fa-spin"></i>
                                    <span>加载中...</span>
                                </div>

                                <div v-else-if="locationData.length === 0" class="empty-state">
                                    <i class="fa fa-map-marker"></i>
                                    <span>暂无数据</span>
                                </div>

                                <div v-else class="location-items">
                                    <div v-for="item in locationData" :key="item.id"
                                        :class="['location-item', { selected: isSelected(item) }]"
                                        @click="selectLocation(item)">
                                        <span class="item-name">{{ item.name }}</span>
                                        <i v-if="isSelected(item)" class="fa fa-check selected-icon"></i>
                                    </div>
                                </div>
                            </div>

                            <!-- 当前选择显示 -->
                            <div v-if="selectedLocation.province" class="current-selection">
                                <span>已选择：</span>
                                <span class="selection-text">
                                    {{ getDisplayText(selectedLocation) }}
                                </span>
                            </div>
                        </div>

                        <div class="modal-footer">
                            <button class="btn-cancel" @click="hideLocationPicker">取消</button>
                            <button class="btn-confirm" @click="confirmLocation">确认</button>
                        </div>
                    </div>
                </div>
            </transition>

            <div class="login-register">
                <template v-if="!userInfo">
                    <button @click="goToLChoose">登录</button>
                    <button @click="goToRChoose">注册</button>
                </template>
                <template v-else>
                    <div class="user-info">
                        <div class="scroll-text">
                            <span>{{ userInfo.username }} ，您好！</span>
                        </div>
                    </div>
                </template>
            </div>
        </header>
        <!-- search部分 -->
        <div class="search">
            <div class="search-fixed-top" ref="fixedBox">
                <div class="search-box">
                    <i class="fa fa-search"></i>
                    <input v-model="searchKeyword" type="text" placeholder="搜索饿了么商家" @keyup.enter="performSearch" />
                    <button @click="performSearch" class="search-btn">搜索</button>
                </div>
            </div>
        </div>


        
        <!-- 销量冠军轮播图部分 -->
        <div class="top-businesses-carousel">
            <div class="carousel-header">
                <h3>🏆 销量冠军榜</h3>
                <p>最受欢迎的优质商家</p>
            </div>

            <div v-if="!topThreeBusinesses || topThreeBusinesses.length === 0" class="empty-carousel">
                <div class="empty-state">
                    <i class="fa fa-trophy"></i>
                    <p>暂无销量冠军数据</p>
                </div>
            </div>

            <div class="carousel-3d-container" v-if="topThreeBusinesses && topThreeBusinesses.length > 0">
                <!-- 左侧箭头按钮 -->
                <div class="carousel-arrow carousel-arrow-left" @click="prevSlide">
                    <i class="fa fa-chevron-left"></i>
                </div>

                <!-- 轮播图内容 -->
                <div v-for="(business, index) in topThreeBusinesses" :key="business.id" :class="[
                    'carousel-3d-item',
                    {
                        'active': index === currentSlide,
                        'left': index === getPrevIndex(),
                        'right': index === getNextIndex()
                    }
                ]" @click="goToSlide(index)">
                    <div class="business-card-3d">
                        <!-- 排名徽章 -->
                        <div class="rank-badge" :class="getRankClass(index)">
                            <span>{{ getRankText(index) }}</span>
                        </div>

                        <!-- 商家图片 -->
                        <div class="business-image">
                            <img :src="business.businessImg || '/trade-assets/business-default.png'"
                                :alt="business.businessName" @error="handleImageError" @click="toBusinessInfo(business.id)">
                        </div>

                        <!-- 商家信息 -->
                        <div class="business-info">
                            <h4>{{ business.businessName || '未命名商铺' }}</h4>
                            <div class="stats">
                                <div class="stat-item rating-stat">
                                    <i class="fa fa-star"></i>
                                    <span>{{ business.score || getBusinessRating(business.id) }}</span>
                                </div>
                                <div class="stat-item sales-stat">
                                    <i class="fa fa-fire"></i>
                                    <span>{{ business.salesCount || 0 }}</span>
                                </div>
                            </div>
                            <div class="delivery-info">
                                <div class="delivery-tag">
                                    <span class="tag-label">起送</span>
                                    <span class="tag-price">¥{{ (business.startPrice || 0).toFixed(2) }}</span>
                                </div>
                                <div class="delivery-tag">
                                    <span class="tag-label">配送</span>
                                    <span class="tag-price">¥{{ (business.deliveryPrice || 0).toFixed(2) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 右侧箭头按钮 -->
                <div class="carousel-arrow carousel-arrow-right" @click="nextSlide">
                    <i class="fa fa-chevron-right"></i>
                </div>

                <!-- 指示器 -->
                <div class="carousel-indicators">
                    <span v-for="(business, index) in topThreeBusinesses" :key="index"
                        :class="['indicator', { active: index === currentSlide }]" @click="goToSlide(index)"></span>
                </div>
            </div>
        </div>

        <!-- 超级会员部分 -->
        <div class="supermember">
            <div class="left">
                <img src="/trade-assets/super_member.png" alt="超级会员">
                <h3>超级会员</h3>
                <p>&#8226; 每月享超值权益</p>
            </div>
            <div class="right">
                立即开通 &gt;
            </div>
        </div>


        <!-- 推荐商家部分 -->
        <div class="recommend">
            <div class="recommend-line"></div>
            <p>推荐商家</p>
            <div class="recommend-line"></div>
        </div>

        <!-- 推荐方式部分 -->
        <ul class="recommendtype">
            <li :class="{ active: sortBy === 'default' }" @click="setSortBy('default')">
                综合排序<i class="fa fa-caret-down"></i>
            </li>

            <li :class="{ active: sortBy === 'sales' }" @click="setSortBy('sales')">
                销量最高
            </li>
            <li :class="{ active: showFilter }" @click="toggleFilter">
                筛选<i class="fa fa-filter"></i>
            </li>
        </ul>

        <!-- 筛选弹窗 -->
        <transition name="fade">
            <div v-if="showFilter" class="filter-modal" @click.self="hideFilter">
                <div class="filter-container">
                    <div class="filter-header">
                        <h3>筛选条件</h3>
                        <button class="close-btn" @click="hideFilter">
                            <i class="fa fa-times"></i>
                        </button>
                    </div>

                    <div class="filter-content">
                        <!-- 免配送费筛选 -->
                        <div class="filter-section">
                            <h4>配送费</h4>
                            <label class="filter-option">
                                <input type="checkbox" v-model="filters.freeDelivery" @change="applyFilters">
                                <span>免配送费</span>
                            </label>
                        </div>

                        <!-- 起送价筛选 -->
                        <div class="filter-section">
                            <h4>起送价</h4>
                            <div class="price-range">
                                <label class="filter-option">
                                    <input type="radio" name="startPrice" value="0" v-model="filters.startPrice"
                                        @change="applyFilters">
                                    <span>不限</span>
                                </label>
                                <label class="filter-option">
                                    <input type="radio" name="startPrice" value="20" v-model="filters.startPrice"
                                        @change="applyFilters">
                                    <span>20元以下</span>
                                </label>
                                <label class="filter-option">
                                    <input type="radio" name="startPrice" value="30" v-model="filters.startPrice"
                                        @change="applyFilters">
                                    <span>30元以下</span>
                                </label>
                                <label class="filter-option">
                                    <input type="radio" name="startPrice" value="50" v-model="filters.startPrice"
                                        @change="applyFilters">
                                    <span>50元以下</span>
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="filter-footer">
                        <button class="btn-reset" @click="resetFilters">重置</button>
                        <button class="btn-confirm" @click="confirmFilters">确定</button>
                    </div>
                </div>
            </div>
        </transition>

        <!-- 推荐商家列表部分 -->
        <div v-if="!businessList || businessList.length === 0" class="empty-business-list">
            <div class="empty-state">
                <i class="fa fa-store"></i>
                <p>暂无商家数据</p>
                <p class="empty-hint">请稍后再试或检查网络连接</p>
            </div>
        </div>

        <ul class="business-list" v-if="businessList && businessList.length > 0">
            <li v-for="business in businessList" :key="business.id || business.businessId"
                @click="toBusinessInfo(business.id || business.businessId)">
                <div class="business-info">
                    <img :src="business.businessImg || '/trade-assets/business-default.png'"
                        @error="handleImageError" :alt="business.businessName">
                    <div class="business-info-detail">
                        <h3>{{ business.businessName || '未命名商铺'}}</h3>
                        <div class="business-info-rating">
                            <span class="rating-score">{{ business.score || getBusinessRating(business.id ||
                                business.businessId) }}分</span>
                            <span class="monthly-sales">月售 {{ business.salesCount || 0 }}</span>
                        </div>
                        <div class="business-info-delivery">
                            <span class="start-price">起送 ¥{{ (business.startPrice || 0).toFixed(2) }}</span>
                            <span class="delivery-fee" :class="{ 'free-delivery': (business.deliveryPrice || 0) === 0 }">
                                {{ (business.deliveryPrice || 0) === 0 ? '免配送费' : `配送 ¥${(business.deliveryPrice || 0).toFixed(2)}` }}
                            </span>
                        </div>
                        <div class="business-info-promotion">
                            <div class="business-info-promotion-left">
                            </div>
                        </div>
                    </div>
                </div>
            </li>
        </ul>
        <!-- 底部菜单部分 -->

        <!-- 返回助手首页悬浮按钮 -->
        <div class="back-home-fab" @click="goToHome">
            <i class="fa fa-home"></i>
        </div>
    </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import Footer from '../components/Footer.vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import request from '@/trade/utils/tradeRequest';
import AMapLoader from '@amap/amap-jsapi-loader';
import { toast } from '@/trade/utils/toast';
const AMAP_KEY = '24cce1eb31aec79422f44af47428fc8a';

export default {
    name: 'Index',
    setup() {
        const fixedBox = ref(null);
        const router = useRouter();
        const userInfo = ref(null);
        const businessList = ref([]);
        const originalBusinessList = ref([]); // 保存原始数据用于筛选和排序
        const ratingMap = ref({});

        const currentSlide = ref(0);
        const topThreeBusinesses = ref([]);
        let autoPlayTimer = null;

        const pointsRules = ref([]); // 所有积分规则
        const enabledPointsRules = ref([]); // 启用的促销积分规则
        const currentPointsIndex = ref(0); // 当前显示的规则索引
        const currentPointsRule = ref(null); // 当前显示的规则
        let pointsCarouselTimer = null; // 轮播定时器

        const currentLocation = ref('定位中...');
        const searchKeyword = ref('');
        const sortBy = ref('default');
        const showPicker = ref(false);
        const showFilter = ref(false);
        const filters = ref({
            freeDelivery: false,
            startPrice: '0'
        });
        const loading = ref(false);
        const locationData = ref([]);
        const currentLevel = ref(0);
        const selectedLocation = ref({
            province: '',
            city: '',
            district: ''
        });
        const locationLevels = ref(['请选择省份', '请选择城市', '请选择区域']);
        const tempSelectedLocation = ref({
            province: '',
            city: '',
            district: ''
        });

        const displayLocation = computed(() => {
            const { province, city, district } = selectedLocation.value;

            if (district && province && city) {
                if (province === city) {
                    return `${province} ${district}`;
                }
                return `${province} ${city} ${district}`;
            }

            if (province && city) {
                return `${province} ${city}`;
            }

            if (province) {
                return province;
            }

            return currentLocation.value;
        });

        const getCurrentLocation = async () => {
            try {
                console.log('🌍 开始获取位置信息...');
                const response = await axios.get(`https://restapi.amap.com/v3/ip?key=${AMAP_KEY}`);
                console.log('🌍 位置API响应:', response);

                if (response && response.data && response.data.status === '1' && response.data.city) {
                    console.log('✅ 位置获取成功:', response.data.city);
                    currentLocation.value = response.data.city;
                    selectedLocation.value = {
                        province: response.data.province,
                        city: response.data.city,
                        district: ''
                    };
                } else {
                    console.log('⚠️ 位置API返回格式不正确，使用默认位置');
                    currentLocation.value = '天津大学北洋园校区';
                }
            } catch (error) {
                console.error('❌ 获取位置失败:', error);
                currentLocation.value = '天津大学北洋园校区';
                selectedLocation.value = {
                    province: '天津市',
                    city: '天津市',
                    district: '津南区'
                };
            }
        };

        const showLocationPicker = () => {
            showPicker.value = true;
            loadProvinces();
        };

        const hideLocationPicker = () => {
            showPicker.value = false;
            tempSelectedLocation.value = { ...selectedLocation.value };
        };

        const loadProvinces = async () => {
            loading.value = true;
            try {
                const response = await axios.get(`https://restapi.amap.com/v3/config/district?key=${AMAP_KEY}&keywords=中国&subdistrict=1`);
                if (response.data.status === '1') {
                    locationData.value = response.data.districts[0].districts;
                    currentLevel.value = 0;
                }
            } catch (error) {
                console.error('加载省份数据失败:', error);
            } finally {
                loading.value = false;
            }
        };

        const loadCities = async (provinceCode, provinceName) => {
            loading.value = true;
            try {
                const response = await axios.get(`https://restapi.amap.com/v3/config/district?key=${AMAP_KEY}&keywords=${provinceCode}&subdistrict=1`);
                if (response.data.status === '1' && response.data.districts[0].districts) {
                    locationData.value = response.data.districts[0].districts;
                    currentLevel.value = 1;
                    tempSelectedLocation.value.province = provinceName;
                    tempSelectedLocation.value.city = '';
                    tempSelectedLocation.value.district = '';
                }
            } catch (error) {
                console.error('加载城市数据失败:', error);
            } finally {
                loading.value = false;
            }
        };

        const loadDistricts = async (cityCode, cityName) => {
            loading.value = true;
            try {
                const response = await axios.get(`https://restapi.amap.com/v3/config/district?key=${AMAP_KEY}&keywords=${cityCode}&subdistrict=1`);
                if (response.data.status === '1' && response.data.districts[0].districts) {
                    locationData.value = response.data.districts[0].districts;
                    currentLevel.value = 2;
                    tempSelectedLocation.value.city = cityName;
                    tempSelectedLocation.value.district = '';
                }
            } catch (error) {
                console.error('加载区域数据失败:', error);
            } finally {
                loading.value = false;
            }
        };

        const switchLevel = (level) => {
            if (level < currentLevel.value) {
                currentLevel.value = level;
                if (level === 0) {
                    tempSelectedLocation.value.city = '';
                    tempSelectedLocation.value.district = '';
                    loadProvinces();
                } else if (level === 1) {
                    loadCities(tempSelectedLocation.value.province, tempSelectedLocation.value.province);
                    tempSelectedLocation.value.district = '';
                }
            }
        };

        const selectLocation = (item) => {
            if (currentLevel.value === 0) {
                loadCities(item.adcode, item.name);
            } else if (currentLevel.value === 1) {
                loadDistricts(item.adcode, item.name);
            } else if (currentLevel.value === 2) {
                tempSelectedLocation.value.district = item.name;
            }
        };

        const confirmLocation = () => {
            const { province, city, district } = tempSelectedLocation.value; // 校验临时变量
            if (!province) {
                toast.error("请先选择省份");
                return;
            }
            if (!city) {
                toast.error("请先选择城市");
                return;
            }
            if (!district) {
                toast.error("请先选择区域");
                return;
            }

            selectedLocation.value = { ...tempSelectedLocation.value };
            localStorage.setItem('userLocation', JSON.stringify(selectedLocation.value));
            const displayText = getDisplayText(selectedLocation.value);
            localStorage.setItem('userLocationDisplay', displayText);

            hideLocationPicker();
        };
        const getDisplayText = (location) => {
            const { province, city, district } = location;
            if (district && province && city) {
                if (province === city) {
                    return `${province} ${district}`;
                }
                return `${province} ${city} ${district}`;
            }
            if (province && city) {
                return `${province} ${city}`;
            }
            if (province) {
                return province;
            }
            return '未知位置';
        };

        const isSelected = (item) => {
            const { province, city, district } = tempSelectedLocation.value; // 关键：用临时变量
            if (currentLevel.value === 0) {
                return province === item.name;
            } else if (currentLevel.value === 1) {
                return city === item.name;
            } else if (currentLevel.value === 2) {
                return district === item.name;
            }
            return false;
        };


        const fetchUserInfo = async () => {

            const tokenFromLocal = localStorage.getItem('token');
            const tokenFromSession = sessionStorage.getItem('token');
            const storage = tokenFromLocal ? localStorage : (tokenFromSession ? sessionStorage : null);

            if (storage) {
                const savedUserInfo = storage.getItem('userInfo');
                if (savedUserInfo) {
                    try {
                        const parsedUserInfo = JSON.parse(savedUserInfo);
                        if (parsedUserInfo.id && parsedUserInfo.username) { // 假设用户信息必须包含id和username
                            userInfo.value = parsedUserInfo;
                            console.log('从本地存储加载用户信息成功:', userInfo.value);
                            return; // 读取到有效信息，直接返回，不发请求
                        }
                    } catch (e) {
                        console.error('解析本地用户信息失败:', e);
                        storage.removeItem('userInfo'); // 删除损坏的存储数据
                    }
                }
            }

            const token = tokenFromLocal || tokenFromSession;
            if (!token) return;

            try {
                const res = await request.get('/api/user');
                if (res && res.id && res.username) { // 校验接口返回的用户信息完整性
                    userInfo.value = res;
                    console.log('从接口加载用户信息成功:', userInfo.value);
                    storage?.setItem('userInfo', JSON.stringify(res));
                } else {
                    console.error('获取用户信息失败：接口返回数据不完整');
                    userInfo.value = null;
                }
            } catch (error) {
                console.error('获取用户信息异常:', error);
                userInfo.value = null;
                if (error.response?.status === 401) {
                    localStorage.removeItem('token');
                    sessionStorage.removeItem('token');
                    localStorage.removeItem('userInfo');
                    sessionStorage.removeItem('userInfo');
                }
            }
        };

        watch(() => userInfo.value, (newUserInfo) => {
            console.log('👤 用户信息变化，重新获取积分规则:', newUserInfo ? '已登录' : '未登录');
            fetchPointsRules();
        }, { immediate: false });

        const loadReactions = () => {
            try {
                return JSON.parse(localStorage.getItem('reactions')) || { likes: {}, favorites: {} };
            } catch (e) {
                return { likes: {}, favorites: {} };
            }
        };

        const getReactionCount = (businessId) => {
            const reactions = loadReactions();
            const likesMap = reactions.likes[String(businessId)] || {};
            const favsMap = reactions.favorites[String(businessId)] || {};
            const likes = Object.keys(likesMap).length;
            const favs = Object.keys(favsMap).length;
            return likes + favs;
        };

        const guessCommentCount = (biz) => {
            return (
                biz.commentCount ??
                biz.comments ??
                biz.remarkNum ??
                biz.reviewCount ??
                0
            ) || 0;
        };

        const computeRatings = () => {
            const entries = originalBusinessList.value || [];
            if (!entries.length) { ratingMap.value = {}; return; }
            const reactionCounts = entries.map(b => getReactionCount(b.businessId || b.id));
            const commentCounts = entries.map(b => guessCommentCount(b));
            const rMin = Math.min(...reactionCounts);
            const rMax = Math.max(...reactionCounts);
            const cMin = Math.min(...commentCounts);
            const cMax = Math.max(...commentCounts);
            const weights = { comments: 0.6, reactions: 0.4 };
            const map = {};
            entries.forEach((b, idx) => {
                const r = reactionCounts[idx];
                const c = commentCounts[idx];
                const rNorm = rMax > rMin ? (r - rMin) / (rMax - rMin) : (r > 0 ? 1 : 0);
                const cNorm = cMax > cMin ? (c - cMin) / (cMax - cMin) : (c > 0 ? 1 : 0);
                const combined = weights.comments * cNorm + weights.reactions * rNorm;
                let rating = 1 + combined * 4; // map to [1,5]
                if (rating < 1) rating = 1;
                if (rating > 5) rating = 5;
                map[b.businessId || b.id] = rating.toFixed(1);
            });
            ratingMap.value = map;
        };

        const getBusinessRating = (businessId) => {
            return ratingMap.value[businessId] || '1.0';
        };

const navigateToPromotion = (foodIds) => {
    if (Array.isArray(foodIds) && foodIds.length > 0) {
        
        const idsString = foodIds.join(',');
        
        console.log(`🔗 尝试跳转。关联的 Food IDs 列表: ${idsString}`);
        
        router.push({ 
            path: '/trade/PromotionList',
            query: {
                ids: idsString // 使用 'ids' 作为查询参数的 key
            }
        }); 
    } else {
        console.warn('⚠️ foodIds 为空或不是有效数组，无法跳转到指定商品页面');
    }
};

const fetchPointsRules = async () => {
    try {
        console.log('🔍 开始获取积分规则列表...');
        
        const tokenFromLocal = localStorage.getItem('token');
        const tokenFromSession = sessionStorage.getItem('token');
        const token = tokenFromLocal || tokenFromSession;
        
        if (!token) {
            console.log('👤 用户未登录，不显示促销积分轮播');
            enabledPointsRules.value = [];
            currentPointsRule.value = null;
            stopPointsCarousel();
            return;
        }
        
        const response = await request.get('/api/marketing/points/rules', {
            params: {
                ruleType: 1, // 1-促销积分
                ruleStatus: 1, // 1-启用
                pageNum: 1,
                pageSize: 100
            },
            headers: {
                'Authorization': `Bearer ${token}`, // 标准 JWT/OAuth2 认证格式
            },
            validateStatus: function (status) {
                return status >= 200 && status < 600; 
            }
        }).catch(error => {
            const status = error?.response?.status;
            console.warn(`⚠️ 获取积分规则请求失败 (状态码: ${status || '无'} 或网络错误):`, error?.message);
            
            enabledPointsRules.value = [];
            currentPointsRule.value = null;
            stopPointsCarousel();
            return null; // 返回 null，表示请求彻底失败
        });
        
        if (!response) {
            console.log('📭 未获取到积分规则响应，可能是网络错误，已清空数据并停止轮播');
            return;
        }
        
        console.log('📊 积分规则接口响应状态:', response?.status);
        
        let rulesData = [];
        const responseData = response.data || response;
        const httpStatus = response.status;

        if (httpStatus && httpStatus !== 200) {
            let warningMsg = `⚠️ 积分规则接口返回非200 HTTP状态码: ${httpStatus}`;
            if (httpStatus === 401) {
                warningMsg += '，权限认证失败 (Unauthorized)。请检查 Token 有效性或登录状态。';
            } else {
                warningMsg += '，清空数据并停止轮播。';
            }
            console.warn(warningMsg);
            
            enabledPointsRules.value = [];
            currentPointsRule.value = null;
            stopPointsCarousel();
            return;
        }

        if (responseData && responseData.success !== undefined) {
            if (responseData.success) {
                rulesData = Array.isArray(responseData.data) ? responseData.data : [];
                console.log('✅ 使用标准响应格式获取积分规则');
            } else {
                console.warn('⚠️ 积分规则接口业务失败:', responseData.message); 
                enabledPointsRules.value = [];
                currentPointsRule.value = null;
                stopPointsCarousel();
                return;
            }
        } 
        else if (Array.isArray(responseData)) {
            rulesData = responseData;
            console.log('✅ 使用直接数组格式获取积分规则');
        } else {
            console.warn('❌ 积分规则响应格式不正确或缺少 success 字段，清空数据:', responseData);
            enabledPointsRules.value = [];
            currentPointsRule.value = null;
            stopPointsCarousel();
            return;
        }

        enabledPointsRules.value = rulesData
            .filter(rule => 
                rule && 
                rule.ruleType === 1 && // 促销积分
                rule.ruleStatus === 1 && // 启用状态
                rule.ruleName // 必须有规则名称
            )
            .map(rule => ({
                ...rule,
                hasPromotionLink: Array.isArray(rule.foodIds) && rule.foodIds.length > 0
            }))
            .sort((a, b) => {
                const priorityA = a.priority || 0;
                const priorityB = b.priority || 0;
                return priorityB - priorityA;
            });
        
        console.log('🎯 已启用的促销积分规则:', enabledPointsRules.value.length, '条');
        
        if (enabledPointsRules.value.length > 0) {
            startPointsCarousel();
        } else {
            console.log('📭 没有可用的促销积分规则');
            currentPointsRule.value = null;
            stopPointsCarousel();
        }
        
    } catch (error) {
        console.error('❌ 获取积分规则异常:', error);
        enabledPointsRules.value = [];
        currentPointsRule.value = null;
        stopPointsCarousel();
    }
};
        
        const startPointsCarousel = () => {
            if (pointsCarouselTimer) {
                clearInterval(pointsCarouselTimer);
            }
            
            if (enabledPointsRules.value.length > 1) {
                pointsCarouselTimer = setInterval(() => {
                    nextPointsRule();
                }, 3000); // 3秒切换一次
                console.log('⏰ 积分轮播已启动，3秒切换一次');
            }
        };

        const nextPointsRule = () => {
            if (enabledPointsRules.value.length === 0) return;
            
            currentPointsIndex.value = (currentPointsIndex.value + 1) % enabledPointsRules.value.length;
            currentPointsRule.value = enabledPointsRules.value[currentPointsIndex.value];
            
            console.log('🔄 切换到下一条积分规则:', currentPointsRule.value.ruleName);
            
            restartPointsCarousel();
        };

        const prevPointsRule = () => {
            if (enabledPointsRules.value.length === 0) return;
            
            currentPointsIndex.value = currentPointsIndex.value === 0 
                ? enabledPointsRules.value.length - 1 
                : currentPointsIndex.value - 1;
            currentPointsRule.value = enabledPointsRules.value[currentPointsIndex.value];
            
            console.log('🔄 切换到上一条积分规则:', currentPointsRule.value.ruleName);
            
            restartPointsCarousel();
        };

        const restartPointsCarousel = () => {
            if (pointsCarouselTimer) {
                clearInterval(pointsCarouselTimer);
            }
            startPointsCarousel();
        };

        const stopPointsCarousel = () => {
            if (pointsCarouselTimer) {
                clearInterval(pointsCarouselTimer);
                pointsCarouselTimer = null;
                console.log('⏹️ 积分轮播已停止');
            }
        };

        const updateTopThreeBusinesses = () => {
            request.get("/api/businesses/carousel")
                .then(response => {
                    if (response.success) {
                        topThreeBusinesses.value = response.data;
                        console.log('🏆 轮播图更新:', topThreeBusinesses.value.map(b => `${b.businessName}(销量${b.salesCount || 0})`).join(', '));

                        restartAutoPlay();
                    }
                })
                .catch(error => {
                    console.error('获取轮播图数据失败:', error);
                    restartAutoPlay();
                });
        };

        const goToSlide = (index) => {
            currentSlide.value = index;
            restartAutoPlay();
        };

        const nextSlide = () => {
            currentSlide.value = (currentSlide.value + 1) % topThreeBusinesses.value.length;
        };

        const prevSlide = () => {
            currentSlide.value = currentSlide.value === 0
                ? topThreeBusinesses.value.length - 1
                : currentSlide.value - 1;
        };

        const getPrevIndex = () => {
            return currentSlide.value === 0 ? 2 : currentSlide.value - 1;
        };

        const getNextIndex = () => {
            return currentSlide.value === 2 ? 0 : currentSlide.value + 1;
        };

        const startAutoPlay = () => {
            if (topThreeBusinesses.value.length > 1) {
                autoPlayTimer = setInterval(() => {
                    nextSlide();
                }, 3000); // 3秒自动切换
            }
        };

        const stopAutoPlay = () => {
            if (autoPlayTimer) {
                clearInterval(autoPlayTimer);
                autoPlayTimer = null;
            }
        };

        const restartAutoPlay = () => {
            stopAutoPlay();
            startAutoPlay();
        };

        const getRankClass = (index) => {
            const classes = ['champion', 'runner-up', 'third'];
            return classes[index] || 'other';
        };

        const getRankText = (index) => {
            const texts = ['冠军', '亚军', '季军'];
            return texts[index] || '优秀';
        };


        const sortBusinessList = (list, sortType) => {
            console.log('=== 开始排序商家列表 ===');
            console.log('排序类型:', sortType);
            console.log('待排序商家数量:', list.length);

            const beforeSalesData = list.filter(b => (b.salesCount || 0) > 0);
            console.log('排序前有销量的商家:', beforeSalesData.map(b => ({
                name: b.businessName,
                id: b.id,
                sales: b.salesCount
            })));

            const sortedList = [...list];

            switch (sortType) {
                case 'default':
                    sortedList.sort((a, b) => {
                        const scoreA = parseFloat(a.score || getBusinessRating(a.id || a.businessId));
                        const scoreB = parseFloat(b.score || getBusinessRating(b.id || b.businessId));

                        const scoreDiff = Math.round((scoreB - scoreA) * 10) / 10;
                        if (Math.abs(scoreDiff) >= 0.1) {
                            return scoreDiff; // 评分降序
                        }

                        const idA = parseInt(a.id || a.businessId || 0);
                        const idB = parseInt(b.id || b.businessId || 0);
                        return idB - idA; // ID降序（新的在前）
                    });
                    break;

                case 'sales':
                    console.log('🔄 执行销量排序');
                    sortedList.sort((a, b) => {
                        const salesA = parseInt(a.salesCount || 0);
                        const salesB = parseInt(b.salesCount || 0);

                        if (salesA > 0 || salesB > 0) {
                            console.log(`比较: ${a.businessName}(销量${salesA}) vs ${b.businessName}(销量${salesB})`);
                        }

                        if (salesA !== salesB) {
                            const result = salesB - salesA; // 销量降序（高销量在前）
                            if (salesA > 0 || salesB > 0) {
                                console.log(`  结果: ${result > 0 ? b.businessName : a.businessName} 排在前面`);
                            }
                            return result;
                        }

                        const idA = parseInt(a.id || a.businessId || 0);
                        const idB = parseInt(b.id || b.businessId || 0);
                        return idB - idA; // ID降序（新的在前）
                    });
                    console.log('✅ 销量排序完成');
                    break;

                default:
                    break;
            }

            const afterSalesData = sortedList.filter(b => (b.salesCount || 0) > 0);
            console.log('排序后有销量的商家:', afterSalesData.map((b, index) => ({
                排名: index + 1,
                name: b.businessName,
                id: b.id,
                sales: b.salesCount
            })));

            console.log('=== 排序完成 ===');
            return sortedList;
        };

        const navigateToOrders = () => {
            router.push({ path: '/trade/orders' });
        };
        const handleScroll = () => {
            let scroll = window.scrollY || document.documentElement.scrollTop;
            let width = document.documentElement.clientWidth;
            let search = fixedBox.value;

            if (scroll > width * 0.12) {
                search.style.position = 'fixed';
                search.style.left = '0';
                search.style.top = '0';
            } else {
                search.style.position = 'static';
            }
        };
        const testAPIConnection = async () => {
            console.log('🧪 === 测试API连通性 ===');

            try {
                console.log('🔄 测试基础连接...');
                const healthResponse = await request.get('/api/businesses/search?keyword=&isScore=0&isSales=0');
                console.log('✅ API连接成功');
                console.log('响应类型:', typeof healthResponse);
                console.log('响应内容:', healthResponse);

                return healthResponse;
            } catch (error) {
                console.error('❌ API连接失败:', error);
                console.error('- 错误类型:', error.name);
                console.error('- 错误消息:', error.message);
                console.error('- 响应状态:', error.response?.status);
                console.error('- 响应数据:', error.response?.data);

                if (error.code === 'ECONNREFUSED') {
                    console.error('🚨 后端服务器未启动或端口不正确');
                } else if (error.response?.status === 404) {
                    console.error('🚨 API端点不存在');
                } else if (error.response?.status === 500) {
                    console.error('🚨 后端服务器内部错误');
                }

                return null;
            }
        };

        const testWithHardcodedData = () => {
            console.log('🧪 === 使用硬编码数据测试 ===');
            const testData = [
                {
                    "id": 1,
                    "businessName": "虾滑火锅",
                    "businessImg": "https://sunnybigevent.oss-cn-beijing.aliyuncs.com/bbd37656-0eae-41be-995e-e2be0b96aca2.png",
                    "startPrice": 120.00,
                    "deliveryPrice": 10.00,
                    "score": 1.00,
                    "salesCount": 4
                },
                {
                    "id": 40,
                    "businessName": "螺狮粉",
                    "businessImg": "https://sunnybigevent.oss-cn-beijing.aliyuncs.com/default-food.png",
                    "startPrice": 2.00,
                    "deliveryPrice": 10.00,
                    "score": 1.00,
                    "salesCount": 3
                },
                {
                    "id": 42,
                    "businessName": "发送",
                    "businessImg": "https://sunnybigevent.oss-cn-beijing.aliyuncs.com/18fa7ba5-83f2-4cc8-8b9f-e4ffaa10875d.png",
                    "startPrice": 1.00,
                    "deliveryPrice": 1.00,
                    "score": 1.00,
                    "salesCount": 2
                },
                {
                    "id": 37,
                    "businessName": "面的传奇面馆",
                    "businessImg": "https://sunnybigevent.oss-cn-beijing.aliyuncs.com/default-food.png",
                    "startPrice": 1.00,
                    "deliveryPrice": 1.00,
                    "score": 1.00,
                    "salesCount": 1
                },
                {
                    "id": 2,
                    "businessName": "黄焖鸡米饭黄焖鸡米饭",
                    "businessImg": null,
                    "startPrice": 2.00,
                    "deliveryPrice": 1.00,
                    "score": 1.00,
                    "salesCount": 0
                }
            ];

            console.log('🧪 硬编码数据:', testData);
            originalBusinessList.value = testData;
            computeRatings();
            updateTopThreeBusinesses(); // 更新轮播图数据
            applyFiltersAndSort();
        };

        onMounted(() => {
            console.log('🚀 === 页面加载开始 ===');
            console.log('Vue组件已挂载');

            const savedLocation = localStorage.getItem('userLocation');
            if (savedLocation) {
                try {
                    const location = JSON.parse(savedLocation);
                    selectedLocation.value = location;
                    tempSelectedLocation.value = { ...location };

                    const savedDisplay = localStorage.getItem('userLocationDisplay');
                    if (savedDisplay) {
                        currentLocation.value = savedDisplay;
                    }
                } catch (e) {
                    getCurrentLocation();
                }
            } else {
                getCurrentLocation();
            }
            fetchUserInfo();

            window.addEventListener('scroll', handleScroll);

            console.log('📋 准备获取商家列表');


            getBusinessList(); // 恢复API调用
            startAutoPlay();
            
            fetchPointsRules();

        });

        onBeforeUnmount(() => {
            window.removeEventListener('scroll', handleScroll);
            stopAutoPlay(); // 清理自动轮播定时器
            stopPointsCarousel(); // 清理积分轮播定时器
        });

        const toBusinessList = (orderTypeId) => {
            router.push({ path: '/trade/businessList', query: { orderTypeId } });
        };
        const goToLChoose = () => {
            router.push({ path: '/trade/login' });
        };
        const goToRChoose = () => {
            console.log('111111');
            router.push({ path: '/trade/register' });
        }
        const navigateToSearch = () => {
            router.push({ path: '/trade/search' });
        };

        const performSearch = async () => {
            if (searchKeyword.value.trim() !== '') {
                try {
                    const params = {
                        keyword: searchKeyword.value.trim()
                    };

                    if (sortBy.value === 'score') {
                        params.isScore = 1;
                        params.isSales = 0;
                    } else if (sortBy.value === 'sales') {
                        params.isScore = 0;
                        params.isSales = 1;
                    } else {
                        params.isScore = 0;
                        params.isSales = 0;
                    }

                    console.log('搜索参数:', params);
                    console.log('请求URL:', '/api/businesses/search');

                    const response = await request.get('/api/businesses/search', { params });

                    console.log('111搜索响应:', response);
                    console.log('222响应状态:', response?.status);
                    console.log('333响应数据:', response?.data);

                    console.log('🔍 搜索API响应数据结构分析:');
                    console.log('- response类型:', typeof response);
                    console.log('- response.success:', response?.success);
                    console.log('- response.data存在:', !!response?.data);

                    let searchData = null;

                    if (response && response.success && Array.isArray(response.data)) {
                        searchData = response.data;
                        console.log('✅ 搜索使用标准响应格式');
                    } else if (Array.isArray(response)) {
                        searchData = response;
                        console.log('✅ 搜索使用直接数组格式');
                    } else {
                        console.warn('❌ 搜索响应格式不正确:', response);
                        searchData = [];
                    }

                    if (searchData && searchData.length > 0) {
                        console.log('🔍 搜索到商家数据:', searchData.length, '个');
                        originalBusinessList.value = searchData;
                        computeRatings();
                        updateTopThreeBusinesses();
                        applyFiltersAndSort();
                    } else {
                        console.log('🔍 搜索无结果');
                        originalBusinessList.value = [];
                        businessList.value = [];
                        topThreeBusinesses.value = [];
                    }

                } catch (error) {
                    console.error('搜索失败:', error);
                    console.error('错误详情:', error.response?.data);
                    console.error('错误状态:', error.response?.status);
                    console.error('错误信息:', error.message);
                    getBusinessList();
                }
            } else {
                getBusinessList();
            }
        };

        const setSortBy = (type) => {
            console.log('🔄 === 用户点击排序按钮 ===');
            console.log('从', sortBy.value, '切换到', type);
            console.log('当前原始数据数量:', originalBusinessList.value.length);
            console.log('当前显示数据数量:', businessList.value.length);

            console.log('🔍 排序前最后检查 - originalBusinessList中的销量:');
            const preCheckSales = originalBusinessList.value.filter(b => (b.salesCount || 0) > 0);
            preCheckSales.forEach(business => {
                console.log(`- ${business.businessName}: salesCount=${business.salesCount}`);
            });

            sortBy.value = type;

            if (searchKeyword.value.trim() !== '') {
                console.log('🔍 有搜索关键词，执行搜索');
                performSearch(); // 重新搜索以应用新的排序
            } else {
                console.log('📋 无搜索关键词，直接排序筛选');
                applyFiltersAndSort();
            }
        };

        const getBusinessList = async () => {
            console.log('=== 开始获取商家列表 ===');
            console.log('🔄 使用search接口获取数据');
            console.log('请求URL: /api/businesses/search');
            console.log('请求参数:', { keyword: '', isScore: 0, isSales: 0 });
            request.get('/api/businesses/search', { params: { keyword: '', isScore: 0, isSales: 0 } })
                .then(response => {
                    console.log('=== 商家列表API响应 ===');
                    console.log('响应状态:', response?.status);
                    console.log('完整响应对象:', response);
                    console.log('响应数据结构:', {
                        success: response?.success,
                        hasData: !!response?.data,
                        dataType: Array.isArray(response?.data) ? 'Array' : typeof response?.data,
                        dataLength: response?.data?.length
                    });

                    if (response?.data && Array.isArray(response.data)) {
                        console.log('🔍 response.data前3个元素的详细信息:');
                        response.data.slice(0, 3).forEach((item, index) => {
                            console.log(`元素 ${index + 1}:`, item);
                            console.log(`  - businessName: ${item.businessName} (类型: ${typeof item.businessName})`);
                            console.log(`  - salesCount: ${item.salesCount} (类型: ${typeof item.salesCount})`);
                            console.log(`  - id: ${item.id} (类型: ${typeof item.id})`);
                        });

                        const withSales = response.data.filter(item => item.salesCount > 0);
                        console.log('🏆 API返回的有销量商家:', withSales.map(item => ({
                            name: item.businessName,
                            sales: item.salesCount,
                            id: item.id
                        })));
                    }

                    console.log('🔍 API响应数据结构分析:');
                    console.log('- response类型:', typeof response);
                    console.log('- response.success:', response?.success);
                    console.log('- response.data存在:', !!response?.data);
                    console.log('- response.data是数组:', Array.isArray(response?.data));

                    let businessData = null;

                    if (response && response.success && Array.isArray(response.data)) {
                        businessData = response.data;
                        console.log('✅ 使用标准响应格式 response.data');
                    } else if (Array.isArray(response)) {
                        businessData = response;
                        console.log('✅ 使用直接数组格式 response');
                    } else {
                        console.warn('❌ 无法识别的响应格式:', response);
                        businessData = [];
                    }

                    if (businessData && businessData.length > 0) {
                        console.log('📊 获取到商家数据:', businessData.length, '个');

                        console.log('🔍 前3个商家预览:');
                        businessData.slice(0, 3).forEach((business, index) => {
                            console.log(`${index + 1}. ${business.businessName}:`, {
                                id: business.id,
                                salesCount: business.salesCount,
                                score: business.score
                            });
                        });

                        const businessesWithSales = businessData.filter(b => (b.salesCount || 0) > 0);
                        console.log('🏆 有销量的商家:', businessesWithSales.length, '个');
                        if (businessesWithSales.length > 0) {
                            console.log('销量排行:');
                            businessesWithSales
                                .sort((a, b) => (b.salesCount || 0) - (a.salesCount || 0))
                                .slice(0, 5)
                                .forEach((business, index) => {
                                    console.log(`  ${index + 1}. ${business.businessName}: ${business.salesCount}`);
                                });
                        }

                        originalBusinessList.value = businessData;
                        computeRatings();
                        updateTopThreeBusinesses();
                        applyFiltersAndSort();

                        console.log('✅ 数据加载完成:', {
                            总商家数: originalBusinessList.value.length,
                            显示商家数: businessList.value.length,
                            轮播图商家数: topThreeBusinesses.value.length
                        });

                    } else {
                        console.warn('❌ 没有获取到商家数据');
                        originalBusinessList.value = [];
                        businessList.value = [];
                        topThreeBusinesses.value = [];
                    }
                })
                .catch(error => {
                    console.error('❌ 获取商家列表失败:', error);
                    console.error('错误详情:', error.response?.data);
                    console.error('错误状态:', error.response?.status);
                    console.error('错误消息:', error.message);

                    originalBusinessList.value = [];
                    businessList.value = [];
                    topThreeBusinesses.value = [];

                    if (error.code === 'NETWORK_ERROR' || error.code === 'ECONNREFUSED') {
                        console.log('🔄 网络错误，3秒后自动重试...');
                        setTimeout(() => {
                            console.log('🔄 重试获取商家列表');
                            getBusinessList();
                        }, 3000);
                    }
                });
        };

        const handleImageError = (e) => {
            e.target.src = '/trade-assets/default-business.png';
        };

        const toBusinessInfo = (businessId) => {
            router.push({
                path: '/trade/businessInfo',
                query: { businessId }
            });
        };

        const toggleFilter = () => {
            showFilter.value = !showFilter.value;
        };

        const hideFilter = () => {
            showFilter.value = false;
        };

        const applyFiltersAndSort = () => {
            console.log('🔧 === 开始应用筛选和排序 ===');
            console.log('筛选条件:', filters.value);
            console.log('排序方式:', sortBy.value);
            console.log('原始数据数量:', originalBusinessList.value.length);

            const originalSalesData = originalBusinessList.value.filter(b => (b.salesCount || 0) > 0);
            console.log('📈 原始数据中有销量的商家:', originalSalesData.map(b => ({
                name: b.businessName,
                id: b.id,
                sales: b.salesCount
            })));

            console.log('🔄 开始筛选 - 复制原始数据');
            let filteredList = [...originalBusinessList.value];

            console.log('🔍 复制后的数据检查:');
            const copiedWithSales = filteredList.filter(b => (b.salesCount || 0) > 0);
            copiedWithSales.forEach(business => {
                console.log(`- ${business.businessName}: salesCount=${business.salesCount} (类型: ${typeof business.salesCount})`);
            });

            if (filters.value.freeDelivery) {
                const beforeCount = filteredList.length;
                filteredList = filteredList.filter(business =>
                    business.deliveryPrice === 0 || business.deliveryPrice === null
                );
                console.log(`免配送费筛选: ${beforeCount} -> ${filteredList.length}`);
            }

            if (filters.value.startPrice !== '0') {
                const maxPrice = parseInt(filters.value.startPrice);
                const beforeCount = filteredList.length;
                filteredList = filteredList.filter(business => {
                    const startPrice = business.startPrice || business.starPrice || 0;
                    return startPrice <= maxPrice;
                });
                console.log(`起送价筛选(≤${maxPrice}): ${beforeCount} -> ${filteredList.length}`);
            }

            console.log('筛选后数量:', filteredList.length);

            const sortedList = sortBusinessList(filteredList, sortBy.value);

            businessList.value = sortedList;
            console.log('筛选和排序后的商家列表:', sortedList.length, '个商家');

            if (sortedList.length > 0) {
                const debugInfo = sortedList.slice(0, 5).map(business => ({
                    name: business.businessName || '未命名',
                    score: parseFloat(business.score || getBusinessRating(business.id || business.businessId)),
                    sales: parseInt(business.salesCount || 0),
                    id: business.id || business.businessId,
                    rawSalesCount: business.salesCount // 显示原始销量数据
                }));
                console.log(`排序后前5个商家 (${sortBy.value}排序):`, debugInfo);

                if (sortBy.value === 'sales') {
                    const salesInfo = sortedList.map(business => ({
                        name: business.businessName || '未命名',
                        sales: parseInt(business.salesCount || 0),
                        id: business.id || business.businessId,
                        rawSalesCount: business.salesCount
                    })).sort((a, b) => b.sales - a.sales).slice(0, 8);
                    console.log('销量排序详情（前8名）:', salesInfo);
                }

                console.log('🎯 === 最终显示数据检查 ===');
                console.log('最终businessList总数量:', businessList.value.length);
                console.log('最终businessList前5个商家详情:');
                businessList.value.slice(0, 5).forEach((business, index) => {
                    console.log(`${index + 1}. ${business.businessName}:`, {
                        id: business.id,
                        salesCount: business.salesCount,
                        score: business.score,
                        原始销量值: business.salesCount,
                        销量类型: typeof business.salesCount
                    });
                });

                console.log('🎭 模板显示检查 - 前5个商家的销量显示值:');
                businessList.value.slice(0, 5).forEach((business, index) => {
                    const displayValue = business.salesCount || 0;
                    console.log(`${index + 1}. ${business.businessName}: 显示值=${displayValue} (原值=${business.salesCount})`);
                });
            }
        };

        const applyFilters = () => {
            applyFiltersAndSort();
        };

        const resetFilters = () => {
            filters.value = {
                freeDelivery: false,
                startPrice: '0'
            };
            sortBy.value = 'default'; // 重置排序为默认
            applyFiltersAndSort();
        };

        const confirmFilters = () => {
            applyFiltersAndSort();
            hideFilter();
        };

        const goToHome = () => {
            router.push({ path: '/' });
        };


        return {
            fixedBox,
            toBusinessList,
            navigateToOrders,
            goToLChoose,
            goToRChoose,
            userInfo,
            isuser: computed(() => !!userInfo.value),
            navigateToSearch,
            businessList,
            toBusinessInfo,
            handleImageError,
            getBusinessRating,
            displayLocation,
            showPicker,
            loading,
            locationData,
            currentLevel,
            locationLevels,
            selectedLocation,
            showLocationPicker,
            hideLocationPicker,
            switchLevel,
            selectLocation,
            isSelected,
            confirmLocation,
            getDisplayText,
            searchKeyword,
            sortBy,
            performSearch,
            setSortBy,
            showFilter,
            filters,
            toggleFilter,
            hideFilter,
            applyFilters,
            resetFilters,
            confirmFilters,
            applyFiltersAndSort,
            sortBusinessList,
            testWithHardcodedData,
            testAPIConnection,
            currentSlide,
            topThreeBusinesses,
            goToSlide,
            nextSlide,
            prevSlide,
            enabledPointsRules,
            currentPointsIndex,
            currentPointsRule,
            prevPointsRule,
            nextPointsRule,
            navigateToPromotion,
            getPrevIndex,
            getNextIndex,
            getRankClass,
            getRankText,
            updateTopThreeBusinesses,
            startAutoPlay,
            stopAutoPlay,
            restartAutoPlay,
            goToHome
        };
    },
    components: {
        Footer
    }
}
</script>

<style scoped>
.wrapper {
    width: 100%;
    height: 100%;
}

.wrapper header {
    width: 100%;
    height: 12vw;
    background: linear-gradient(to right, #3a7bd5, #00d2ff);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 4vw;
    box-sizing: border-box;
}



.wrapper header .icon-location-box {
    width: 3.5vw;
    height: 3.5vw;
    margin-right: 1vw;
    flex-shrink: 0;
}

.wrapper header .location-text {
    font-size: 4.5vw;
    font-weight: 700;
    color: #fff;
    flex-shrink: 0;
    white-space: nowrap;
}

.wrapper header .icon-location-box i {
    font-size: 5vw;
    color: #fff;
}

.wrapper header .location-text .fa-caret-down {
    margin-left: 1vw;
}

.user-info {
    width: 150px;
    overflow: hidden;
    white-space: nowrap;
    position: relative;
}

.scroll-text {
    display: inline-block;
    padding-left: 100%;
    animation: scroll-text 10s linear infinite;
}

@keyframes scroll-text {
    0% {
        transform: translateX(0);
    }

    100% {
        transform: translateX(-100%);
    }
}


.wrapper .login-register {
    display: flex;
    gap: 2vw;
    align-items: center;
    margin-left: 5vw;
    flex-grow: 1;
    justify-content: flex-end;
    min-width: 0;
}

.wrapper .login-register .user-info {
    font-size: 4vw;
    font-weight: 500;
    color: #fff;
    white-space: nowrap;

    overflow-x: auto;
    overflow-y: hidden;
    -webkit-overflow-scrolling: touch;

    scrollbar-width: none;
    -ms-overflow-style: none;
}

.wrapper .login-register .user-info::-webkit-scrollbar {
    display: none;
}

.wrapper .login-register button {
    padding: 1.5vw 3vw;
    border: none;
    background-color: white;
    color: #0097ff;
    cursor: pointer;
    border-radius: 1vw;
    transition: background-color 0.3s;
    font-size: 3.5vw;
    flex-shrink: 0;
}

.wrapper .login-register button:hover {
    background-color: #f0f0f0;
}

.wrapper .search {
    width: 100%;
    height: 13vw;
}

.wrapper .search .search-fixed-top {
    width: 100%;
    height: 13vw;
    background: linear-gradient(to right, #3a7bd5, #00d2ff);
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    z-index: 20;
}

.wrapper .search .search-fixed-top .search-box {
    width: 90%;
    height: 9vw;
    background-color: #fff;
    border-radius: 2px;

    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 2vw;

    font-size: 3.5vw;
    color: #AEAEAE;
    font-family: "宋体";
    user-select: none;
}

.wrapper .search .search-fixed-top .search-box input {
    flex: 1;
    border: none;
    outline: none;
    background: transparent;
    font-size: 3.5vw;
    color: #333;
    margin: 0 1vw;
}

.wrapper .search .search-fixed-top .search-box input::placeholder {
    color: #AEAEAE;
}

.wrapper .search .search-fixed-top .search-box .search-btn {
    background: #0097ff;
    color: white;
    border: none;
    padding: 1.5vw 3vw;
    border-radius: 1vw;
    font-size: 3vw;
    cursor: pointer;
    transition: background-color 0.3s;
}

.wrapper .search .search-fixed-top .search-box .search-btn:hover {
    background: #0080e0;
}

.wrapper .search .search-fixed-top .search-box .fa-search {
    margin-right: 1vw;
}

.sort-options {
    width: 100%;
    padding: 3vw;
    background-color: #f8f9fa;
    border-bottom: 1px solid #e0e0e0;
}

.sort-buttons {
    display: flex;
    gap: 2vw;
    justify-content: center;
    flex-wrap: wrap;
}

.sort-buttons button {
    padding: 2vw 4vw;
    border: 1px solid #ddd;
    background-color: white;
    color: #666;
    border-radius: 2vw;
    cursor: pointer;
    transition: all 0.3s;
    font-size: 3.2vw;
    min-width: 20vw;
}

.sort-buttons button:hover {
    border-color: #0097ff;
    color: #0097ff;
}

.sort-buttons button.active {
    background-color: #0097ff;
    color: white;
    border-color: #0097ff;
}

.wrapper .foodtype {
    width: 100%;
    height: 48vw;
    background-color: white;
    display: flex;
    flex-wrap: wrap;
    justify-content: space-around;
    align-content: center;
}

.wrapper .foodtype li {
    width: 18vw;
    height: 20vw;

    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    user-select: none;
    cursor: pointer;
}

.wrapper .foodtype li img {
    width: 12vw;
    height: 10.3vw;
}

.wrapper .foodtype li p {
    font-size: 3.2vw;
    color: #666;
}

.wrapper .top-businesses-carousel {
    width: 95%;
    margin: 1.5vw auto;
    background: white;
    border-radius: 2vw;
    padding: 1.5vw 2vw;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    position: relative;
    z-index: 10;
}

.wrapper .top-businesses-carousel .carousel-header {
    text-align: center;
    margin-bottom: 1.5vw;
    color: #333;
}

.wrapper .top-businesses-carousel .carousel-header h3 {
    font-size: 6vw;
    margin: 0 0 1vw 0;
    font-weight: 700;
    text-shadow: none;
}

.wrapper .top-businesses-carousel .carousel-header p {
    font-size: 2.8vw;
    margin: 0;
    opacity: 0.7;
    color: #666;
}

.wrapper .top-businesses-carousel .carousel-3d-container {
    position: relative;
    height: 50vw;
    min-height: 320px;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 3vw 12vw;
    margin: -3vw -12vw;
}

.wrapper .top-businesses-carousel .carousel-3d-item {
    position: absolute;
    transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    cursor: pointer;
    transform-style: preserve-3d;
}

.wrapper .top-businesses-carousel .carousel-3d-item.active {
    z-index: 13;
    transform: translateX(0) scale(1);
    opacity: 1;
}

.wrapper .top-businesses-carousel .carousel-3d-item.left {
    z-index: 12;
    transform: translateX(-20vw) scale(0.75);
    opacity: 0.6;
}

.wrapper .top-businesses-carousel .carousel-3d-item.right {
    z-index: 12;
    transform: translateX(20vw) scale(0.75);
    opacity: 0.6;
}

.wrapper .top-businesses-carousel .business-card-3d {
    width: 38vw;
    min-width: 240px;
    background: white;
    border-radius: 2vw;
    padding: 2.5vw;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    position: relative;
    overflow: hidden;
}

.wrapper .top-businesses-carousel .carousel-3d-item:hover .business-card-3d {
    transform: translateY(-0.5vw);
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.2);
}

.wrapper .top-businesses-carousel .rank-badge {
    position: absolute;
    top: 0;
    right: 0;
    padding: 1.5vw 3vw;
    border-radius: 0 2vw 0 2vw;
    color: white;
    font-weight: 700;
    font-size: 2.5vw;
    z-index: 15;
}

.wrapper .top-businesses-carousel .rank-badge.champion {
    background: linear-gradient(135deg, #FFD700, #FFA500);
}

.wrapper .top-businesses-carousel .rank-badge.runner-up {
    background: linear-gradient(135deg, #C0C0C0, #A9A9A9);
}

.wrapper .top-businesses-carousel .rank-badge.third {
    background: linear-gradient(135deg, #CD7F32, #B8860B);
}

.wrapper .top-businesses-carousel .business-image {
    width: 100%;
    height: 25vw;
    min-height: 160px;
    border-radius: 1.5vw;
    overflow: hidden;
    margin-bottom: 1.5vw;
}

.wrapper .top-businesses-carousel .business-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.wrapper .top-businesses-carousel .carousel-3d-item:hover .business-image img {
    transform: scale(1.05);
}

.wrapper .top-businesses-carousel .business-info {
    text-align: center;
}

.wrapper .top-businesses-carousel .business-info h4 {
    font-size: 3.2vw;
    font-weight: 700;
    color: #333;
    margin: 0 0 1vw 0;
    line-height: 1.2;
}

.wrapper .top-businesses-carousel .stats {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1.5vw;
    gap: 1.5vw;
}

.wrapper .top-businesses-carousel .stat-item {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.8vw;
    padding: 1.2vw 2vw;
    border-radius: 2vw;
    font-size: 2.5vw;
    font-weight: 600;
    flex: 1;
    color: white;
}

.wrapper .top-businesses-carousel .rating-stat {
    background: linear-gradient(135deg, #FF6B6B, #FF8E53);
}

.wrapper .top-businesses-carousel .rating-stat .fa-star {
    color: #FFD700;
}

.wrapper .top-businesses-carousel .sales-stat {
    background: linear-gradient(135deg, #4ECDC4, #44A08D);
}

.wrapper .top-businesses-carousel .sales-stat .fa-fire {
    color: #FF6B35;
}

.wrapper .top-businesses-carousel .delivery-info {
    display: flex;
    justify-content: space-between;
    gap: 1vw;
}

.wrapper .top-businesses-carousel .delivery-tag {
    display: flex;
    align-items: center;
    gap: 0.5vw;
    background: #f8f9fa;
    padding: 1vw 1.5vw;
    border-radius: 1.5vw;
    border: 1px solid #e9ecef;
    flex: 1;
    justify-content: center;
}

.wrapper .top-businesses-carousel .delivery-tag .tag-label {
    font-size: 2.2vw;
    color: #6c757d;
    font-weight: 500;
}

.wrapper .top-businesses-carousel .delivery-tag .tag-price {
    font-size: 2.4vw;
    color: #007bff;
    font-weight: 600;
}

.wrapper .top-businesses-carousel .carousel-arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 8vw;
    height: 8vw;
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid #ddd;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 3vw;
    color: #333;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    z-index: 16;
    opacity: 0;
    visibility: hidden;
}

.wrapper .top-businesses-carousel:hover .carousel-arrow {
    opacity: 1;
    visibility: visible;
}

.wrapper .top-businesses-carousel .carousel-arrow:hover {
    background: white;
    transform: translateY(-50%) scale(1.1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.wrapper .top-businesses-carousel .carousel-arrow-left {
    left: 8vw;
}

.wrapper .top-businesses-carousel .carousel-arrow-right {
    right: 8vw;
}

.wrapper .top-businesses-carousel .carousel-indicators {
    display: flex;
    justify-content: center;
    gap: 1.5vw;
    margin-top: 1.5vw;
}

.wrapper .top-businesses-carousel .indicator {
    width: 2.5vw;
    height: 2.5vw;
    border-radius: 50%;
    background: #ddd;
    cursor: pointer;
    transition: all 0.3s ease;
}

.wrapper .top-businesses-carousel .indicator.active {
    background: #667eea;
    transform: scale(1.2);
}

.wrapper .supermember {
    width: 95%;
    margin: 0 auto;
    height: 11.5vw;
    background-color: #FEEDC1;
    margin-top: 1.3vw;
    border-radius: 2px;
    color: #644F1B;

    display: flex;
    justify-content: space-between;
    align-items: center;
}

.wrapper .supermember .left {
    display: flex;
    align-items: center;
    margin-left: 4vw;
    user-select: none;
}

.wrapper .supermember .left img {
    width: 6vw;
    height: 6vw;
    margin-right: 2vw;
}

.wrapper .supermember .left h3 {
    font-size: 4vw;
    margin-right: 2vw;
}

.wrapper .supermember .left p {
    font-size: 3vw;
}

.wrapper .supermember .right {
    font-size: 3vw;
    margin-right: 4vw;
    cursor: pointer;
}

.wrapper .points-promotion-carousel {
    width: 97%;
    margin: 2vw auto;
    
    background: linear-gradient(135deg, #FFC107 0%, #FF8F00 100%);
    position: relative;
    overflow: visible;
    border-radius: 16px;
    padding: 2.5vw 2vw;
    height: 14vw;
    min-height: 14vw;
    
    box-shadow: 0 8px 24px rgba(255, 193, 7, 0.4), 
                0 4px 8px rgba(255, 143, 0, 0.3);
    
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.wrapper .points-promotion-carousel::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -10%;
    width: 200px;
    height: 200px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    pointer-events: none;
}

.wrapper .points-promotion-carousel::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: -5%;
    width: 150px;
    height: 150px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 50%;
    pointer-events: none;
}

.wrapper .points-promotion-carousel .carousel-container {
    flex: 1;
    position: relative;
    height: 100%;
    min-height: 9vw;
    display: flex;
    align-items: center;
    z-index: 1;
    overflow: visible;
}

.wrapper .points-promotion-carousel:hover .carousel-controls {
    opacity: 1;
    visibility: visible;
}

.wrapper .points-promotion-carousel .promotion-icon-fixed {
    position: absolute;
    left: 3vw;
    top: 50%;
    transform: translateY(-50%);
    width: 7vw;
    height: 7vw;
    min-width: 48px;
    min-height: 48px;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0.1));
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    z-index: 5;
    overflow: hidden;
}

.wrapper .points-promotion-carousel .promotion-icon-fixed::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transform: rotate(45deg);
    animation: shine 3s infinite;
}

.wrapper .points-promotion-carousel .promotion-icon-fixed i {
    font-size: 3.5vw;
    color: white;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    position: relative;
    z-index: 1;
}

.wrapper .points-promotion-carousel .promotion-item {
    display: flex;
    align-items: center;
    width: 100%;
    height: 100%;
    padding: 1.5vw 2vw;
    padding-left: 11vw; /* 为固定的图标留出空间 */
    justify-content: flex-start;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    animation: slideInUp 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
    box-sizing: border-box;
    flex-shrink: 0;
    position: relative;
}

@keyframes shine {
    0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
    100% { transform: translateX(100%) translateY(100%) rotate(45deg); }
}


.wrapper .points-promotion-carousel .promotion-content {
    margin-right: 15vw; /* 为右下角按钮留出空间，避免重叠 */
    flex: 1;
    overflow: hidden;
}

.wrapper .points-promotion-carousel .promotion-text {
    font-size: 4vw;
    font-weight: 600;
    color: white;
    display: block;
    line-height: 1.5;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
    letter-spacing: 0.5px;
}

.wrapper .points-promotion-carousel .promotion-action {
    position: absolute;
    bottom: 1vw;
    right: 1.5vw;
    z-index: 5;
}

.wrapper .points-promotion-carousel .promotion-action .jump-button {
    font-size: 2.2vw;
    font-weight: 500;
    padding: 0.4vw 1.2vw;
    background: none;
    color: rgba(255, 255, 255, 0.9);
    border: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.6);
    cursor: pointer;
    transition: all 0.2s ease;
    white-space: nowrap;
    display: flex;
    align-items: center;
    gap: 0.3vw;
}

.wrapper .points-promotion-carousel .promotion-action .jump-button:hover {
    color: rgba(255, 255, 255, 1);
    border-bottom-color: rgba(255, 255, 255, 1);
}

.wrapper .points-promotion-carousel .promotion-action .jump-button:active {
    opacity: 0.8;
}

.wrapper .points-promotion-carousel .empty-promotion {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    padding: 3vw 0;
    color: rgba(255, 255, 255, 0.9);
    font-size: 3.8vw;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.wrapper .points-promotion-carousel .empty-promotion i {
    margin-right: 2vw;
    font-size: 4.5vw;
    opacity: 0.8;
}

.wrapper .points-promotion-carousel .carousel-controls {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    width: 100%;
    height: 100%;
    z-index: 10;
    pointer-events: none;
    opacity: 0;
    visibility: hidden;
    transition: opacity 0.3s ease, visibility 0.3s ease;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    padding: 0.5vw 0;
}

.wrapper .points-promotion-carousel .control-btn {
    width: 3.5vw;
    height: 3.5vw;
    min-width: 20px;
    min-height: 20px;
    background: none;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: opacity 0.2s ease;
    color: rgba(255, 255, 255, 0.9);
    font-size: 2vw;
    pointer-events: auto;
    flex-shrink: 0;
}

.wrapper .points-promotion-carousel .control-btn.prev-btn {
    order: 1;
    margin-top: -5vw; /* 往上移动 */
}

.wrapper .points-promotion-carousel .control-btn.next-btn {
    order: 2;
    margin-bottom: -5vw; /* 往下移动 */
}

.wrapper .points-promotion-carousel .control-btn:hover {
    color: rgba(255, 255, 255, 1);
    opacity: 1;
}

.wrapper .points-promotion-carousel .control-btn:active {
    opacity: 0.7;
}

@keyframes slideInUp {
    from {
        opacity: 0;
        transform: translateY(30px) scale(0.95);
    }
    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

.slide-up-enter-active,
.slide-up-leave-active {
    transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    right: 0;
}

.slide-up-enter-from {
    opacity: 0;
    transform: translateY(40px) scale(0.9);
}

.slide-up-leave-to {
    opacity: 0;
    transform: translateY(-40px) scale(0.9);
}

@media (max-width: 768px) {
    .wrapper .points-promotion-carousel {
        padding: 3vw 4vw;
        height: 16vw;
        min-height: 16vw;
        border-radius: 12px;
    }
    
    .wrapper .points-promotion-carousel .promotion-item {
        padding: 2vw 2.5vw;
        padding-left: 13vw; /* 为固定的图标留出空间 */
        border-radius: 10px;
    }
    
    .wrapper .points-promotion-carousel .promotion-text {
        font-size: 4.5vw;
        white-space: normal;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
        line-height: 1.4;
    }
    
    .wrapper .points-promotion-carousel .promotion-icon-fixed {
        width: 9vw;
        height: 9vw;
        min-width: 44px;
        min-height: 44px;
        left: 4vw;
        border-radius: 10px;
    }
    
    .wrapper .points-promotion-carousel .promotion-icon-fixed i {
        font-size: 4.5vw;
    }
    
    .wrapper .points-promotion-carousel .control-btn {
        width: 4vw;
        height: 4vw;
        min-width: 24px;
        min-height: 24px;
        font-size: 2.5vw;
    }
    
    .wrapper .points-promotion-carousel .promotion-action .jump-button {
        font-size: 2.8vw;
        padding: 1vw 2.2vw;
        border-radius: 14px;
    }

}

.wrapper .recommend {
    width: 100%;
    height: 14vw;
    display: flex;
    justify-content: center;
    align-items: center;
}

.wrapper .recommend .recommend-line {
    width: 6vw;
    height: 0.2vw;
    background-color: #888;
}

.wrapper .recommend p {
    font-size: 4vw;
    margin: 0 4vw;
}

.wrapper .recommendtype {
    width: 100%;
    height: 5vw;
    margin-bottom: 5vw;

    display: flex;
    justify-content: space-around;
    align-items: center;
}

.wrapper .recommendtype li {
    font-size: 3.5vw;
    color: #555;
}

.wrapper .business-list {
    width: 100%;
    padding: 0;
    margin: 0 0 15vh 0;
    list-style: none;
}

.wrapper .business-list li {
    padding: 3vw;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.3s;
}

.wrapper .business-list li:hover {
    background-color: #f9f9f9;
}

.wrapper .business-list li .business-info {
    display: flex;
    align-items: flex-start;
}

.wrapper .business-list li .business-info img {
    width: 20vw;
    height: 20vw;
    object-fit: cover;
    border-radius: 4px;
}

.wrapper .business-list li .business-info .business-info-detail {
    flex: 1;
    margin-left: 3vw;
}

.wrapper .business-list li .business-info .business-info-detail h3 {
    font-size: 4vw;
    margin: 0 0 2vw 0;
    color: #333;
}

.wrapper .business-list li .business-info .business-info-rating {
    display: flex;
    align-items: center;
    gap: 3vw;
    margin-bottom: 2vw;
}

.wrapper .business-list li .business-info .business-info-rating .rating-score {
    font-size: 3.2vw;
    color: #FF6600;
    font-weight: 600;
}

.wrapper .business-list li .business-info .business-info-rating .monthly-sales {
    font-size: 2.8vw;
    color: #999;
}

.wrapper .business-list li .business-info .business-info-delivery {
    display: flex;
    gap: 2vw;
    margin-bottom: 2vw;
}

.wrapper .business-list li .business-info .business-info-delivery .start-price {
    font-size: 2.8vw;
    color: #666;
}

.wrapper .business-list li .business-info .business-info-delivery .delivery-fee {
    font-size: 2.8vw;
    color: #666;
}

.wrapper .business-list li .business-info .business-info-delivery .delivery-fee.free-delivery {
    color: #FF6600;
    font-weight: 500;
}

.wrapper .business-list li .business-info .business-info-promotion {
    display: flex;
    align-items: center;
}

.wrapper .business-list li .business-info .business-info-promotion .business-info-promotion-left {
    display: flex;
    align-items: center;
    gap: 1vw;
}

.wrapper .business-list li .business-info .business-info-promotion .business-info-promotion-left .business-info-promotion-left-incon {
    background-color: #ff4444;
    color: white;
    padding: 0.5vw 1vw;
    border-radius: 2px;
    font-size: 2.5vw;
}

.wrapper .business-list li .business-info .business-info-promotion .business-info-promotion-left p {
    color: #666;
    font-size: 3vw;
    margin: 0;
}

.location-text {
    cursor: pointer;
    transition: color 0.3s;
    display: flex;
    align-items: center;
    gap: 4px;
}

.location-text:hover {
    color: #e0e0e0;
}

.location-display {
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.location-modal {
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
    padding: 20px;
}

.modal-container {
    background: white;
    border-radius: 12px;
    width: 100%;
    max-width: 400px;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    overflow: hidden;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;
    background: linear-gradient(135deg, #0097ff, #0066cc);
    color: white;
}

.modal-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
}

.close-btn {
    background: none;
    border: none;
    color: white;
    font-size: 20px;
    cursor: pointer;
    padding: 5px;
    border-radius: 50%;
    transition: background-color 0.3s;
}

.close-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
}

.modal-content {
    display: flex;
    flex-direction: column;
    padding: 20px;
    margin-left: 27px;
    margin-top: 10px;
    margin-bottom: 10px;
    overflow-y: auto;
}

.location-nav {
    display: flex;
    margin-bottom: 20px;
    border-bottom: 2px solid #f0f0f0;
}

.nav-item {
    padding: 12px 20px;
    cursor: pointer;
    border-bottom: 3px solid transparent;
    transition: all 0.3s;
    font-weight: 500;
    color: #666;
}

.nav-item.active {
    color: #0097ff;
    border-bottom-color: #0097ff;
}

.nav-item.disabled {
    color: #ccc;
    cursor: not-allowed;
}

.nav-item:not(.disabled):hover {
    color: #0097ff;
}

.location-list-container {
    min-height: 200px;
    max-height: 300px;
    overflow-y: auto;
    margin-bottom: 20px;
}

.loading-state,
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 150px;
    color: #999;
}

.loading-state i,
.empty-state i {
    font-size: 24px;
    margin-bottom: 10px;
}

.location-items {
    display: grid;
    gap: 8px;
}

.location-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
}

.location-item:hover {
    border-color: #0097ff;
    background-color: #f8f9ff;
}

.location-item.selected {
    border-color: #0097ff;
    background-color: #e6f3ff;
}

.item-name {
    font-weight: 500;
}

.selected-icon {
    color: #0097ff;
    font-size: 14px;
}

.current-selection {
    padding: 15px;
    background-color: #f8f9fa;
    border-radius: 8px;
    margin-top: 15px;
}

.selection-text {
    font-weight: 600;
    color: #0097ff;
    display: inline-block;
    max-width: 250px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.modal-footer {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid #f0f0f0;
    background-color: #fafafa;
}

.btn-cancel,
.btn-confirm {
    flex: 1;
    padding: 12px;
    border: none;
    border-radius: 6px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
}

.btn-cancel {
    background-color: #f8f9fa;
    color: #666;
}

.btn-cancel:hover {
    background-color: #e9ecef;
}

.btn-confirm {
    background: linear-gradient(135deg, #0097ff, #0066cc);
    color: white;
}

.btn-confirm:hover {
    background: linear-gradient(135deg, #0080e0, #0055aa);
    transform: translateY(-1px);
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.empty-carousel,
.empty-business-list {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
    padding: 40px 20px;
}

.empty-carousel .empty-state,
.empty-business-list .empty-state {
    text-align: center;
    color: #999;
}

.empty-carousel .empty-state i,
.empty-business-list .empty-state i {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.5;
}

.empty-carousel .empty-state p,
.empty-business-list .empty-state p {
    font-size: 16px;
    margin: 8px 0;
}

.empty-business-list .empty-state .empty-hint {
    font-size: 14px;
    color: #ccc;
}

.wrapper .recommendtype {
    width: 100%;
    height: 5vw;
    margin-bottom: 5vw;
    display: flex;
    justify-content: space-around;
    align-items: center;
}

.wrapper .recommendtype li {
    font-size: 3.5vw;
    color: #555;
    cursor: pointer;
    transition: color 0.3s;
    padding: 1vw 2vw;
    border-radius: 1vw;
}

.wrapper .recommendtype li:hover {
    color: #0097ff;
}

.wrapper .recommendtype li.active {
    color: #0097ff;
    background-color: #f0f8ff;
}

.filter-modal {
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
    padding: 20px;
}

.filter-container {
    background: white;
    border-radius: 12px;
    width: 100%;
    max-width: 400px;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    overflow: hidden;
}

.filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;
    background: linear-gradient(135deg, #0097ff, #0066cc);
    color: white;
}

.filter-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
}

.close-btn {
    background: none;
    border: none;
    color: white;
    font-size: 20px;
    cursor: pointer;
    padding: 5px;
    border-radius: 50%;
    transition: background-color 0.3s;
}

.close-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
}

.filter-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
}

.filter-section {
    margin-bottom: 25px;
}

.filter-section h4 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 15px 0;
    color: #333;
}

.filter-option {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    cursor: pointer;
    font-size: 14px;
    color: #666;
}

.filter-option input[type="checkbox"],
.filter-option input[type="radio"] {
    margin-right: 10px;
    transform: scale(1.2);
}

.filter-option:hover {
    color: #0097ff;
}

.price-range {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.filter-footer {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid #f0f0f0;
    background-color: #fafafa;
}

.btn-reset,
.btn-confirm {
    flex: 1;
    padding: 12px;
    border: none;
    border-radius: 6px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
}

.btn-reset {
    background-color: #f8f9fa;
    color: #666;
}

.btn-reset:hover {
    background-color: #e9ecef;
}

.btn-confirm {
    background: linear-gradient(135deg, #0097ff, #0066cc);
    color: white;
}

.btn-confirm:hover {
    background: linear-gradient(135deg, #0080e0, #0055aa);
    transform: translateY(-1px);
}

.back-home-fab {
    position: fixed;
    right: 20px;
    bottom: 100px;
    width: 50px;
    height: 50px;
    background: linear-gradient(135deg, #3a7bd5, #00d2ff);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(58, 123, 213, 0.4);
    cursor: pointer;
    z-index: 999;
    transition: transform 0.2s, box-shadow 0.2s;
}

.back-home-fab:active {
    transform: scale(0.92);
}

.back-home-fab i {
    color: white;
    font-size: 22px;
}

</style>