package cdtu.mall.goods.service;

import cdtu.mall.item.pojo.Images;

import java.util.List;

public interface ImgService {

    public List<Images> findAllBySkuId(String skuId);
}
