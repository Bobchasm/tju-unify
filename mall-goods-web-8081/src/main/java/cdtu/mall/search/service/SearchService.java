package cdtu.mall.search.service;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.item.response.RespGood;
import cdtu.mall.item.response.SearchInfo;
import cdtu.mall.search.pojo.Goods;
import cdtu.mall.search.pojo.SearchRequest;

import java.util.List;
import java.util.Map;

public interface SearchService {

    //导入数据
    public List<Goods> buildGoodsList(int pageNum, int pageSize );

    //查询接口
    public PageResult<Goods> search(SearchRequest searchRequest,int flag);

    //根据skuId查询
    public Goods findBySkuId(String skuId);

    //搜索相关信息
    public PageResult<SearchInfo> searchInfo(SearchRequest searchRequest);
}
