package com.tju.elm.user.mapper;


import com.tju.elm.user.zoo.pojo.entity.Person;
import com.tju.elm.user.zoo.pojo.vo.PersonVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PersonMapper {
    void insert(Person person);

    @Select("select * from person where id = #{id}")
    Person getPersonByUserId(Long id);

    void updateById(Person updateDTO);

    @Select("select * from person")
    List<Person> list();

    @Select("SELECT u.username, p.phone, p.email, u.id, u.is_deleted " +  // 建议显式指定字段，避免歧义
            "FROM users u " +
            "INNER JOIN person p ON u.id = p.id " +  // 关键：INNER JOIN只保留两表都存在的记录
            "WHERE (u.username LIKE CONCAT('%', #{keyword}, '%') " +  // 括号确保OR逻辑范围正确
            "OR p.phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.email LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND u.is_deleted = 0")  // 筛选未删除的用户
    List<PersonVO> searchByKeyword(String keyword);
}

