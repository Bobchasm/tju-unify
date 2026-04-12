package com.tju.unify.conv.news.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.news.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/unify-api/news/unit")
public class UnitController {

    @Autowired
    private UnitService unitService;

    @PostMapping("/unit")
    public HttpResult<String> utterance(@RequestParam String query, @RequestParam String botId, @RequestParam String userId) {
        return HttpResult.success(unitService.utterance(query,botId,userId));
    }
}
