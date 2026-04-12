package com.tju.elm.user.controller;

import com.tju.elm.user.mapper.SystemConfigMapper;
import com.tju.elm.user.zoo.pojo.entity.SystemConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import result.HttpResult;

@RestController
@RequestMapping("/api")
@Tag(name="其他接口")
public class CommonController {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @GetMapping("/config/key")
    @Operation(description = "获取系统配置")
    public HttpResult<SystemConfig> getSystemConfig(@RequestParam String key) {
        return HttpResult.success(systemConfigMapper.getConfigByKey(key));
    }

}
