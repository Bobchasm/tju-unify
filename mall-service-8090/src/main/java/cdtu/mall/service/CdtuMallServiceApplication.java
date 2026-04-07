package cdtu.mall.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
@MapperScan("cdtu.mall.service.mapper")
public class CdtuMallServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdtuMallServiceApplication.class,args);
    }
}
