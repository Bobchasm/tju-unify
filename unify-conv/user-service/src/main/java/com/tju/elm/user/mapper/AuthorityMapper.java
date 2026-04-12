package com.tju.elm.user.mapper;

import com.tju.elm.user.zoo.pojo.entity.Authority;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthorityMapper {
    @Select("SELECT * FROM authority WHERE name = #{name}")
    Authority findByName(String name);
}

