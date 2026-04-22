package com.tju.unify.conv.memo.pojo.dto;

import com.tju.unify.conv.memo.pojo.entity.Memo;
import com.tju.unify.conv.memo.pojo.entity.MemoTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MemoDetailVO {
    @Schema(description = "备忘录主信息")
    private Memo memo;
    @Schema(description = "子任务列表")
    private List<MemoTask> tasks;
}
