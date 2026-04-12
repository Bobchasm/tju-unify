package com.tju.elm.user.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAuthorityMapper {
    @Select("SELECT COUNT(*) FROM user_authority WHERE user_id = #{userId} AND authority_name = #{authority}")
    int countByUserIdAndAuthority(Long userId, String authority);

    @Insert("INSERT INTO user_authority (user_id, authority_name) VALUES (#{userId}, #{authority})")
    void insertUserAuthority(Long userId, String authority);
}
