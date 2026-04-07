package cdtu.mall.recommend.service.imp;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.item.pojo.Sku;
import cdtu.mall.recommend.FeignClient.SkusClient;
import cdtu.mall.recommend.service.SkusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkusServiceImp implements SkusService {

    @Autowired
    private SkusClient skusClient;


    @Override
    public List<Sku> findAll(int pageNum, int pageSize, String key) {
        List<Sku> skus = skusClient.findAll(pageNum, pageSize, key);
        return skus;
    }

    @Override
    public Sku findBySkuId(String id) {
        return null;
    }
}
