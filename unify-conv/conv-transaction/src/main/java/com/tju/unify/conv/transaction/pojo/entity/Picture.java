package com.tju.unify.conv.transaction.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("picture")
public class Picture {
    @Schema(description = "帖子id")
    private Integer postId;
    @Schema(description = "图片url")
    private String url;
    @Schema(description = "图片顺序")
    private Integer order;
}
