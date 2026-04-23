package com.tju.unify.conv.errand.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("conv_errand_order")
public class ErrandOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Schema(description = "发起人用户ID")
    private Long publisherId;
    private String startPoint;
    private String endPoint;
    private BigDecimal tipAmount;
    private LocalDateTime expectTime;
    private String contactInfo;
    private String remark;
    @Schema(description = "0-待接单 1-已接单 2-已完成 3-已取消")
    private Integer status;
    private Long runnerId;
    private LocalDateTime acceptTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @Schema(description = "0-正常 1-删除")
    private Integer isDeleted;
}
