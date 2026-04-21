/**
 * 交易平台路由守卫：与 elm 顾客端一致，除首页/商家列表/商家详情/登录/注册等外需登录。
 * 仅作用于 path 以 /trade 开头的路由。
 */
export function registerTradeRouterGuard(router) {
  const publicPaths = new Set([
    '/trade',
    '/trade/businessList',
    '/trade/businessInfo',
    '/trade/login',
    '/trade/register',
    '/trade/search',
    '/trade/search-test',
    '/trade/PromotionList'
  ])

  router.beforeEach((to, from, next) => {
    if (!to.path.startsWith('/trade')) {
      next()
      return
    }
    if (publicPaths.has(to.path)) {
      next()
      return
    }
    const userFromLocal = localStorage.getItem('userInfo')
    const userFromSession = sessionStorage.getItem('userInfo')
    const user =
      (userFromLocal ? JSON.parse(userFromLocal) : null) ||
      (userFromSession ? JSON.parse(userFromSession) : null)
    if (user === null) {
      next({ path: '/trade/login', query: { redirect: to.fullPath } })
      return
    }
    next()
  })
}
