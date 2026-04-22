package com.tju.unify.conv.memo.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.memo.pojo.entity.MemoTask;
import com.tju.unify.conv.memo.service.MemoTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/memo/task")
@Tag(name = "备忘录子任务")
public class MemoTaskController {

    @Autowired
    private MemoTaskService memoTaskService;

    @GetMapping("/list")
    @Operation(summary = "某备忘录下的子任务列表")
    public HttpResult<List<MemoTask>> list(@RequestParam Long memoId) {
        return HttpResult.success(memoTaskService.listByMemoId(memoId));
    }

    @PostMapping("/add")
    @Operation(summary = "新增子任务")
    public HttpResult<Long> add(@RequestBody MemoTask task) {
        return HttpResult.success(memoTaskService.add(task));
    }

    @PutMapping("/update")
    @Operation(summary = "更新子任务")
    public HttpResult<Integer> update(@RequestBody MemoTask task) {
        return HttpResult.success(memoTaskService.update(task));
    }

    @PutMapping("/{id}/done")
    @Operation(summary = "勾选/取消完成")
    public HttpResult<Integer> done(@PathVariable Long id, @RequestParam boolean done) {
        return HttpResult.success(memoTaskService.setDone(id, done));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除子任务")
    public HttpResult<Integer> delete(@PathVariable Long id) {
        return HttpResult.success(memoTaskService.delete(id));
    }
}
