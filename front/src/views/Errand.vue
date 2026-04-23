<template>
  <div class="page errand-page">
    <header>
      <div class="header-content">
        <div class="back-btn" @click="goBack">
          <i class="fa-solid fa-backward"></i>
        </div>
        <div class="page-title">
          <span class="title-icon">🛵</span>
          <span>校园跑腿</span>
        </div>
        <div class="header-placeholder"></div>
      </div>
      <div class="tab-bar">
        <div class="chip" :class="{ active: tab === 'hall' }" @click="tab = 'hall'; loadHall()">大厅</div>
        <div class="chip" :class="{ active: tab === 'mine' }" @click="tab = 'mine'; loadMine()">我的</div>
      </div>
    </header>

    <div class="errand-list">
      <div v-if="loading" class="hint">加载中…</div>
      <template v-else>
        <div v-for="o in displayList" :key="o.id" class="errand-card">
          <div class="errand-card-top">
            <span class="status-pill" :class="'s' + o.status">{{ statusText(o.status) }}</span>
            <span v-if="tab === 'mine'" class="role-tag">{{ roleTag(o) }}</span>
          </div>
          <div class="route-line">
            <span class="dot start"></span>
            <span class="route-text">{{ o.startPoint }}</span>
          </div>
          <div class="route-line">
            <span class="dot end"></span>
            <span class="route-text">{{ o.endPoint }}</span>
          </div>
          <div class="meta-row">
            <span>小费 ¥{{ formatTip(o.tipAmount) }}</span>
            <span>期望 {{ formatDateTime(o.expectTime) }}</span>
          </div>
          <div class="contact-row">
            <span class="label">联系</span>
            <span>{{ o.contactInfo }}</span>
          </div>
          <p v-if="o.remark" class="remark">{{ o.remark }}</p>
          <div class="actions">
            <button
              v-if="tab === 'hall' && o.status === 0 && !isPublisher(o)"
              type="button"
              class="primary-btn small"
              :disabled="acting"
              @click="onAccept(o)"
            >
              接单
            </button>
            <button
              v-if="tab === 'mine' && o.status === 0 && isPublisher(o)"
              type="button"
              class="secondary-btn small"
              :disabled="acting"
              @click="onCancel(o)"
            >
              取消
            </button>
            <button
              v-if="tab === 'mine' && o.status === 1 && isRunner(o)"
              type="button"
              class="primary-btn small"
              :disabled="acting"
              @click="onComplete(o)"
            >
              确认完成
            </button>
          </div>
        </div>
        <div v-if="displayList.length === 0" class="empty-state">
          <span>📭</span>
          <p>{{ tab === 'hall' ? '暂无待接单' : '暂无相关订单' }}</p>
        </div>
      </template>
    </div>

    <div class="fab-btn" @click="openPublish">
      <i class="fa-solid fa-plus" style="color: rgb(251, 251, 252);"></i>
    </div>

    <div v-if="publish.show" class="sheet-mask" @click.self="closePublish">
      <div class="sheet" @click.stop>
        <div class="sheet-head">
          <span>发布跑腿</span>
          <button type="button" class="link-btn" @click="closePublish">关闭</button>
        </div>
        <div class="sheet-body">
          <label class="field-label">起点</label>
          <input v-model="publish.startPoint" class="field-input" type="text" placeholder="取货/见面地点" />
          <label class="field-label">终点</label>
          <input v-model="publish.endPoint" class="field-input" type="text" placeholder="送达地点" />
          <label class="field-label">小费（元）</label>
          <input v-model.number="publish.tipAmount" class="field-input" type="number" min="0" step="0.01" />
          <label class="field-label">期望时间</label>
          <input v-model="publish.expectLocal" class="field-input" type="datetime-local" />
          <label class="field-label">联系方式</label>
          <input v-model="publish.contactInfo" class="field-input" type="text" placeholder="手机 / 微信等" />
          <label class="field-label">补充说明（可选）</label>
          <textarea v-model="publish.remark" class="field-textarea" rows="3" placeholder="物品、注意事项等" />
        </div>
        <div class="sheet-foot">
          <button type="button" class="primary-btn" :disabled="acting" @click="submitPublish">发布</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import auth from '../api/auth'
import * as errandApi from '../api/errand'
import { toast } from '../utils/toast'

const router = useRouter()
const tab = ref('hall')
const loading = ref(false)
const acting = ref(false)
const hallList = ref([])
const mineList = ref([])

const publish = reactive({
  show: false,
  startPoint: '',
  endPoint: '',
  tipAmount: 0,
  expectLocal: '',
  contactInfo: '',
  remark: ''
})

const myId = computed(() => {
  const u = auth.getUserInfo()
  return u && u.id != null ? Number(u.id) : null
})

const displayList = computed(() => (tab.value === 'hall' ? hallList.value : mineList.value))

const goBack = () => router.push('/')

const ensureLogin = () => {
  if (!auth.isAuthenticated()) {
    router.replace('/login')
    return false
  }
  return true
}

const unwrap = (res) => {
  if (res && res.success) return res.data
  throw new Error(res?.message || '请求失败')
}

const statusText = (s) => {
  const m = { 0: '待接单', 1: '已接单', 2: '已完成', 3: '已取消' }
  return m[s] ?? String(s)
}

const roleTag = (o) => {
  if (myId.value == null) return ''
  if (Number(o.publisherId) === myId.value) return '我发布的'
  if (o.runnerId != null && Number(o.runnerId) === myId.value) return '我接的'
  return ''
}

const isPublisher = (o) => myId.value != null && Number(o.publisherId) === myId.value
const isRunner = (o) => o.runnerId != null && myId.value != null && Number(o.runnerId) === myId.value

const formatDateTime = (v) => {
  if (!v) return ''
  if (typeof v === 'string') return v.replace('T', ' ').slice(0, 16)
  return String(v)
}

const formatTip = (t) => {
  if (t == null || t === '') return '0.00'
  const n = Number(t)
  return Number.isFinite(n) ? n.toFixed(2) : String(t)
}

const loadHall = async () => {
  loading.value = true
  try {
    const res = await errandApi.errandOpenList()
    hallList.value = unwrap(res) || []
  } catch (e) {
    console.error(e)
    toast.warning(e.message || '加载失败')
    hallList.value = []
  } finally {
    loading.value = false
  }
}

const loadMine = async () => {
  loading.value = true
  try {
    const res = await errandApi.errandMine()
    mineList.value = unwrap(res) || []
  } catch (e) {
    console.error(e)
    toast.warning(e.message || '加载失败')
    mineList.value = []
  } finally {
    loading.value = false
  }
}

const openPublish = () => {
  if (!ensureLogin()) return
  publish.show = true
  publish.startPoint = ''
  publish.endPoint = ''
  publish.tipAmount = 0
  publish.expectLocal = ''
  publish.contactInfo = ''
  publish.remark = ''
}

const closePublish = () => {
  publish.show = false
}

const toExpectIso = (localVal) => {
  if (!localVal) return null
  if (localVal.length === 16) return `${localVal}:00`
  return localVal
}

const submitPublish = async () => {
  if (!publish.startPoint.trim() || !publish.endPoint.trim()) {
    toast.warning('请填写起点和终点')
    return
  }
  if (!publish.contactInfo.trim()) {
    toast.warning('请填写联系方式')
    return
  }
  const expectTime = toExpectIso(publish.expectLocal)
  if (!expectTime) {
    toast.warning('请选择期望时间')
    return
  }
  acting.value = true
  try {
    const tip = publish.tipAmount == null || publish.tipAmount === '' ? 0 : Number(publish.tipAmount)
    const res = await errandApi.errandPublish({
      startPoint: publish.startPoint.trim(),
      endPoint: publish.endPoint.trim(),
      tipAmount: tip,
      expectTime,
      contactInfo: publish.contactInfo.trim(),
      remark: publish.remark.trim() || undefined
    })
    unwrap(res)
    toast.warning('发布成功')
    closePublish()
    await loadHall()
    tab.value = 'hall'
  } catch (e) {
    console.error(e)
    toast.warning(e.response?.data?.message || e.message || '发布失败')
  } finally {
    acting.value = false
  }
}

const onAccept = async (o) => {
  if (!ensureLogin()) return
  acting.value = true
  try {
    await errandApi.errandAccept(o.id)
    toast.warning('已接单')
    await loadHall()
    await loadMine()
  } catch (e) {
    console.error(e)
    toast.warning(e.response?.data?.message || e.message || '操作失败')
  } finally {
    acting.value = false
  }
}

const onComplete = async (o) => {
  acting.value = true
  try {
    await errandApi.errandComplete(o.id)
    toast.warning('已标记完成')
    await loadMine()
  } catch (e) {
    console.error(e)
    toast.warning(e.response?.data?.message || e.message || '操作失败')
  } finally {
    acting.value = false
  }
}

const onCancel = async (o) => {
  acting.value = true
  try {
    await errandApi.errandCancel(o.id)
    toast.warning('已取消')
    await loadMine()
    await loadHall()
  } catch (e) {
    console.error(e)
    toast.warning(e.response?.data?.message || e.message || '操作失败')
  } finally {
    acting.value = false
  }
}

onMounted(async () => {
  if (!ensureLogin()) return
  await loadHall()
})
</script>

<style scoped>
.errand-page {
  min-height: 100vh;
  background: #f8f9fa;
  padding-bottom: 28vw;
  margin: 0;
  padding-left: 0;
  padding-right: 0;
  padding-top: 0;
}

.errand-page header {
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  padding: 4vw 4vw 3vw;
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.25);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2vw;
}

.back-btn {
  width: 10vw;
  height: 10vw;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 4.5vw;
  cursor: pointer;
}

.header-placeholder {
  width: 10vw;
  height: 10vw;
}

.page-title {
  flex: 1;
  text-align: center;
  color: #fff;
  font-size: 4.5vw;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2vw;
}

.title-icon {
  font-size: 5vw;
}

.tab-bar {
  display: flex;
  gap: 2vw;
  margin-top: 3vw;
}

.chip {
  flex: 1;
  text-align: center;
  padding: 2vw 3vw;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 3.2vw;
  cursor: pointer;
  border: 1px solid transparent;
}

.chip.active {
  background: #fff;
  color: #3a7bd5;
  font-weight: 600;
}

.errand-list {
  padding: 4vw;
}

.hint {
  text-align: center;
  color: #666;
  padding: 6vw;
}

.errand-card {
  background: #fff;
  border-radius: 2.5vw;
  padding: 4vw;
  margin-bottom: 3vw;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.errand-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2.5vw;
}

.status-pill {
  font-size: 2.8vw;
  padding: 0.8vw 2.2vw;
  border-radius: 999px;
  background: #eef2f7;
  color: #445;
}

.status-pill.s0 {
  background: #fff3e0;
  color: #e65100;
}
.status-pill.s1 {
  background: #e3f2fd;
  color: #1565c0;
}
.status-pill.s2 {
  background: #e8f5e9;
  color: #2e7d32;
}
.status-pill.s3 {
  background: #f5f5f5;
  color: #757575;
}

.role-tag {
  font-size: 2.8vw;
  color: #3a7bd5;
}

.route-line {
  display: flex;
  align-items: flex-start;
  gap: 2vw;
  margin-bottom: 1.5vw;
  font-size: 3.4vw;
  line-height: 1.4;
}

.dot {
  width: 2.8vw;
  height: 2.8vw;
  border-radius: 50%;
  margin-top: 1vw;
  flex-shrink: 0;
}

.dot.start {
  background: #43a047;
}
.dot.end {
  background: #e53935;
}

.route-text {
  flex: 1;
  color: #222;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 2vw 4vw;
  font-size: 3vw;
  color: #555;
  margin-top: 2vw;
}

.contact-row {
  margin-top: 2vw;
  font-size: 3.2vw;
}

.contact-row .label {
  color: #888;
  margin-right: 2vw;
}

.remark {
  margin: 2vw 0 0;
  font-size: 3vw;
  color: #666;
  line-height: 1.45;
}

.actions {
  margin-top: 3vw;
  display: flex;
  flex-wrap: wrap;
  gap: 2vw;
}

.primary-btn {
  border: none;
  border-radius: 2vw;
  padding: 2.5vw 5vw;
  font-size: 3.4vw;
  font-weight: 600;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: #fff;
  cursor: pointer;
}

.primary-btn:disabled {
  opacity: 0.6;
}

.primary-btn.small {
  padding: 1.8vw 4vw;
  font-size: 3.2vw;
}

.secondary-btn {
  border: 1px solid #ccc;
  border-radius: 2vw;
  padding: 1.8vw 4vw;
  font-size: 3.2vw;
  background: #fff;
  color: #444;
  cursor: pointer;
}

.secondary-btn:disabled {
  opacity: 0.6;
}

.empty-state {
  text-align: center;
  padding: 12vw 4vw;
  color: #888;
  font-size: 3.5vw;
}

.empty-state span {
  font-size: 10vw;
  display: block;
  margin-bottom: 2vw;
}

.fab-btn {
  position: fixed;
  right: 5vw;
  bottom: 18vw;
  width: 14vw;
  height: 14vw;
  border-radius: 50%;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(58, 123, 213, 0.4);
  cursor: pointer;
  font-size: 6vw;
  z-index: 20;
}

.sheet-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 50;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.sheet {
  width: 100%;
  max-height: 88vh;
  background: #fff;
  border-radius: 3vw 3vw 0 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4vw;
  border-bottom: 1px solid #eee;
  font-weight: 600;
  font-size: 4vw;
}

.link-btn {
  border: none;
  background: none;
  color: #3a7bd5;
  font-size: 3.5vw;
  cursor: pointer;
}

.sheet-body {
  padding: 4vw;
  overflow-y: auto;
}

.sheet-foot {
  padding: 3vw 4vw 5vw;
  border-top: 1px solid #eee;
}

.field-label {
  display: block;
  font-size: 3.2vw;
  color: #555;
  margin-bottom: 1.5vw;
  margin-top: 2vw;
}

.field-label:first-child {
  margin-top: 0;
}

.field-input,
.field-textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #ddd;
  border-radius: 2vw;
  padding: 2.5vw 3vw;
  font-size: 3.4vw;
}

.field-textarea {
  resize: vertical;
  min-height: 20vw;
}
</style>
