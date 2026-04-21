package com.tju.unify.conv.transaction.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    @Schema(description = "评论id")
    private Integer id;
    @Schema(description = "帖子id")
    private Integer postId;
    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
