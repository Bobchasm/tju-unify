package com.tju.unify.conv.news.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.news.pojo.SchoolNews;
import com.tju.unify.conv.news.service.SchoolNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/unify-api/news/schoolNews")
public class SchoolNewsController {

    @Autowired
    private SchoolNewsService schoolNewsService;

    @RequestMapping("/getByFlag")
    public HttpResult<List<SchoolNews>> getByFlag(@RequestParam("flag") Long flag, @RequestParam("page") int page) {
        return HttpResult.success(schoolNewsService.getByFlag(flag,page));
    }

    @RequestMapping("/detail")
    public HttpResult<SchoolNews> getDetail(@RequestParam("id")String id) {
        return HttpResult.success(schoolNewsService.getById(id));
    }
}
