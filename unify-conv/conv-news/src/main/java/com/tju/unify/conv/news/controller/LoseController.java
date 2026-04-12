package com.tju.unify.conv.news.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.news.pojo.Lose;
import com.tju.unify.conv.news.service.LoseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/news/lose")
public class LoseController {

    @Autowired
    private LoseService loseService;

    @PostMapping("/issue")
    public HttpResult<Boolean> issue(@RequestBody Lose lose) {
        return HttpResult.success(loseService.issue(lose));
    }

    @GetMapping("/get")
    public HttpResult<List<Lose>> getLoses() {
        return HttpResult.success(loseService.getLoses());
    }

    @GetMapping("/getOne")
    public HttpResult<Lose> getOne(@RequestParam("id") String id) {
        return HttpResult.success(loseService.getOne(id));
    }
}
