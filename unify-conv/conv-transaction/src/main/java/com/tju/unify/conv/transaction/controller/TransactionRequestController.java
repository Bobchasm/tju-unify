package com.tju.unify.conv.transaction.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.transaction.pojo.entity.TransactionRequest;
import com.tju.unify.conv.transaction.service.TransactionRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/transaction/request")
@Tag(name="交易请求接口")
public class TransactionRequestController {

    @Autowired
    private TransactionRequestService transactionRequestService;

    @GetMapping("/list")
    @Operation(summary = "根据帖子ID获取所有交易请求")
    public HttpResult<List<TransactionRequest>> getList(@RequestParam Integer postId) {
        return HttpResult.success(transactionRequestService.findByPostId(postId));
    }

    @PostMapping("/add")
    @Operation(summary = "创建交易请求（我想要）")
    public HttpResult<Integer> add(@RequestBody TransactionRequest request) {
        return HttpResult.success(transactionRequestService.insert(request));
    }

    @GetMapping("/updateStatus")
    @Operation(summary = "更新交易请求状态 0-待处理 1-已同意 2-已拒绝 3-已取消")
    public HttpResult<Integer> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        return HttpResult.success(transactionRequestService.updateStatus(id, status));
    }
}
