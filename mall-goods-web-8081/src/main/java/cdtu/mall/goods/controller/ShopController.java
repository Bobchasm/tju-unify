package cdtu.mall.goods.controller;

import cdtu.mall.goods.service.Imp.ShopServiceImp;
import cdtu.mall.item.pojo.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ShopController {
    @Autowired
    private ShopServiceImp shopServiceImp;

    @GetMapping("/getShop")
    public Shop getShop(@RequestParam("skuId") String skuId)
    {
        List<Shop> shops = shopServiceImp.findOneBySkuId(skuId);
        return shops.get(0);
    }
}
