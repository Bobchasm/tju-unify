package cdtu.mall.faceRecg.FeignClient;

import cdtu.mall.item.api.AuthApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("mall-auth")
public interface AuthClient extends AuthApi {
}
