import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/styles.css'
import 'font-awesome/css/font-awesome.min.css'
import { registerTradeRouterGuard } from './trade/tradeRouterGuard.js'

registerTradeRouterGuard(router)

const app = createApp(App)
app.use(router)
app.mount('#app')
