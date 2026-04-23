package com.tju.unify.conv.news.Task;

import com.tju.unify.conv.news.pojo.SchoolNews;
import com.tju.unify.conv.news.service.SchoolNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

@Component
public class MyPipeLine implements Pipeline {

    @Autowired
    private SchoolNewsService newsService;

    @Override
    public void process(ResultItems resultItems, Task task) {

        SchoolNews schoolNews = resultItems.get("snewsInfo");
        if (schoolNews != null) {
            newsService.saveIfNotExists(schoolNews);
        }
    }
}
