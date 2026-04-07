package cdtu.mall.recommend.FeignClient;

import cdtu.mall.item.api.ShopApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("mall-goods-web")
public interface ShopClient extends ShopApi {
}
