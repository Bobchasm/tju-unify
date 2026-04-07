package cdtu.mall.goods.service.Imp;


import cdtu.mall.goods.dao.ShopDao;
import cdtu.mall.goods.service.ShopService;
import cdtu.mall.item.pojo.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopServiceImp implements ShopService {

    @Autowired
    private ShopDao shopDao;
    @Override
    public List<Shop> findOneBySkuId(String skuId) {
        List<Shop> shops = shopDao.findOneBySkuId(skuId);
        return shops;
    }
}
