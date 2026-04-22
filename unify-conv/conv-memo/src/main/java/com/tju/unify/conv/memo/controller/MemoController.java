package com.tju.unify.conv.memo.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.memo.pojo.dto.MemoCreateRequest;
import com.tju.unify.conv.memo.pojo.dto.MemoDetailVO;
import com.tju.unify.conv.memo.pojo.dto.MemoRemindUpdateRequest;
import com.tju.unify.conv.memo.pojo.entity.Memo;
import com.tju.unify.conv.memo.service.MemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/unify-api/memo")
@Tag(name = "备忘录")
public class MemoController {

    @Autowired
    private MemoService memoService;

    @GetMapping("/list")
    @Operation(summary = "备忘录列表", description = "置顶优先，其次 sort_order、更新时间")
    public HttpResult<List<Memo>> list(@RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) Integer pinned) {
        return HttpResult.success(memoService.list(categoryId, pinned));
    }

    @GetMapping("/{id}")
    @Operation(summary = "备忘录详情（含子任务）")
    public HttpResult<MemoDetailVO> detail(@PathVariable Long id) {
        return HttpResult.success(memoService.detail(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增备忘录", description = "可同时传入子任务列表")
    public HttpResult<Long> add(@RequestBody MemoCreateRequest request) {
        return HttpResult.success(memoService.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "更新备忘录", description = "分类置空：传 categoryId = -1")
    public HttpResult<Integer> update(@RequestBody Memo memo) {
        return HttpResult.success(memoService.update(memo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除备忘录（含子任务，逻辑删除）")
    public HttpResult<Integer> delete(@PathVariable Long id) {
        return HttpResult.success(memoService.delete(id));
    }

    @PutMapping("/{id}/pinned")
    @Operation(summary = "置顶/取消置顶")
    public HttpResult<Integer> pinned(@PathVariable Long id, @RequestParam boolean pinned) {
        return HttpResult.success(memoService.setPinned(id, pinned));
    }

    @PutMapping("/{id}/remind")
    @Operation(summary = "设置或清除提醒", description = "clearRemind=true 时清除提醒时间")
    public HttpResult<Integer> remind(@PathVariable Long id, @RequestBody MemoRemindUpdateRequest body) {
        return HttpResult.success(memoService.updateRemind(id, body));
    }

    @GetMapping("/reminders/due")
    @Operation(summary = "待提醒列表", description = "默认时间窗：过去7天至未来1天，未处理且已设置 remind_at")
    public HttpResult<List<Memo>> dueReminders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime until) {
        return HttpResult.success(memoService.listDueReminders(from, until));
    }
}
