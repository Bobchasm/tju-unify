import request from './request'

export const getPosts = async (pageNo = 1) => {
  return await request.get(`/unify-api/transaction/post/list?pageNo=${pageNo}`)
}

export const getPost = async (id) => {
  return await request.get(`/unify-api/transaction/post/detail?id=${id}`)
}

export const addPost = async (data) => {
  return await request.post('/unify-api/transaction/post/add', data)
}

export const searchPosts = async (keyword) => {
  const kw = (keyword ?? '').trim()
  return await request.get('/unify-api/transaction/post/search', { params: { keyword: kw } })
}

export const getContactByUserId = async (userId) => {
  return await request.get(`/unify-api/transaction/contact/getByUserId?userId=${userId}`)
}

export const addContact = async (data) => {
  return await request.post('/unify-api/transaction/contact/add', data)
}

export const updateContact = async (data) => {
  return await request.post('/unify-api/transaction/contact/update', data)
}

export const getRequests = async (postId) => {
  return await request.get(`/unify-api/transaction/request/list?postId=${postId}`)
}

export const addRequest = async (data) => {
  return await request.post('/unify-api/transaction/request/add', data)
}

export const updateRequestStatus = async (id, status) => {
  return await request.get(`/unify-api/transaction/request/updateStatus?id=${id}&status=${status}`)
}

export const getComments = async (postId) => {
  return await request.get(`/unify-api/transaction/comment/list?postId=${postId}`)
}

export const addComment = async (data) => {
  return await request.post('/unify-api/transaction/comment/add', data)
}

export default {
  getPosts,
  getPost,
  addPost,
  searchPosts,
  getContactByUserId,
  addContact,
  updateContact,
  getRequests,
  addRequest,
  updateRequestStatus,
  getComments,
  addComment
}
