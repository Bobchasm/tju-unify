package cdtu.mall.recommend.FeignClient;

import cdtu.mall.item.api.SkusApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("mall-goods-web")
public interface SkusClient extends SkusApi {
}
