package cdtu.mall.recommend.service;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.recommend.pojo.Goods;


public interface RecommendService {

    //查询接口
    public PageResult<Goods> searchAll(int pageNum);
}
