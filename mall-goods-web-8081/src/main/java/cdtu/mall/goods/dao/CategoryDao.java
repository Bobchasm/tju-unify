package cdtu.mall.goods.dao;

import cdtu.mall.item.pojo.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryDao {

    @Select("select * from category")
    public List<Category> findAll();

    @Select("select * from category where category_name like CONCAT('%',#{keyName},'%')")
    public List<Category> findListByCategoryName(String keyName);

    @Select("select * from category where category_id=#{cid}")
    public List<Category> findOneByCategoryId(String cid);
}
