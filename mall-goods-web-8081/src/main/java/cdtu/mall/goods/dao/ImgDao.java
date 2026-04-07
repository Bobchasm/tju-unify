package cdtu.mall.goods.dao;

import cdtu.mall.item.pojo.Images;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImgDao {

    @Select("select * from images where sku_id=#{skuId}")
    public List<Images> findAllBySkuId(String skuId);



}
