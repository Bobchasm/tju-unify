package cdtu.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("cdtu.mall.user.mapper")
public class CdtuMallUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdtuMallUserApplication.class, args);
    }
}
