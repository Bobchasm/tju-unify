package cdtu.mall.second.controller;

import cdtu.mall.second.pojo.SecGoods;
import cdtu.mall.second.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @RequestMapping("/sec")
    public List<SecGoods> findAll(@RequestParam("category")int category,
                                  @RequestParam("flag")int flag,
                                  @RequestParam("page") int page)
    {
        return  goodsService.findAll(category,flag,page);
    }

    @GetMapping("/click")
    public void click(@RequestParam("id") String id)
    {
        goodsService.click(id);
        return;
    }
    @GetMapping("/getOne")
    public SecGoods getOne(@RequestParam("id") String id)
    {
        return goodsService.getOne(id);
    }
}
