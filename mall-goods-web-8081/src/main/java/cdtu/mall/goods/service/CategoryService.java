package cdtu.mall.goods.service;

import cdtu.mall.item.pojo.Category;

import java.util.List;

public interface CategoryService {

    public List<Category> findAll();

    /**
     * @Describ //为了商品查询时检索分类查询商品，那么我需要得到分类名称中包含该关键字的所有分类情况，在根据分类信息找到下级所有商品
     * @param keyName
     * @return
     */
    public List<Category> findListByCategoryName(String keyName);

    public List<Category> findOneByCategoryId(String cid);
}
