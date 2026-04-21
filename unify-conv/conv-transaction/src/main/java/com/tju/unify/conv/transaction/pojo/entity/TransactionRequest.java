package com.tju.unify.conv.transaction.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @Schema(description = "请求id")
    private Integer id;
    @Schema(description = "帖子id")
    private Integer postId;
    @Schema(description = "买家id")
    private Long buyerId;
    @Schema(description = "卖家id")
    private Long sellerId;
    @Schema(description = "状态: 0-待处理 1-已同意 2-已拒绝 3-已取消")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "逻辑删除 0-正常 1-删除")
    private Boolean isDeleted;
}
