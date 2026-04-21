<template>
  <div class="bottom-nav">
    <router-link
      to="/admin/home"
      class="nav-item"
      :class="{ active: $route.path === '/admin/home' }"
    >
      <i class="fas fa-home"></i>
      <span>首页</span>
    </router-link>
    <router-link
      to="/admin/user"
      class="nav-item"
      :class="{ active: $route.path === '/admin/user' }"
    >
      <i class="fas fa-user-friends"></i>
      <span>用户管理</span>
    </router-link>
    <router-link
      to="/admin/business"
      class="nav-item"
      :class="{ active: $route.path === '/admin/business' }"
    >
      <i class="fas fa-store"></i>
      <span>商铺管理</span>
    </router-link>
    <router-link
      to="/admin/marketing"
      class="nav-item"
      :class="{ active: $route.path === '/admin/marketing' }"
    >
      <i class="fas fa-bullhorn"></i> <span>营销中心</span>
    </router-link>
    </div>
</template>

<script setup>
import { useRoute } from 'vue-router';

// 引入 useRoute 获取当前路由对象
const $route = useRoute();
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 55px;
  background: white;
  display: flex;
  justify-content: space-around;
  align-items: center;
  /* 增加了 flex-wrap: nowrap; 确保不换行，虽然默认就是不换行 */
  flex-wrap: nowrap; 
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
  text-decoration: none;
  font-size: 0.8rem;
  gap: 4px;
  /* 每个 nav-item 占用等宽空间 */
  flex: 1; 
  height: 100%;
  /* 增加 padding/min-width 避免内容过多时挤压，不过 flex: 1; 已经做了很好的分配 */
  min-width: 0; 
}

.nav-item i {
  font-size: 1.2rem;
}

.nav-item.active {
  color: #0097ff;
}
</style>