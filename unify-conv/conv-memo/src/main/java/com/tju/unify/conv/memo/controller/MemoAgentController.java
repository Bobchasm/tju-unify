package com.tju.unify.conv.memo.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.memo.pojo.dto.MemoAgentSnapshotVO;
import com.tju.unify.conv.memo.service.MemoAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 与小智联动的备忘录上下文（路径固定 /agent/ 避免与 /{id} 冲突）。
 */
@RestController
@RequestMapping("/unify-api/memo/agent")
@Tag(name = "备忘录-小智联动")
public class MemoAgentController {

    @Autowired
    private MemoAgentService memoAgentService;

    @GetMapping("/snapshot")
    @Operation(summary = "聚合快照", description = "近一周完成情况、待提醒、置顶与最近备忘，供开场白与自然语言记备忘使用")
    public HttpResult<MemoAgentSnapshotVO> snapshot() {
        return HttpResult.success(memoAgentService.buildSnapshot());
    }
}
