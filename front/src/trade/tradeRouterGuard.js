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
