package com.tju.unify.conv.news.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.news.pojo.SchoolNews;
import com.tju.unify.conv.news.pojo.dto.SchoolNewsPageVO;
import com.tju.unify.conv.news.service.SchoolNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/unify-api/news/schoolNews")
public class SchoolNewsController {

    @Autowired
    private SchoolNewsService schoolNewsService;

    @RequestMapping("/getByFlag")
    public HttpResult<SchoolNewsPageVO> getByFlag(
            @RequestParam("flag") Long flag,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "8") int size) {
        return HttpResult.success(schoolNewsService.getByFlag(flag, page, size));
    }

    @RequestMapping("/detail")
    public HttpResult<SchoolNews> getDetail(@RequestParam("id")String id) {
        return HttpResult.success(schoolNewsService.getById(id));
    }

    /** 异步拉取天津大学新闻网最新内容入库，供前端「刷新」调用 */
    @PostMapping("/crawler")
    public HttpResult<String> triggerCrawler() {
        schoolNewsService.triggerCrawler();
        return HttpResult.success("爬虫任务已触发");
    }
}
