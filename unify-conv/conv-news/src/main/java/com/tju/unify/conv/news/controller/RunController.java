package com.tju.unify.conv.news.controller;


import com.tju.unify.conv.news.pojo.Run;
import com.tju.unify.conv.news.service.RunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/run")
public class RunController {

    @Autowired
    private RunService runService;

    @PostMapping("/issue")
    public void issue(@RequestBody Run run)
    {
        runService.issue(run);
    }

    @GetMapping("get")
    public List<Run> getRuns()
    {
        return runService.getRuns();
    }

    @GetMapping("getOne")
    public Run getOne(@RequestParam("id") String id)
    {
        return runService.getOne(id);
    }
}
