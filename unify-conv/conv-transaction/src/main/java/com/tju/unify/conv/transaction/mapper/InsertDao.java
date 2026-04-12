package com.tju.unify.conv.transaction.mapper;

import com.tju.unify.conv.transaction.pojo.SecGoods;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InsertDao {

    @Insert("insert into second (id,title,qq,desc,phone,commit,look,img,time,price,category) values(#{id},#{title},#{qq},#{desc},#{phone},#{commit},#{look},#{img},#{time},#{price},#{category})")
    int Insert(SecGoods secGoods);

}
