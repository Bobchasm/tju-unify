package cdtu.mall.goods.service.Imp;

import cdtu.mall.goods.dao.CategoryDao;
import cdtu.mall.goods.dao.SkusDao;
import cdtu.mall.goods.service.SkusService;
import cdtu.mall.item.pojo.Category;
import cdtu.mall.item.pojo.Sku;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkusServiceImp implements SkusService {
    @Autowired
    private SkusDao skusDao;
    @Autowired
    private CategoryDao categoryDao;

    @Override
    public List<Sku> findAll(int pageNum,int pageSize,String key) {
        //这个是根据skuName查询的相关商品集
        List<Sku> skus = skusDao.findAll(pageNum, pageSize, key);
        return skus;
    }

    @Override
    public Sku findBySkuId(String id) {
        Sku sku = skusDao.findBySkuId(id);
        return sku;
    }


}
