package com.tju.unify.conv.transaction;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.tju.unify.conv.transaction.mapper")
@EnableFeignClients(basePackages = "com.tju.unify.conv.api")
public class ConvTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvTransactionApplication.class);
    }
}
