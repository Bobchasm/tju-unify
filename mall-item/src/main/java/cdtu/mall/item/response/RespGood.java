package cdtu.mall.item.response;

import cdtu.mall.item.pojo.Images;
import cdtu.mall.item.pojo.Shop;
import cdtu.mall.item.pojo.Sku;
import lombok.Data;

import java.util.List;

@Data
public class RespGood {


    private Sku sku;//商品基本信息
    private List<Images> imgs;//商品图片
    private Shop shop;// 商铺基本信息
}
