<template>
    <div v-if="showBackButton" class="back-button" @click="goBack">
      <i class="fa fa-arrow-left"></i>
    </div>
  </template>
  
  <script>
  import { computed } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  
  export default {
    name: 'BackButton',
    setup() {
    
      const route = useRoute();
      const router = useRouter();
  
      const excludedPaths = ['/trade', '/trade/', '/trade/myInformation'];
      const showBackButton = computed(() => !excludedPaths.includes(route.path));
      const goBack = () => {
        router.back();
      };
  
      return {
        showBackButton,
        goBack,
      };
    },
  };
  </script>
  
  <style scoped>
.back-button {
  position: fixed;
  height: 7.5vw; /* 使用相对单位，屏幕越大，按钮越大 */
  width: 7.5vw;
  cursor: pointer;
  padding: 1vw 1vw; /* 使用相对单位，屏幕越大，按钮越大，同时确保最小值 */
  background: #0097ff;
  color: white;
  border-radius: max(1vw, 10px); /* 控制圆角大小，屏幕大时变大，最小为10px */
  user-select: none;
  z-index: 1005; /* 确保在页面其他元素上面 */

  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 5vw ;/* 确保图标在大屏幕上变大，最小16px */
  margin-left: 2vw; 
  margin-top: 1vw; 
}



</style>
