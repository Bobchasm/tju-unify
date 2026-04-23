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
  flex: 1; 
  height: 100%;
  min-width: 0; 
}

.nav-item i {
  font-size: 1.2rem;
}

.nav-item.active {
  color: #0097ff;
}
</style>