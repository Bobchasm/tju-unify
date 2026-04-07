package cdtu.mall.item.api;

import cdtu.mall.item.pojo.Shop;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface ShopApi {

    @GetMapping("/getShop")
    public Shop getShop(@RequestParam("skuId") String skuId);
}
