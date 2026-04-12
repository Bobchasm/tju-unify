package com.tju.unify.conv.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("cdtu.mall.second.mapper")
public class ConvTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvTransactionApplication.class);
    }
}
