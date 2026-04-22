import * as memoApi from '../api/memo'
import auth from '../api/auth'

const STORAGE_PREFIX = 'memo_remind_os_'
const POLL_MS = 45_000

let intervalId = null

function pad2(n) {
  return String(n).padStart(2, '0')
}

/** 本地时间的 ISO 片段（无时区），与后端 LocalDateTime 对齐 */
function toLocalDateTimeIso(d) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}T${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`
}

function parseRemindAt(val) {
  if (!val) return null
  if (typeof val === 'string') {
    const s = val.includes('T') ? val : val.replace(' ', 'T')
    const t = new Date(s.length <= 16 ? `${s}:00` : s)
    return Number.isNaN(t.getTime()) ? null : t
  }
  return null
}

function isDueNow(memo) {
  if (memo.remindDone === 1) return false
  const t = parseRemindAt(memo.remindAt)
  if (!t) return false
  return t.getTime() <= Date.now()
}

function fireKey(memo) {
  return `${STORAGE_PREFIX}${memo.id}_${String(memo.remindAt)}`
}

function alreadyFired(memo) {
  return localStorage.getItem(fireKey(memo)) === '1'
}

function markFired(memo) {
  try {
    localStorage.setItem(fireKey(memo), '1')
  } catch (_) {}
}

function goMemoHash() {
  if (typeof window === 'undefined') return
  window.location.hash = '#/memo'
}

async function pollOnce() {
  if (!auth.isAuthenticated()) return
  if (typeof Notification === 'undefined') return
  if (Notification.permission !== 'granted') return

  const until = new Date(Date.now() + 120_000)
  const from = new Date(Date.now() - 30 * 86400000)
  const params = {
    from: toLocalDateTimeIso(from),
    until: toLocalDateTimeIso(until)
  }

  let list = []
  try {
    const res = await memoApi.memoRemindersDue(params)
    if (res && res.success && Array.isArray(res.data)) list = res.data
  } catch (e) {
    console.warn('[memo reminder] poll failed', e)
    return
  }

  for (const m of list) {
    if (!isDueNow(m)) continue
    if (alreadyFired(m)) continue
    try {
      const n = new Notification(m.title || '备忘录提醒', {
        body: (m.content && String(m.content).trim()) ? String(m.content).slice(0, 160) : '到提醒时间啦，点一下打开备忘录',
        tag: `memo-remind-${m.id}`,
        requireInteraction: false
      })
      n.onclick = () => {
        window.focus()
        goMemoHash()
        n.close()
      }
      markFired(m)
    } catch (e) {
      console.warn('[memo reminder] Notification failed', e)
    }
  }
}

/**
 * 登录后由 App 调用：仅当浏览器已授予通知权限时才启动轮询，避免无效请求。
 * 用户首次需在备忘录页点击「开启桌面提醒」完成授权。
 */
export function startMemoReminderScheduler() {
  if (typeof Notification === 'undefined') return
  if (Notification.permission !== 'granted') return
  if (intervalId != null) return
  intervalId = setInterval(pollOnce, POLL_MS)
  pollOnce()
}

export function stopMemoReminderScheduler() {
  if (intervalId != null) {
    clearInterval(intervalId)
    intervalId = null
  }
}

/** 用户手势内调用：请求系统通知权限，并确保轮询已启动 */
export async function enableMemoDesktopNotifications() {
  if (typeof Notification === 'undefined') {
    return { ok: false, reason: 'unsupported' }
  }
  const cur = Notification.permission
  if (cur === 'denied') {
    return { ok: false, reason: 'denied' }
  }
  if (cur === 'default') {
    const p = await Notification.requestPermission()
    if (p !== 'granted') {
      return { ok: false, reason: p }
    }
  }
  startMemoReminderScheduler()
  await pollOnce()
  return { ok: true, reason: 'granted' }
}

export function getNotificationPermission() {
  if (typeof Notification === 'undefined') return 'unsupported'
  return Notification.permission
}
