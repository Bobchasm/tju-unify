package cdtu.mall.second.mapper;

import cdtu.mall.second.pojo.SecGoods;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InsertDao {

    @Insert("insert into second (id,title,qq,desc,phone,commit,look,img,time,price,category) values(#{id},#{title},#{qq},#{desc},#{phone},#{commit},#{look},#{img},#{time},#{price},#{category})")
    int Insert(SecGoods secGoods);

}
