package com.tju.unify.conv.errand.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErrandPublishRequest {
    @Schema(description = "起点", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startPoint;
    @Schema(description = "终点", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endPoint;
    @Schema(description = "小费（元），默认 0")
    private BigDecimal tipAmount;
    @Schema(description = "期望时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expectTime;
    @Schema(description = "联系方式", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactInfo;
    @Schema(description = "补充说明")
    private String remark;
}
