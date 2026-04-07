package cdtu.mall.recommend.service.imp;

import cdtu.mall.item.pojo.Shop;
import cdtu.mall.recommend.Dao.ShopDao;
import cdtu.mall.recommend.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShopServiceImp implements ShopService {

    @Autowired
    private ShopDao shopDao;

    public List<Shop> getShop() {
        List<Shop> shops = new ArrayList<>();
        int i=0;
        for (Shop shop:shopDao.getShop())
        {

            if(i>11)
            {
                break;
            }
            i++;
            shops.add(shop);
        }
        return shops;
    }
}
