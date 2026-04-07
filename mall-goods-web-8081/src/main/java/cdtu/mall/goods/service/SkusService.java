package cdtu.mall.goods.service;

import cdtu.mall.item.pojo.Sku;

import java.util.List;

public interface SkusService {

    public List<Sku> findAll(int pageNum,int pageSize,String key);

    public Sku findBySkuId(String id);
}
