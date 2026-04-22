package com.tju.unify.conv.memo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conv_memo_task")
public class MemoTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memoId;
    private String title;
    @Schema(description = "是否完成 0/1")
    private Integer done;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @Schema(description = "0-正常 1-删除")
    private Integer isDeleted;
}
