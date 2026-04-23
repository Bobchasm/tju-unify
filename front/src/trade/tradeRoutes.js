import TradeLayout from '@/trade/TradeLayout.vue'

export const tradeRoute = {
  path: '/trade',
  component: TradeLayout,
  children: [
    { path: '', name: 'TradeHome', component: () => import('@/trade/views/Index.vue') },
    { path: 'login', name: 'TradeLogin', component: () => import('@/trade/views/Login.vue') },
    { path: 'register', name: 'TradeRegister', component: () => import('@/trade/views/Register.vue') },
    { path: 'businessList', name: 'TradeBusinessList', component: () => import('@/trade/views/BusinessList.vue') },
    { path: 'businessInfo', name: 'TradeBusinessInfo', component: () => import('@/trade/views/BusinessInfo.vue') },
    { path: 'orders', name: 'TradeOrders', component: () => import('@/trade/views/Orders.vue') },
    { path: 'userAddress', name: 'TradeUserAddress', component: () => import('@/trade/views/UserAddress.vue') },
    { path: 'payment', name: 'TradePayment', component: () => import('@/trade/views/Payment.vue') },
    { path: 'orderList', name: 'TradeOrderList', component: () => import('@/trade/views/OrderList.vue') },
    { path: 'listDetail', name: 'TradeListDetail', component: () => import('@/trade/views/ListDetail.vue') },
    { path: 'addUserAddress', name: 'TradeAddUserAddress', component: () => import('@/trade/views/AddUserAddress.vue') },
    { path: 'editUserAddress', name: 'TradeEditUserAddress', component: () => import('@/trade/views/EditUserAddress.vue') },
    { path: 'successfulPayment', name: 'TradeSuccessfulPayment', component: () => import('@/trade/views/SuccessfulPayment.vue') },
    { path: 'PromotionList', name: 'TradePromotionList', component: () => import('@/trade/views/PromotionList.vue') },
    { path: 'myInformation', name: 'TradeMyInformation', component: () => import('@/trade/views/MyInformation.vue') },
    { path: 'favorites', name: 'TradeFavorites', component: () => import('@/trade/views/Favorites.vue') },
    { path: 'notifications', name: 'TradeNotifications', component: () => import('@/trade/views/Notifications.vue') },
    { path: 'search', name: 'TradeSearch', component: () => import('@/trade/views/Search.vue') },
    { path: 'search-test', name: 'TradeSearchTest', component: () => import('@/trade/views/SearchTest.vue') },
    { path: 'cart', name: 'TradeCart', component: () => import('@/trade/views/Cart.vue') }
  ]
}
