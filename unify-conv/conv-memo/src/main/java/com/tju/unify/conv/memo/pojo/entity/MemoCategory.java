package com.tju.unify.conv.memo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conv_memo_category")
public class MemoCategory {
    @TableId(type = IdType.AUTO)
    @Schema(description = "分类ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "排序")
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @Schema(description = "0-正常 1-删除")
    private Integer isDeleted;
}
