<template>
  <div class="page memo-page">
    <header>
      <div class="header-content">
        <div class="back-btn" @click="goBack">
          <i class="fa-solid fa-backward"></i>
        </div>
        <div class="page-title">
          <span class="title-icon">📝</span>
          <span>备忘录</span>
        </div>
        <div class="header-action" @click="openReminders" title="待提醒">
          <span>⏰</span>
        </div>
      </div>
      <div class="category-bar">
        <div
          class="chip"
          :class="{ active: filterCategoryId === null }"
          @click="setFilter(null)"
        >
          全部
        </div>
        <div
          v-for="c in categories"
          :key="c.id"
          class="chip"
          :class="{ active: filterCategoryId === c.id }"
          @click="setFilter(c.id)"
        >
          <span>{{ c.name }}</span>
          <span class="chip-del" @click.stop="openConfirmModal(c)">×</span>
        </div>
        <div class="chip chip-add" @click="openCategoryModal"><span><i class="fa-solid fa-plus" style="color: rgb(251, 251, 252);"></i></span></div>
      </div>
      <div class="notif-tip">
        <div class="notif-tip-actions">
          <button
            v-if="notifPerm === 'default'"
            type="button"
            class="notif-enable-btn"
            @click="enableDesktopNotify"
          >
            开启桌面提醒
          </button>
          <span v-else-if="notifPerm === 'granted'" class="notif-status ok">已开启系统通知</span>
          <span v-else-if="notifPerm === 'denied'" class="notif-status warn">浏览器已拒绝通知，可在地址栏「网站设置」中改为允许</span>
          <span v-else class="notif-status warn">当前环境不支持系统通知</span>
          <p class="notif-tip-text">
          到点后会尝试弹出<strong>系统通知</strong>（需授权、保持登录；页面在后台时浏览器可能降频，建议重要事项用手机日历作备份）。
        </p>
        </div>
        
        
        
      </div>
    </header>

    <div class="memo-list">
      <div v-if="loading" class="hint">加载中…</div>
      <div
        v-for="m in memos"
        :key="m.id"
        class="memo-card"
        @click="openEditor(m.id)"
      >
        <div class="memo-card-top">
          <span v-if="m.pinned === 1" class="pin">📌</span>
          <h3 class="memo-title">{{ m.title }}</h3>
          <button class="icon-btn" type="button" @click.stop="togglePin(m)">
            {{ m.pinned === 1 ? '取消置顶' : '置顶' }}
          </button>
        </div>
        <p v-if="m.content" class="memo-excerpt">{{ m.content }}</p>
        <div v-if="m.remindAt && m.remindDone === 0" class="memo-remind">
          ⏰ {{ formatDateTime(m.remindAt) }}
        </div>
      </div>
      <div v-if="!loading && memos.length === 0" class="empty-state">
        <span>📄</span>
        <p>暂无备忘录</p>
        <p class="empty-hint">右下角添加一条吧</p>
      </div>
    </div>

    <div class="fab-btn" @click="openNewMemo">
      <i class="fa-solid fa-plus" style="color: rgb(251, 251, 252);"></i>
    </div>

    <!-- 编辑抽屉 -->
    <div v-if="editor.show" class="sheet-mask" @click.self="closeEditor">
      <div class="sheet" @click.stop>
        <div class="sheet-head">
          <span>{{ editor.id ? '编辑备忘录' : '新建备忘录' }}</span>
          <button type="button" class="link-btn" @click="closeEditor">关闭</button>
        </div>
        <div class="sheet-body">
          <label class="field-label">标题</label>
          <input v-model="editor.title" class="field-input" type="text" placeholder="标题" />

          <label class="field-label">正文</label>
          <textarea v-model="editor.content" class="field-textarea" rows="5" placeholder="正文（可选）" />

          <label class="field-label">分类</label>
          <select v-model.number="editor.categoryId" class="field-input">
            <option :value="0">未分类</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>

          <label class="field-row">
            <input v-model="editor.pinned" type="checkbox" />
            置顶
          </label>

          <label class="field-label">提醒时间</label>
          <div class="remind-row">
            <input v-model="editor.remindLocal" class="field-input" type="datetime-local" />
            <button type="button" class="secondary-btn" @click="clearEditorRemind">清除</button>
          </div>

          <template v-if="editor.id">
            <label class="field-label">子任务</label>
            <div class="task-list">
              <label v-for="t in editor.tasks" :key="t.id" class="task-row">
                <input
                  type="checkbox"
                  :checked="t.done === 1"
                  @change="onTaskToggle(t, $event.target.checked)"
                />
                <span :class="{ done: t.done === 1 }">{{ t.title }}</span>
                <button type="button" class="task-del" @click.prevent="removeTask(t)">删</button>
              </label>
            </div>
            <div class="task-add-row">
              <input v-model="newTaskTitle" class="field-input" type="text" placeholder="新子任务" />
              <button type="button" class="primary-btn small" @click="addTask">添加</button>
            </div>
          </template>
        </div>
        <div class="sheet-foot">
          <button v-if="editor.id" type="button" class="danger-btn" :disabled="saving" @click="deleteCurrentMemo">
            删除
          </button>
          <button type="button" class="primary-btn" :disabled="saving" @click="saveMemo">保存</button>
        </div>
      </div>
    </div>

    <!-- 待提醒浮层 -->
    <div v-if="reminderPanel.show" class="sheet-mask" @click.self="reminderPanel.show = false">
      <div class="sheet sheet-small" @click.stop>
        <div class="sheet-head">
          <span>待提醒</span>
          <button type="button" class="link-btn" @click="reminderPanel.show = false">关闭</button>
        </div>
        <div class="sheet-body">
          <div v-if="reminderPanel.list.length === 0" class="hint">当前时间窗内暂无待处理提醒</div>
          <div
            v-for="r in reminderPanel.list"
            :key="r.id"
            class="remind-item"
            @click="openFromReminder(r.id)"
          >
            <div class="remind-title">{{ r.title }}</div>
            <div class="remind-time">{{ formatDateTime(r.remindAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 自定义确认弹窗 -->
<div v-if="showConfirmModal" class="confirm-modal" @click.self="closeConfirmModal">
  <div class="confirm-modal-content">
    <div class="confirm-modal-header">
      <h3>确认删除</h3>
    </div>
    <div class="confirm-modal-body">
      <p>删除分类「{{ deletingCategory?.name }}」？</p>
      <p class="confirm-hint">下属备忘将变为未分类。</p>
    </div>
    <div class="confirm-modal-footer">
      <button class="confirm-btn cancel" @click="closeConfirmModal">取消</button>
      <button class="confirm-btn confirm" @click="confirmDeleteCategory">确认删除</button>
    </div>
  </div>
</div>


<!-- 添加分类弹窗 -->
<div v-if="showCategoryModal" class="confirm-modal" @click.self="closeCategoryModal">
  <div class="confirm-modal-content">
    <div class="confirm-modal-header">
      <h3>新建分类</h3>
    </div>
    <div class="confirm-modal-body">
      <p>请输入分类名称：</p>
      <input 
        v-model="newCategoryName" 
        class="modal-input" 
        type="text" 
        placeholder="例如：工作、学习、生活"
        autofocus
      />
    </div>
    <div class="confirm-modal-footer">
      <button class="confirm-btn cancel" @click="closeCategoryModal">取消</button>
      <button class="confirm-btn confirm" @click="confirmAddCategory">确认添加</button>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import auth from '../api/auth'
import * as memoApi from '../api/memo'
import {
  enableMemoDesktopNotifications,
  getNotificationPermission,
  startMemoReminderScheduler
} from '../composables/useMemoReminders'
import { toast } from '../utils/toast'

const router = useRouter()
const notifPerm = ref(getNotificationPermission())

const categories = ref([])
const memos = ref([])
const filterCategoryId = ref(null)
const loading = ref(false)
const saving = ref(false)
const newTaskTitle = ref('')
const showConfirmModal = ref(false)
const deletingCategory = ref(null)
const showCategoryModal = ref(false)
const newCategoryName = ref('')

const openConfirmModal = (category) => {
  deletingCategory.value = category
  showConfirmModal.value = true
}

const closeConfirmModal = () => {
  showConfirmModal.value = false
  deletingCategory.value = null
}

const confirmDeleteCategory = async () => {
  if (!deletingCategory.value) return
  try {
    await memoApi.categoryDelete(deletingCategory.value.id)
    if (filterCategoryId.value === deletingCategory.value.id) filterCategoryId.value = null
    await loadCategories()
    await loadMemos()
  } catch (e) {
    console.error(e)
    toast.warning('删除失败')
  } finally {
    closeConfirmModal()
  }
}

const editor = reactive({
  show: false,
  id: null,
  title: '',
  content: '',
  categoryId: 0,
  pinned: false,
  remindLocal: '',
  tasks: []
})

const reminderPanel = reactive({
  show: false,
  list: []
})

const goBack = () => router.push('/')

const ensureLogin = () => {
  if (!auth.isAuthenticated()) {
    router.replace('/trade/login')
    return false
  }
  return true
}

const unwrap = (res) => {
  if (res && res.success) return res.data
  throw new Error(res?.message || '请求失败')
}

const loadCategories = async () => {
  const res = await memoApi.categoryList()
  categories.value = unwrap(res) || []
}

const loadMemos = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterCategoryId.value != null) params.categoryId = filterCategoryId.value
    const res = await memoApi.memoList(params)
    memos.value = unwrap(res) || []
  } finally {
    loading.value = false
  }
}

const setFilter = (id) => {
  filterCategoryId.value = id
  loadMemos()
}

const formatDateTime = (v) => {
  if (!v) return ''
  if (typeof v === 'string') return v.replace('T', ' ').slice(0, 16)
  return String(v)
}

const toIsoRemind = (localVal) => {
  if (!localVal) return null
  if (localVal.length === 16) return `${localVal}:00`
  return localVal
}

const addCategory = async () => {
  const name = window.prompt('新分类名称')
  if (!name || !name.trim()) return
  await memoApi.categoryAdd({ name: name.trim(), sortOrder: 0 })
  await loadCategories()
}

const removeCategory = async (c) => {
  if (!window.confirm(`删除分类「${c.name}」？下属备忘将变为未分类。`)) return
  await memoApi.categoryDelete(c.id)
  if (filterCategoryId.value === c.id) filterCategoryId.value = null
  await loadCategories()
  await loadMemos()
}

const togglePin = async (m) => {
  try {
    await memoApi.memoSetPinned(m.id, m.pinned !== 1)
    await loadMemos()
  } catch (e) {
    console.error(e)
    toast.warning('操作失败')
  }
}

const openNewMemo = () => {
  editor.show = true
  editor.id = null
  editor.title = ''
  editor.content = ''
  editor.categoryId = filterCategoryId.value || 0
  editor.pinned = false
  editor.remindLocal = ''
  editor.tasks = []
  newTaskTitle.value = ''
}

const closeEditor = () => {
  editor.show = false
}

const openEditor = async (id) => {
  try {
    const res = await memoApi.memoDetail(id)
    const data = unwrap(res)
    const memo = data.memo
    editor.show = true
    editor.id = memo.id
    editor.title = memo.title || ''
    editor.content = memo.content || ''
    editor.categoryId = memo.categoryId || 0
    editor.pinned = memo.pinned === 1
    editor.remindLocal = memo.remindAt ? String(memo.remindAt).slice(0, 16) : ''
    editor.tasks = data.tasks || []
    newTaskTitle.value = ''
  } catch (e) {
    console.error(e)
    toast.warning('加载失败')
  }
}

const clearEditorRemind = () => {
  editor.remindLocal = ''
}

const saveMemo = async () => {
  if (!editor.title.trim()) {
    toast.warning('请填写标题')
    return
  }
  saving.value = true
  try {
    const remindAt = toIsoRemind(editor.remindLocal)
    const categoryId = editor.categoryId === 0 ? null : editor.categoryId
    if (!editor.id) {
      const body = {
        title: editor.title.trim(),
        content: editor.content || '',
        categoryId,
        pinned: editor.pinned,
        sortOrder: 0,
        remindAt: remindAt || undefined,
        tasks: []
      }
      await memoApi.memoAdd(body)
    } else {
      const body = {
        id: editor.id,
        title: editor.title.trim(),
        content: editor.content,
        pinned: editor.pinned ? 1 : 0,
        sortOrder: 0
      }
      if (editor.categoryId === 0) {
        body.categoryId = -1
      } else {
        body.categoryId = editor.categoryId
      }
      if (remindAt) {
        body.remindAt = remindAt
        body.remindDone = 0
      }
      await memoApi.memoUpdate(body)
      if (!remindAt) {
        await memoApi.memoSetRemind(editor.id, { clearRemind: true })
      }
    }
    closeEditor()
    await loadMemos()
  } catch (e) {
    console.error(e)
    toast.warning('保存失败')
  } finally {
    saving.value = false
  }
}

const deleteCurrentMemo = async () => {
  if (!editor.id) return
  if (!window.confirm('确定删除该备忘录？')) return
  saving.value = true
  try {
    await memoApi.memoDelete(editor.id)
    closeEditor()
    await loadMemos()
  } finally {
    saving.value = false
  }
}

const onTaskToggle = async (t, checked) => {
  try {
    await memoApi.taskSetDone(t.id, checked)
    t.done = checked ? 1 : 0
  } catch (e) {
    console.error(e)
    toast.warning('更新子任务失败')
  }
}

const addTask = async () => {
  if (!editor.id || !newTaskTitle.value.trim()) return
  try {
    await memoApi.taskAdd({
      memoId: editor.id,
      title: newTaskTitle.value.trim(),
      done: 0,
      sortOrder: editor.tasks.length
    })
    newTaskTitle.value = ''
    const res = await memoApi.memoDetail(editor.id)
    const data = unwrap(res)
    editor.tasks = data.tasks || []
  } catch (e) {
    console.error(e)
    toast.warning('添加子任务失败')
  }
}

const removeTask = async (t) => {
  if (!window.confirm('删除该子任务？')) return
  try {
    await memoApi.taskDelete(t.id)
    editor.tasks = editor.tasks.filter((x) => x.id !== t.id)
  } catch (e) {
    console.error(e)
    toast.warning('删除失败')
  }
}

const openReminders = async () => {
  try {
    const res = await memoApi.memoRemindersDue()
    reminderPanel.list = unwrap(res) || []
    reminderPanel.show = true
  } catch (e) {
    console.error(e)
    toast.warning('加载提醒失败')
  }
}

const openFromReminder = (id) => {
  reminderPanel.show = false
  openEditor(id)
}

const refreshNotifPerm = () => {
  notifPerm.value = getNotificationPermission()
}

const enableDesktopNotify = async () => {
  const r = await enableMemoDesktopNotifications()
  refreshNotifPerm()
  if (r.ok) {
    startMemoReminderScheduler()
    toast.warning('已开启。到点后若本标签页仍打开，将弹出系统通知。')
  } else if (r.reason === 'denied') {
    toast.warning('未获得通知权限，请在浏览器里允许本站发送通知。')
  } else {
    toast.warning('未能开启通知，请重试或更换浏览器。')
  }
}

onMounted(async () => {
  if (!ensureLogin()) return
  refreshNotifPerm()
  startMemoReminderScheduler()
  try {
    await loadCategories()
    await loadMemos()
  } catch (e) {
    console.error(e)
    toast.warning('加载失败，请确认已登录且网关可用')
  }
})



// 打开添加分类弹窗
const openCategoryModal = () => {
  newCategoryName.value = ''
  showCategoryModal.value = true
}

// 关闭添加分类弹窗
const closeCategoryModal = () => {
  showCategoryModal.value = false
  newCategoryName.value = ''
}

// 确认添加分类
const confirmAddCategory = async () => {
  const name = newCategoryName.value.trim()
  if (!name) {
    alert('请输入分类名称')
    return
  }
  try {
    await memoApi.categoryAdd({ name: name.trim(), sortOrder: 0 })
    await loadCategories()
    closeCategoryModal()
  } catch (e) {
    console.error(e)
    alert('添加失败')
  }
}
</script>

<style scoped>
.memo-page {
  min-height: 100vh;
  background: #f8f9fa;
  padding-bottom: 24vw;
  /* 添加强制重置 */
  margin: 0;
  padding-left: 0;
  padding-right: 0;
  padding-top: 0;
}

.memo-page header {
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  padding: 4vw 4vw 3vw;
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.25);
  border-radius: 0;
  margin: 0;
  width: 100%;
  /* 确保没有负边距 */
  margin-left: 0;
  margin-right: 0;
}
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2vw;
}

.back-btn,
.header-action {
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

.category-bar {
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  gap: 2vw;
  margin-top: 3vw;
  padding-bottom: 1vw;
  -webkit-overflow-scrolling: touch;
}

.chip {
  flex-shrink: 0;
  padding: 1.8vw 3.2vw;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 3vw;
  display: flex;
  align-items: center;
  gap: 1vw;
  cursor: pointer;
  border: 1px solid transparent;
}

.chip.active {
  background: #fff;
  color: #3a7bd5;
  font-weight: 600;
}

.chip-add {
  font-weight: bold;
}

.chip-del {
  opacity: 0.85;
  font-size: 3.6vw;
  line-height: 1;
}

.notif-tip {
  margin-top: 3vw;
  padding: 2.5vw 3vw;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 2vw;
  color: #fff;
}

.notif-tip-text {
  margin: 0 0 2vw;
  font-size: 2.8vw;
  line-height: 1.45;
  opacity: 0.95;
}

.notif-tip-text strong {
  font-weight: 600;
}

.notif-tip-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2vw;
}

.notif-enable-btn {
  border: none;
  border-radius: 2vw;
  padding: 1.8vw 3.5vw;
  font-size: 3vw;
  font-weight: 600;
  background: #fff;
  color: #3a7bd5;
  cursor: pointer;
}

.notif-status {
  font-size: 2.8vw;
}

.notif-status.ok {
  opacity: 0.95;
}

.notif-status.warn {
  opacity: 0.9;
  line-height: 1.35;
}

.memo-list {
  padding: 4vw;
}

.memo-card {
  background: #fff;
  border-radius: 2.5vw;
  padding: 4vw;
  margin-bottom: 3vw;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
}

.memo-card-top {
  display: flex;
  align-items: flex-start;
  gap: 2vw;
}

.pin {
  font-size: 3.5vw;
}

.memo-title {
  flex: 1;
  margin: 0;
  font-size: 4vw;
  color: #222;
  line-height: 1.35;
}

.icon-btn {
  flex-shrink: 0;
  font-size: 2.6vw;
  padding: 1vw 2vw;
  border: none;
  border-radius: 1vw;
  background: #eef4ff;
  color: #3a7bd5;
  cursor: pointer;
}

.memo-excerpt {
  margin: 2vw 0 0;
  font-size: 3.2vw;
  color: #666;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.memo-remind {
  margin-top: 2vw;
  font-size: 2.8vw;
  color: #c45c26;
}

.empty-state {
  text-align: center;
  padding: 12vw 0;
  color: #999;
}

.empty-state span {
  font-size: 10vw;
  display: block;
  margin-bottom: 2vw;
  opacity: 0.5;
}

.empty-hint {
  font-size: 3vw;
  color: #bbb;
}

.hint {
  text-align: center;
  color: #888;
  font-size: 3.2vw;
  padding: 4vw;
}

.fab-btn {
  position: fixed;
  right: 5vw;
  bottom: 8vw;
  width: 14vw;
  height: 14vw;
  border-radius: 50%;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(58, 123, 213, 0.45);
  cursor: pointer;
  z-index: 40;
}

.fab-icon {
  color: #fff;
  font-size: 8vw;
  font-weight: 300;
  line-height: 1;
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
  border-radius: 4vw 4vw 0 0;
  display: flex;
  flex-direction: column;
  animation: sheetUp 0.22s ease-out;
}

.sheet-small {
  max-height: 70vh;
}

@keyframes sheetUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
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
  font-size: 3.4vw;
  cursor: pointer;
}

.sheet-body {
  padding: 4vw;
  overflow-y: auto;
  flex: 1;
}

.sheet-foot {
  display: flex;
  justify-content: flex-end;
  gap: 3vw;
  padding: 3vw 4vw calc(3vw + env(safe-area-inset-bottom));
  border-top: 1px solid #eee;
}

.field-label {
  display: block;
  font-size: 3vw;
  color: #666;
  margin: 2vw 0 1vw;
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
}

.field-row {
  display: flex;
  align-items: center;
  gap: 2vw;
  margin: 3vw 0;
  font-size: 3.4vw;
}

.remind-row {
  display: flex;
  gap: 2vw;
  align-items: center;
}

.remind-row .field-input {
  flex: 1;
}

.primary-btn {
  border: none;
  border-radius: 2vw;
  padding: 2.5vw 5vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: #fff;
  font-size: 3.4vw;
  cursor: pointer;
}

.primary-btn.small {
  padding: 2vw 3vw;
  flex-shrink: 0;
}

.primary-btn:disabled {
  opacity: 0.55;
}

.secondary-btn {
  border: 1px solid #ccc;
  border-radius: 2vw;
  padding: 2.5vw 3vw;
  background: #f5f5f5;
  font-size: 3vw;
  cursor: pointer;
}

.danger-btn {
  border: none;
  border-radius: 2vw;
  padding: 2.5vw 4vw;
  background: #fff0f0;
  color: #c0392b;
  font-size: 3.4vw;
  margin-right: auto;
  cursor: pointer;
}

.task-list {
  margin-bottom: 2vw;
}

.task-row {
  display: flex;
  align-items: center;
  gap: 2vw;
  padding: 2vw 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 3.4vw;
  cursor: pointer;
}

.task-row .done {
  text-decoration: line-through;
  color: #999;
}

.task-del {
  margin-left: auto;
  border: none;
  background: none;
  color: #999;
  font-size: 3vw;
  cursor: pointer;
}

.task-add-row {
  display: flex;
  gap: 2vw;
  margin-top: 2vw;
}

.task-add-row .field-input {
  flex: 1;
}

.remind-item {
  padding: 3vw;
  border-radius: 2vw;
  background: #f8f9fa;
  margin-bottom: 2vw;
  cursor: pointer;
}

.remind-title {
  font-size: 3.6vw;
  font-weight: 500;
  color: #333;
}

.remind-time {
  font-size: 3vw;
  color: #c45c26;
  margin-top: 1vw;
}


/* 自定义确认弹窗 */
.confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.confirm-modal-content {
  width: 70%;
  max-width: 300px;
  background: white;
  border-radius: 4vw;
  overflow: hidden;
  animation: modalPop 0.2s ease;
}

@keyframes modalPop {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.confirm-modal-header {
  padding: 4vw;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  text-align: center;
}

.confirm-modal-header h3 {
  color: white;
  font-size: 4.5vw;
  margin: 0;
  font-weight: 600;
}

.confirm-modal-body {
  padding: 5vw 4vw;
  text-align: center;
}

.confirm-modal-body p {
  font-size: 3.8vw;
  color: #333;
  margin: 0 0 2vw;
}

.confirm-hint {
  font-size: 3vw !important;
  color: #999 !important;
}

.confirm-modal-footer {
  display: flex;
  gap: 3vw;
  padding: 3vw 4vw 5vw;
}

.confirm-btn {
  flex: 1;
  padding: 3vw;
  border: none;
  border-radius: 3vw;
  font-size: 3.6vw;
  font-weight: 500;
  cursor: pointer;
}

.confirm-btn.cancel {
  background: #f0f0f0;
  color: #666;
}

.confirm-btn.confirm {
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  color: white;
}

.confirm-btn:active {
  transform: scale(0.97);
}


.modal-input {
  width: 100%;
  padding: 3vw;
  border: 1px solid #ddd;
  border-radius: 2vw;
  font-size: 3.6vw;
  margin-top: 2vw;
  box-sizing: border-box;
}

.modal-input:focus {
  outline: none;
  border-color: #3a7bd5;
}
</style>
