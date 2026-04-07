package cdtu.mall.recommend.service;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.item.pojo.Sku;

import java.util.List;

public interface SkusService {

    public List<Sku> findAll(int pageNum, int pageSize, String key);

    public Sku findBySkuId(String id);

}
