package cdtu.mall.goods.dao;

import cdtu.mall.item.pojo.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Repository
@Mapper
public interface SkusDao {

    //分页查询所有sku----根据skuName/categoryName
    @Select("SELECT * from sku where title like  CONCAT('%',#{key},'%') LIMIT #{pageNum},#{pageSize}")
    public List<Sku> findAll(int pageNum,int pageSize,String key);

    //通过skuid查找
    @Select("select * from sku where sku_id = #{id}")
    public Sku findBySkuId(String id);


}
