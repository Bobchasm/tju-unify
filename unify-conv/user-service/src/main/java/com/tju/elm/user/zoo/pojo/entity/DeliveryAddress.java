package com.tju.elm.user.zoo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {
    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人性别（0-女，1-男）")
    private Integer contactSex;

    @Schema(description = "联系人电话")
    private String contactTel;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "创建人ID")
    private Long creator;

    @Schema(description = "是否删除")
    private Boolean isDeleted;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "更新人ID")
    private Long updater;

    @Schema(description = "所属用户ID")
    private Long userId;

    // 关联字段
    @Schema(description = "所属用户")
    private User user;

}
