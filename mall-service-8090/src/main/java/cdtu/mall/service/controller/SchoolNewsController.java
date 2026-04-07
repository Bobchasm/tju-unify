package cdtu.mall.service.controller;

import cdtu.mall.service.pojo.SchoolNews;
import cdtu.mall.service.service.SchoolNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/schoolNews")
public class SchoolNewsController {

    @Autowired
    private SchoolNewsService schoolNewsService;

    @RequestMapping("/getByFlag")
    public List<SchoolNews> getByFlag(@RequestParam("flag") Long flag,@RequestParam("page") int page)
    {
        List<SchoolNews> newsList = schoolNewsService.getByFlag(flag,page);
        return newsList;
    }

    @RequestMapping("/detail")
    public SchoolNews getDetail(@RequestParam("id")String id)
    {
        SchoolNews news = schoolNewsService.getById(id);
        return news;
    }
}
