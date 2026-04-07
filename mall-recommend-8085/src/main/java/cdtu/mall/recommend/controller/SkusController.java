package cdtu.mall.recommend.controller;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.item.pojo.Sku;
import cdtu.mall.recommend.service.imp.SkusServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SkusController {

    @Autowired
    private SkusServiceImp skusServiceImp;

    private static int pageSize=20;
    @GetMapping("/getSkus")
    public PageResult getRecommed(@RequestParam("pageNum")int pageNum)
    {
        PageResult result=new PageResult();
        List<Sku> skus = skusServiceImp.findAll(pageNum, pageSize, "");
        result.setItems(skus);
        result.setTotal(skus.size());
        return result;
    }
}
