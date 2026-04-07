package cdtu.mall.item.api;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.item.pojo.Sku;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface SkusApi {

    //分页查询商品
    @GetMapping("getSkus")
    public List<Sku> findAll(@RequestParam(value ="pageNum",defaultValue ="1") int pageNum,
                        @RequestParam(value ="pageSize",defaultValue ="20" ) int pageSize,
                        @RequestParam(value ="key",defaultValue ="") String key);

    //根据id查询
   @GetMapping("/sku/{skuId}")
    public Sku findBySkuId(@PathVariable("skuId") String skuId);
}
