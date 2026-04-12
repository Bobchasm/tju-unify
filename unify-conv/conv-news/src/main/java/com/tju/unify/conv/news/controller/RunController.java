package com.tju.unify.conv.news.controller;


import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.news.pojo.Run;
import com.tju.unify.conv.news.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/news/run")
public class RunController {

    @Autowired
    private RunService runService;

    @PostMapping("/issue")
    public HttpResult<Boolean> issue(@RequestBody Run run) {
        return HttpResult.success(runService.issue(run));
    }

    @GetMapping("/get")
    public HttpResult<List<Run>> getRuns() {
        return HttpResult.success(runService.getRuns());
    }

    @GetMapping("/getOne")
    public HttpResult<Run> getOne(@RequestParam("id") String id) {
        return HttpResult.success(runService.getOne(id));
    }
}
