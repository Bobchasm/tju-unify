<template>
  <div class="trade-app">
    <BackButton v-if="showBackButton" />
    <div class="trade-content">
      <router-view />
    </div>
    <Footer v-if="showFooter" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BackButton from '@/trade/components/BackButton.vue'
import Footer from '@/trade/components/Footer.vue'

const route = useRoute()

const showBackButton = computed(() => {
  const p = route.path
  if (p === '/trade' || p === '/trade/' || p === '/trade/myInformation') return false
  return true
})

const showFooter = computed(() => {
  const name = route.name
  const noFooter = [
    'TradeBusinessInfo',
    'TradePayment',
    'TradeSuccessfulPayment',
    'TradeOrders',
    'TradeCart',
    'TradeFavorites',
    'TradeNotifications',
    'TradeUserAddress',
    'TradeListDetail',
    'TradeRegister',
    'TradeLogin',
    'TradeEditUserAddress',
    'TradeWallet',
    'TradePoints',
    'TradePointsLottery',
    'TradePointsDetails',
    'TradePointsExpiring',
    'TradePromotionList'
  ]
  return !noFooter.includes(name)
})
</script>

<style>
@import '@/trade/assets/styles/global.css';
</style>

<style scoped>
.trade-app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.trade-content {
  flex: 1;
  overflow-y: auto;
}
</style>
