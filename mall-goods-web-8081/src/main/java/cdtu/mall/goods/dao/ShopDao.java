package cdtu.mall.goods.dao;

import cdtu.mall.item.pojo.Category;
import cdtu.mall.item.pojo.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopDao {

    @Select("select * from shop where sku_id=#{skuId}")
    public List<Shop > findOneBySkuId(String skuId);



}
