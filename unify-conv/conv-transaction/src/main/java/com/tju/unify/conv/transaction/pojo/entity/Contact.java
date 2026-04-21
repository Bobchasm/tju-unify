package com.tju.unify.conv.transaction.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("contact")
public class Contact {
    @Schema(description = "用户id")
    @TableId
    private Long userId;
    @Schema(description = "联系方式内容")
    private String contact;
    @Schema(description = "联系方式种类 0-qq 1-wx 2-other")
    private Integer type;
    @Schema(description = "种类为其他时的选填")
    @TableField("type_other")
    private String other;
    @Schema(description = "逻辑删除 0-正常 1-删除")
    private Boolean isDeleted;
}
