import request from './request'

/** @typedef {{id?:number,userId?:number,name:string,sortOrder?:number}} MemoCategory */
/** @typedef {{id?:number,userId?:number,categoryId?:number|null,title:string,content?:string,pinned?:number,sortOrder?:number,remindAt?:string|null,remindDone?:number}} Memo */
/** @typedef {{id?:number,memoId:number,title:string,done?:number,sortOrder?:number}} MemoTask */

/** 小智 / 开场白：聚合近一周备忘与提醒 */
export function memoAgentSnapshot() {
  return request.get('/unify-api/memo/agent/snapshot')
}

export function categoryList() {
  return request.get('/unify-api/memo/category/list')
}

export function categoryAdd(data) {
  return request.post('/unify-api/memo/category/add', data)
}

export function categoryUpdate(data) {
  return request.put('/unify-api/memo/category/update', data)
}

export function categoryDelete(id) {
  return request.delete(`/unify-api/memo/category/${id}`)
}

export function memoList(params) {
  return request.get('/unify-api/memo/list', { params })
}

export function memoDetail(id) {
  return request.get(`/unify-api/memo/${id}`)
}

export function memoAdd(data) {
  return request.post('/unify-api/memo/add', data)
}

export function memoUpdate(data) {
  return request.put('/unify-api/memo/update', data)
}

export function memoDelete(id) {
  return request.delete(`/unify-api/memo/${id}`)
}

export function memoSetPinned(id, pinned) {
  return request.put(`/unify-api/memo/${id}/pinned`, null, { params: { pinned } })
}

export function memoSetRemind(id, data) {
  return request.put(`/unify-api/memo/${id}/remind`, data)
}

export function memoRemindersDue(params) {
  return request.get('/unify-api/memo/reminders/due', { params })
}

export function taskList(memoId) {
  return request.get('/unify-api/memo/task/list', { params: { memoId } })
}

export function taskAdd(data) {
  return request.post('/unify-api/memo/task/add', data)
}

export function taskUpdate(data) {
  return request.put('/unify-api/memo/task/update', data)
}

export function taskSetDone(id, done) {
  return request.put(`/unify-api/memo/task/${id}/done`, null, { params: { done } })
}

export function taskDelete(id) {
  return request.delete(`/unify-api/memo/task/${id}`)
}

export default {
  memoAgentSnapshot,
  categoryList,
  categoryAdd,
  categoryUpdate,
  categoryDelete,
  memoList,
  memoDetail,
  memoAdd,
  memoUpdate,
  memoDelete,
  memoSetPinned,
  memoSetRemind,
  memoRemindersDue,
  taskList,
  taskAdd,
  taskUpdate,
  taskSetDone,
  taskDelete
}
