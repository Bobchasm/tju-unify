package com.tju.unify.conv.news;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
@MapperScan("com.tju.unify.conv.news.mapper")
public class ConvNewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvNewsApplication.class,args);
    }
}
