package com.tju.unify.conv.transaction.pojo.entity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    @Schema(description = "帖子id")
    private Integer id;
    @Schema(description = "发贴用户id")
    private Long userId;
    @Schema(description = "物品")
    private String title;
    @Schema(description = "详细描述")
    private String description;
    @Schema(description = "价格")
    private Float price;
    @Schema(description = "图片链接，多个用逗号分隔")
    private String images;
    @Schema(description = "状态: 0-进行中 1-关闭 2-自己可见")
    private Integer status;
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "逻辑删除 0-正常 1-删除")
    private Boolean isDeleted;
}
