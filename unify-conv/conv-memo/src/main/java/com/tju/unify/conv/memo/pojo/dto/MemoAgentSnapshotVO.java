package com.tju.unify.conv.memo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class MemoAgentSnapshotVO {
    @Schema(description = "近7天勾选的子任务数")
    private long weekCompletedTasks;
    @Schema(description = "近7天有更新的备忘条数")
    private long weekUpdatedMemos;
    @Schema(description = "当前待处理提醒条数（remind_done=0 且已设 remind_at）")
    private long pendingReminders;
    @Schema(description = "下一条待处理提醒的标题，可空")
    private String upcomingReminderTitle;
    @Schema(description = "下一条待处理提醒时间，可空")
    private String upcomingReminderAt;
    @Schema(description = "置顶备忘标题（最多5条）")
    private List<String> pinnedTitles = new ArrayList<>();
    @Schema(description = "最近更新的备忘标题（最多10条）")
    private List<String> recentMemoTitles = new ArrayList<>();
}
