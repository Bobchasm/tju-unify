package cdtu.mall.recommend.controller;

import cdtu.mall.item.pojo.Shop;
import cdtu.mall.recommend.service.imp.ShopServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ShopController {

    @Autowired
    private ShopServiceImp shopServiceImp;

    @RequestMapping("/shop")
    public List<Shop> getShop()
    {
        List<Shop> shopList=shopServiceImp.getShop();
        return shopList;
    }

}
