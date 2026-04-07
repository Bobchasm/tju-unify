package cdtu.mall.goods.controller;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.goods.service.Imp.SkusServiceImp;
import cdtu.mall.item.pojo.Sku;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SkusController {
    @Autowired
    private SkusServiceImp skusServiceImp;

    @GetMapping("getSkus")
    public List<Sku> findAll(@RequestParam(value ="pageNum",defaultValue ="1") int pageNum,
                              @RequestParam(value ="pageSize",defaultValue ="20" ) int pageSize,
                              @RequestParam(value ="key",defaultValue ="") String key)
    {
        List<Sku> skus = skusServiceImp.findAll(pageNum, pageSize, key);
//        PageResult<Sku> pageResult=new PageResult();
//        pageResult.setTotal(skus.size());
//        pageResult.setItems(skus);
//        pageResult.setTotalPage(pageSize/20);
        return skus;
    }

    @GetMapping("/sku/{skuId}")
    public Sku findBySkuId(@PathVariable("skuId") String skuId)
    {
        Sku sku = skusServiceImp.findBySkuId(skuId);
        return sku;
    }


}
