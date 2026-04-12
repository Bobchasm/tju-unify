package com.tju.elm.user.mapper;

import com.tju.elm.user.zoo.pojo.entity.PermissionApplication;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PermissionApplicationMapper {

    void insert(PermissionApplication application);

    @Select("SELECT COUNT(*) FROM permission_application WHERE userId = #{currentUserId} AND status = 0")
    int countByUserId(Long currentUserId);

    @Select("SELECT * FROM permission_application WHERE id = #{id}")
    PermissionApplication selectById(@NotNull(message = "申请ID不能为空") Long id);

    @Update("UPDATE permission_application SET status = #{status},update_time = #{updateTime} WHERE id = #{id}")
    void updateAuditStatus(PermissionApplication application);

    @Select("SELECT * FROM permission_application WHERE status = 0 AND is_deleted = false")
    List<PermissionApplication> list();
}
