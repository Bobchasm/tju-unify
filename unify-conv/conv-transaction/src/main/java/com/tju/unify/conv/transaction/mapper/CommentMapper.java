package com.tju.unify.conv.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tju.unify.conv.transaction.pojo.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
