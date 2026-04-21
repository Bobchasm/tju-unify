import request from './request'

export const getNews = async (page = 1, flag = 0) => {
  return await request.get(`/unify-api/news/schoolNews/getByFlag?flag=${flag}&page=${page}`)
}

export const getNewsDetail = async (id) => {
  return await request.get(`/unify-api/news/schoolNews/detail?id=${id}`)
}

export default {
  getNews,
  getNewsDetail
}
