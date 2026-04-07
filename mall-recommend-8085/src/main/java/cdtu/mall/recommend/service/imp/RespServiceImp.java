package cdtu.mall.recommend.service.imp;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.item.pojo.Images;
import cdtu.mall.item.pojo.Shop;
import cdtu.mall.item.pojo.Sku;
import cdtu.mall.item.response.RespGood;
import cdtu.mall.recommend.FeignClient.ImgClient;
import cdtu.mall.recommend.FeignClient.ShopClient;
import cdtu.mall.recommend.FeignClient.SkusClient;
import cdtu.mall.recommend.service.RespService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RespServiceImp implements RespService {

    @Autowired
    private ImgClient imgClient;
    @Autowired
    private ShopClient shopClient;
    @Autowired
    private SkusClient skusClient;

    @Override
    public List<RespGood> findAll(int pageNum,int pageSize,String key) {
        List<Sku> skus = skusClient.findAll(pageNum, pageSize, key);
        System.out.println(skus);
        List<RespGood> list=new ArrayList<>();
        for(Sku sku:skus)
        {
            RespGood respGood=new RespGood();
            List<Images> imgs1=new ArrayList<>();
//            Images images1=new Images();
//            images1.setUrl("http://img2.3png.com/f1456e7063510bde6f9e45dcc9fef27b78ff.png");
//            imgs1.add(images1);
            List<Images> imgs2 = imgClient.getImgs(sku.getSkuId());
            for(Images images:imgs2)
            {
                if((images.getIsMaster().equals("-1"))&&imgs1.isEmpty())
                {
                    imgs1.add(images);
                }
                if((images.getIsMaster().equals("1")))
                {
                    imgs1.clear();
                    imgs1.add(images);
                    break;
                }
            }
            Shop shop = shopClient.getShop(sku.getSkuId());
            respGood.setSku(sku);
            respGood.setImgs(imgs1);
            respGood.setShop(shop);
            list.add(respGood);
        }
        return list;
    }
}
