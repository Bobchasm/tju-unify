package com.tju.unify.conv.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tju.unify.conv.transaction.pojo.entity.Picture;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<Picture> {

    @Insert("""
        <script>
            INSERT INTO picture (post_id, url, `order`)
            VALUES
            <foreach collection="pictureList" item="item" separator=",">
                (#{item.postId}, #{item.url}, #{item.order})
            </foreach>
        </script>
    """)
    Integer insertBatch(List<Picture> pictureList);
}
