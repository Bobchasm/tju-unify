package com.tju.unify.conv.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tju.unify.conv.transaction.pojo.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("""
        <script>
            select * from post where is_deleted = 0
            <if test="null!=userId">
                and user_id = #{userId}
            </if>
            <if test="null!=status">
                and status = #{status}
            </if>
            order by update_time desc
        </script>
    """)
    IPage<Post> getPosts(Page<Post> page, Long userId, Integer status);

    @Update("update post set title=#{title},description=#{description},price=#{price},images=#{images},status=#{status} where id=#{id}")
    Integer updatePost(Post post);

    Integer insertPost(Post post);
}
