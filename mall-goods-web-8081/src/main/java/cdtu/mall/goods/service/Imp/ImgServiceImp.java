package cdtu.mall.goods.service.Imp;

import cdtu.mall.goods.dao.ImgDao;
import cdtu.mall.goods.service.ImgService;
import cdtu.mall.item.pojo.Images;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImgServiceImp implements ImgService {

    @Autowired
    private ImgDao imgDao;
    @Override
    public List<Images> findAllBySkuId(String skuId) {

        List<Images> images = imgDao.findAllBySkuId(skuId);
        return images;
    }
}
