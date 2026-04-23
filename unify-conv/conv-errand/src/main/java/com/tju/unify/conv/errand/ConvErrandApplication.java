package com.tju.unify.conv.errand;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.tju.unify.conv.errand", "com.tju.unify.conv.common", "com.tju.unify.conv.api"})
@EnableDiscoveryClient
@MapperScan("com.tju.unify.conv.errand.mapper")
@EnableFeignClients(basePackages = "com.tju.unify.conv.api.client")
public class ConvErrandApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvErrandApplication.class, args);
    }
}
