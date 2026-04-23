package com.tju.unify.conv.errand.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.errand.pojo.dto.ErrandPublishRequest;
import com.tju.unify.conv.errand.pojo.entity.ErrandOrder;
import com.tju.unify.conv.errand.service.ErrandOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/errand")
@Tag(name = "校园跑腿")
public class ErrandOrderController {

    @Autowired
    private ErrandOrderService errandOrderService;

    @PostMapping("/publish")
    @Operation(summary = "发布跑腿")
    public HttpResult<Long> publish(@RequestBody ErrandPublishRequest request) {
        return HttpResult.success(errandOrderService.publish(request));
    }

    @GetMapping("/open")
    @Operation(summary = "待接单列表")
    public HttpResult<List<ErrandOrder>> openList() {
        return HttpResult.success(errandOrderService.listOpen());
    }

    @GetMapping("/mine")
    @Operation(summary = "我相关：我发布的或我接的")
    public HttpResult<List<ErrandOrder>> mine() {
        return HttpResult.success(errandOrderService.listMine());
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public HttpResult<ErrandOrder> detail(@PathVariable Long id) {
        return HttpResult.success(errandOrderService.getById(id));
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "接单")
    public HttpResult<Void> accept(@PathVariable Long id) {
        errandOrderService.accept(id);
        return HttpResult.success();
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "接单者确认完成")
    public HttpResult<Void> complete(@PathVariable Long id) {
        errandOrderService.complete(id);
        return HttpResult.success();
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "发起人取消（仅待接单）")
    public HttpResult<Void> cancel(@PathVariable Long id) {
        errandOrderService.cancel(id);
        return HttpResult.success();
    }
}
