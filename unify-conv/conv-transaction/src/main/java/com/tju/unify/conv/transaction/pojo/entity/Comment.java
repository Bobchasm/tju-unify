package com.tju.unify.conv.transaction.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Schema(description = "评论id")
    private Integer id;
    @Schema(description = "帖子id")
    private Integer postId;
    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "逻辑删除 0-正常 1-删除")
    private Boolean isDeleted;
}
