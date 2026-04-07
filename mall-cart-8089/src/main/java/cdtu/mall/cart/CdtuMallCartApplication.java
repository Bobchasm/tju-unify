package cdtu.mall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * desc:
 * - 用户可以在登录状态下将商品添加到购物车
 *   - 放入数据库
 *   - 放入redis（采用）
 * - 用户可以在未登录状态下将商品添加到购物车
 *   - 放入localstorage
 * - 用户可以使用购物车一起结算下单
 * - 用户可以查询自己的购物车
 * - 用户可以在购物车中可以修改购买商品的数量。
 * - 用户可以在购物车中删除商品。
 * - 在购物车中展示商品优惠信息
 * - 提示购物车商品价格变化
 * - 对商品结算下单
 */


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CdtuMallCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CdtuMallCartApplication.class, args);
    }
}
