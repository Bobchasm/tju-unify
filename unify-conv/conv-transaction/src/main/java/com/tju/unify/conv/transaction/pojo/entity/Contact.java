package com.tju.unify.conv.transaction.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contact {
    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "联系方式内容")
    private String contact;
    @Schema(description = "联系方式种类 0-qq 1-wx 2-other")
    private Integer type;
    @Schema(description = "种类为其他时的选填")
    private String other;
    @Schema(description = "逻辑删除 0-正常 1-删除")
    private Boolean isDeleted;
}
