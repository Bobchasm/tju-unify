package cdtu.mall.service.Task;

import cdtu.mall.service.pojo.SchoolNews;
import cdtu.mall.service.service.SchoolNewsService;
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
        //通过resultItems获取爬取的信息
        SchoolNews schoolNews=resultItems.get("snewsInfo");
        if (schoolNews != null) {
            newsService.save(schoolNews);
        }
    }
}
