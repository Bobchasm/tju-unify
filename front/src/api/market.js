import request from './request'

// 获取帖子列表
export const getPosts = async (pageNo = 1) => {
  return await request.get(`/unify-api/transaction/post/list?pageNo=${pageNo}`)
}

// 获取帖子详情
export const getPost = async (id) => {
  return await request.get(`/unify-api/transaction/post/detail?id=${id}`)
}

// 发布帖子
export const addPost = async (data) => {
  return await request.post('/unify-api/transaction/post/add', data)
}

// 搜索帖子（params 由 axios 编码，中文与特殊字符更安全）
export const searchPosts = async (keyword) => {
  const kw = (keyword ?? '').trim()
  return await request.get('/unify-api/transaction/post/search', { params: { keyword: kw } })
}

// 根据用户ID获取联系方式
export const getContactByUserId = async (userId) => {
  return await request.get(`/unify-api/transaction/contact/getByUserId?userId=${userId}`)
}

// 添加联系方式
export const addContact = async (data) => {
  return await request.post('/unify-api/transaction/contact/add', data)
}

// 更新联系方式
export const updateContact = async (data) => {
  return await request.post('/unify-api/transaction/contact/update', data)
}

// 获取帖子的交易请求列表
export const getRequests = async (postId) => {
  return await request.get(`/unify-api/transaction/request/list?postId=${postId}`)
}

// 创建交易请求（我想要）
export const addRequest = async (data) => {
  return await request.post('/unify-api/transaction/request/add', data)
}

// 更新交易请求状态
export const updateRequestStatus = async (id, status) => {
  return await request.get(`/unify-api/transaction/request/updateStatus?id=${id}&status=${status}`)
}

// 获取帖子的评论列表
export const getComments = async (postId) => {
  return await request.get(`/unify-api/transaction/comment/list?postId=${postId}`)
}

// 添加评论
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
