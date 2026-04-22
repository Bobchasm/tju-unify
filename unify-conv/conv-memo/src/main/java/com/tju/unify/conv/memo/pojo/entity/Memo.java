package com.tju.unify.conv.memo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conv_memo")
public class Memo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    @Schema(description = "分类ID，可空")
    private Long categoryId;
    private String title;
    private String content;
    @Schema(description = "是否置顶 0/1")
    private Integer pinned;
    private Integer sortOrder;
    @Schema(description = "提醒时间")
    private LocalDateTime remindAt;
    @Schema(description = "提醒是否已处理 0-否 1-是")
    private Integer remindDone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @Schema(description = "0-正常 1-删除")
    private Integer isDeleted;
}
