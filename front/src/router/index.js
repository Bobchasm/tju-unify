import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Chat from '../views/Chat.vue'
import Market from '../views/Market.vue'
import MarketDetail from '../views/MarketDetail.vue'
import MarketPublish from '../views/MarketPublish.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'
import Memo from '../views/Memo.vue'
import { tradeRoute } from '../trade/tradeRoutes.js'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/chat',
    name: 'Chat',
    component: Chat
  },
  {
    path: '/market',
    name: 'Market',
    component: Market
  },
  {
    path: '/market/detail/:id',
    name: 'MarketDetail',
    component: MarketDetail
  },
  {
    path: '/market/publish',
    name: 'MarketPublish',
    component: MarketPublish
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile
  },
  {
    path: '/memo',
    name: 'Memo',
    component: Memo
  },
  tradeRoute
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
