package cdtu.mall.recommend.Dao;

import cdtu.mall.item.pojo.Shop;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ShopDao  {

    @Select("select * from shop order by point desc")
    public List<Shop> getShop();
}
