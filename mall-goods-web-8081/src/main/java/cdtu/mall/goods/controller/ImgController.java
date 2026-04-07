package cdtu.mall.goods.controller;

import cdtu.mall.goods.service.Imp.ImgServiceImp;
import cdtu.mall.item.pojo.Images;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ImgController {

    @Autowired
    private ImgServiceImp imgServiceImp;

    @GetMapping("/getImgs")
    public List<Images> getImgs(@RequestParam("skuId") String skuId)
    {
        List<Images> imagesList = imgServiceImp.findAllBySkuId(skuId);
        return imagesList;
    }
}
