package com.tju.elm.user.controller;


import com.tju.elm.user.service.PermissionApplicationService;
import com.tju.elm.user.zoo.pojo.dto.AuditPermissionDTO;
import com.tju.elm.user.zoo.pojo.entity.PermissionApplication;
import com.tju.elm.user.zoo.pojo.vo.MerchantApplicationsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import result.HttpResult;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
@Tag(name = "权限申请管理", description = "顾客申请成为商家与申请开店")
public class PermissionApplicationController {
    @Autowired
    private PermissionApplicationService permissionApplicationService;
    /**
     * 顾客申请成为商家
     */
    @PostMapping("/apply-merchant")
    @Operation(summary = "申请成为商家", description = "顾客提交成为商家的申请，提交后会通知管理员审核")
    public HttpResult<PermissionApplication> applyMerchant() {
        return HttpResult.success(permissionApplicationService.applyMerchant());
    }

    /**
     * 审核用户的商家申请（同意/拒绝）
     */
    @PostMapping("/audit")
    @Operation(summary = "审核商家申请", description = "管理员审核用户的商家申请，同意后将添加BUSINESS权限并通知用户")
    @PreAuthorize("hasAuthority('ADMIN')")
    public HttpResult<PermissionApplication> auditApplication(@Valid @RequestBody AuditPermissionDTO auditDTO) {
        return HttpResult.success(permissionApplicationService.auditApplication(auditDTO));
    }



    @GetMapping("/merchant-applications")
    @Operation(summary = "获取申请成为商家的待审核列表", description = "获取申请成为商家的待审核列表")
    @PreAuthorize("hasAuthority('ADMIN')")
    public HttpResult<List<MerchantApplicationsVO>> getMerchantApplications() {
        return HttpResult.success(permissionApplicationService.getMerchantApplications());
    }


}
