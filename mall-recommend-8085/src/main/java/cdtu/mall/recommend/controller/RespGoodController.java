package cdtu.mall.recommend.controller;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.item.response.RespGood;
import cdtu.mall.recommend.pojo.Goods;
import cdtu.mall.recommend.service.imp.RecommendServiceImp;
import cdtu.mall.recommend.service.imp.RespServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RespGoodController {

    @Autowired
    private RespServiceImp respServiceImp;

    @Autowired
    private RecommendServiceImp recommendServiceImp;

    @GetMapping("/recommendList")
    public PageResult findAll(@RequestParam("pageNum")int pageNum)
    {
        PageResult<Goods> pageResult = recommendServiceImp.searchAll(pageNum);
//        System.out.println(pageNum);
//        List<RespGood> all = respServiceImp.findAll(pageNum, pageSize, "");
//        System.out.println(all);
//        PageResult<RespGood> pageResult=new PageResult<>();
//        pageResult.setItems(all);
//        pageResult.setTotal(all.size());
//        pageResult.setTotalPage(1);
//        return pageResult;
        return pageResult;
    }}
