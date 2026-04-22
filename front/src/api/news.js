import request from './request'

/** @param {number} page 从 1 开始 @param {number} flag 0 表示全部栏目 @param {number} size 每页条数，最大 50 */
export const getNews = async (page = 1, flag = 0, size = 8) => {
  return await request.get('/unify-api/news/schoolNews/getByFlag', {
    params: { flag, page, size },
    headers: {
      'Cache-Control': 'no-cache',
      Pragma: 'no-cache'
    }
  })
}

export const getNewsDetail = async (id) => {
  return await request.get(`/unify-api/news/schoolNews/detail?id=${id}`)
}

/** 触发后端异步爬取天大新闻网最新内容入库 */
export const triggerNewsCrawler = async () => {
  return await request.post('/unify-api/news/schoolNews/crawler')
}

export default {
  getNews,
  getNewsDetail,
  triggerNewsCrawler
}
