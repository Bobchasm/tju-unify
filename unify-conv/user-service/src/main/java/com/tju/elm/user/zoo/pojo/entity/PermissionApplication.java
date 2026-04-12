package com.tju.elm.user.zoo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionApplication {
    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "申请人用户ID（关联users表）")
    private Long userId;

    @Schema(description = "申请状态（0-未审核，1-同意，2-拒绝）")
    private Integer status = 0; // 默认未审核

    @Schema(description = "逻辑删除")
    private Boolean isDeleted = false;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
