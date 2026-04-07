package cdtu.mall.item.api;

import cdtu.mall.item.pojo.Images;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ImgApi {

    @GetMapping("/getImgs")
    public List<Images> getImgs(@RequestParam("skuId") String skuId);
}
