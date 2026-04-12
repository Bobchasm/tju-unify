package com.tju.elm.user.zoo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditPermissionDTO {
    @NotNull(message = "申请ID不能为空")
    @Schema(description = "权限申请记录ID")
    private Long id;

    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果（1-同意，2-拒绝）")
    private Integer auditResult;
}
