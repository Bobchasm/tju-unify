package cdtu.mall.recommend.FeignClient;

import cdtu.mall.item.api.ImgApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("mall-goods-web")
public interface ImgClient extends ImgApi {
}
