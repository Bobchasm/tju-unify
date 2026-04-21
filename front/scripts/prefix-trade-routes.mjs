import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const tradeRoot = path.join(__dirname, '../src/trade')

/** Longest paths first */
const SEGMENTS = [
  'successfulPayment',
  'wallet/transactions',
  'wallet/loans',
  'points/lottery',
  'points/details',
  'points/expiring',
  'search-test',
  'addUserAddress',
  'editUserAddress',
  'orderList',
  'businessList',
  'businessInfo',
  'PromotionList',
  'myInformation',
  'notifications',
  'userAddress',
  'listDetail',
  'ListDetail',
  'register',
  'favorites',
  'payment',
  'orders',
  'search',
  'wallet',
  'points',
  'login',
  'cart',
  'ai-chat'
]

function prefixPathLiteral (content) {
  let c = content
  // router.push("/foo") or router.push('/foo')
  c = c.replace(/router\.push\(\s*(["'])\/([^"']*)\1\s*\)/g, (m, q, p) => {
    if (p.startsWith('trade/') || p === 'trade') return m
    if (p === '') return `router.push(${q}/trade${q})`
    return `router.push(${q}/trade/${p}${q})`
  })
  // path: '/foo' or path: "/foo" inside objects
  for (const seg of SEGMENTS) {
    const reSingle = new RegExp(`path:\\s*'/${seg.replace(/\//g, '\\/')}'`, 'g')
    const reDouble = new RegExp(`path:\\s*"/${seg.replace(/\//g, '\\/')}"`, 'g')
    c = c.replace(reSingle, `path: '/trade/${seg}'`)
    c = c.replace(reDouble, `path: "/trade/${seg}"`)
  }
  // /index -> /trade
  c = c.replace(/path:\s*'\/index'/g, "path: '/trade'")
  c = c.replace(/path:\s*"\/index"/g, 'path: "/trade"')
  // /BusinessList -> /trade/businessList
  c = c.replace(/path:\s*'\/BusinessList'/g, "path: '/trade/businessList'")
  c = c.replace(/path:\s*"\/BusinessList"/g, 'path: "/trade/businessList"')
  // /ListDetail -> /trade/listDetail (route path lowercase)
  c = c.replace(/path:\s*'\/ListDetail'/g, "path: '/trade/listDetail'")
  c = c.replace(/path:\s*"\/ListDetail"/g, 'path: "/trade/listDetail"')
  // /UserAddress -> /trade/userAddress
  c = c.replace(/path:\s*'\/UserAddress'/g, "path: '/trade/userAddress'")
  c = c.replace(/path:\s*"\/UserAddress"/g, 'path: "/trade/userAddress"')
  // router.push("/") only as full string
  c = c.replace(/router\.push\(\s*["']\/["']\s*\)/g, "router.push('/trade')")
  c = c.replace(/router\.push\(\s*["']\/["']\s*\)/g, 'router.push("/trade")')
  return c
}

function walk (dir, acc = []) {
  for (const name of fs.readdirSync(dir)) {
    const p = path.join(dir, name)
    const st = fs.statSync(p)
    if (st.isDirectory()) walk(p, acc)
    else if (/\.(vue|js)$/.test(name)) acc.push(p)
  }
  return acc
}

for (const file of walk(tradeRoot)) {
  let c = fs.readFileSync(file, 'utf8')
  const n = prefixPathLiteral(c)
  if (n !== c) fs.writeFileSync(file, n, 'utf8')
}

console.log('Done prefixing trade routes')
