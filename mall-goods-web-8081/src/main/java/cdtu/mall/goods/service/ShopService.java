package cdtu.mall.goods.service;

import cdtu.mall.item.pojo.Shop;

import java.util.List;

public interface ShopService {

    public List<Shop > findOneBySkuId(String skuId);
}
