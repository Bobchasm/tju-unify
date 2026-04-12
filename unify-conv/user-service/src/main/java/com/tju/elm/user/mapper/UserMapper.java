package com.tju.elm.user.mapper;

import com.tju.elm.user.zoo.pojo.dto.BusinessInfoDTO;
import com.tju.elm.user.zoo.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id} AND is_deleted = 0")
    User findById(Long id);
    @Select("SELECT * FROM users WHERE username = #{username} AND is_deleted = 0")
    User findByUsername(String username);
    User findByUsernameWithAuthorities(String username);
    User findByUserIdWithAuthorities(Long userId);
    void insert(User user);
    void update(User user);
    @Select("SELECT COUNT(*) FROM users WHERE is_deleted = 0")
    int count();
    @Insert("INSERT INTO user_authority (user_id, authority_name) VALUES (#{userId}, #{authorityName})")
    void insertUserAuthority(@Param("userId") Long userId, @Param("authorityName") String authorityName);

    @Select("select id from users where username = #{username}")
    Long getUserIdByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{userId} AND is_deleted = 0")
    Integer countUserById(@Param("userId") Long userId);

    @Update("UPDATE users SET activated = #{activated} WHERE id = #{id}")
    void updateActivated(User user);

    @Select("SELECT " +
            "    p.id AS userId, " +  // 注意这里改为userId，与DTO字段名一致
            "    u.username, " +
            "    p.phone, " +
            "    p.photo " +
            "FROM " +
            "    user_authority ua " +
            "    JOIN users u ON ua.user_id = u.id " +
            "    JOIN person p ON ua.user_id = p.id " +
            "WHERE " +
            "    ua.authority_name = 'BUSINESS' " +
            "    AND u.is_deleted = 0 ")
    List<BusinessInfoDTO> getAllActiveBusinesses();

    @Select("""
        <script>
            SELECT * FROM users u
                <where>
                    u.is_deleted = 0
                    AND
                    u.id in
                    <foreach collection='userIds' item='userId' open='(' separator=',' close=')'>
                        #{userId}
                    </foreach>
                </where>
        </script>
    """)
    List<User> getUserByIds(Set<Long> userIds);

}