package com.tju.unify.conv.memo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tju.unify.conv.memo.pojo.entity.Memo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface MemoMapper extends BaseMapper<Memo> {

    @Select("SELECT COUNT(*) FROM conv_memo_task t INNER JOIN conv_memo m ON t.memo_id = m.id "
            + "WHERE m.user_id = #{userId} AND m.is_deleted = 0 AND t.is_deleted = 0 AND t.done = 1 "
            + "AND COALESCE(t.update_time, t.create_time) >= #{since}")
    Long countCompletedTasksSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Select("SELECT COUNT(*) FROM conv_memo WHERE user_id = #{userId} AND is_deleted = 0 "
            + "AND COALESCE(update_time, create_time) >= #{since}")
    Long countMemosTouchedSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
