package com.tju.unify.conv.memo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemoRemindUpdateRequest {
    @Schema(description = "为 true 时清除 remindAt，并可将 remindDone 置 0")
    private Boolean clearRemind;
    @Schema(description = "提醒时间；与 clearRemind 互斥时以 clearRemind 为准")
    private LocalDateTime remindAt;
    @Schema(description = "0-待处理 1-已处理")
    private Integer remindDone;
}
