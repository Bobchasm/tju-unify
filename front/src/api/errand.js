import request from './request'

export function errandPublish(data) {
  return request.post('/unify-api/errand/publish', data)
}

export function errandOpenList() {
  return request.get('/unify-api/errand/open')
}

export function errandMine() {
  return request.get('/unify-api/errand/mine')
}

export function errandDetail(id) {
  return request.get(`/unify-api/errand/${id}`)
}

export function errandAccept(id) {
  return request.post(`/unify-api/errand/${id}/accept`)
}

export function errandComplete(id) {
  return request.post(`/unify-api/errand/${id}/complete`)
}

export function errandCancel(id) {
  return request.post(`/unify-api/errand/${id}/cancel`)
}

export default {
  errandPublish,
  errandOpenList,
  errandMine,
  errandDetail,
  errandAccept,
  errandComplete,
  errandCancel
}
