package com.tju.unify.conv.memo.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.memo.pojo.entity.MemoCategory;
import com.tju.unify.conv.memo.service.MemoCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/memo/category")
@Tag(name = "备忘录分类")
public class MemoCategoryController {

    @Autowired
    private MemoCategoryService memoCategoryService;

    @GetMapping("/list")
    @Operation(summary = "分类列表")
    public HttpResult<List<MemoCategory>> list() {
        return HttpResult.success(memoCategoryService.list());
    }

    @PostMapping("/add")
    @Operation(summary = "新增分类")
    public HttpResult<Long> add(@RequestBody MemoCategory category) {
        return HttpResult.success(memoCategoryService.add(category));
    }

    @PutMapping("/update")
    @Operation(summary = "更新分类")
    public HttpResult<Integer> update(@RequestBody MemoCategory category) {
        return HttpResult.success(memoCategoryService.update(category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类（备忘录该分类字段会置空）")
    public HttpResult<Integer> delete(@PathVariable Long id) {
        return HttpResult.success(memoCategoryService.delete(id));
    }
}
