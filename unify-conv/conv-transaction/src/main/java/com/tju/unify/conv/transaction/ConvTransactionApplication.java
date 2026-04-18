package com.tju.unify.conv.transaction;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(
        exclude = {
                ElasticsearchClientAutoConfiguration.class,
                ElasticsearchRestClientAutoConfiguration.class,
                ElasticsearchDataAutoConfiguration.class,
                ElasticsearchRepositoriesAutoConfiguration.class
        }
)
@ComponentScan(basePackages = {"com.tju.unify.conv.transaction", "com.tju.unify.conv.common", "com.tju.unify.conv.api"})
@EnableDiscoveryClient
@MapperScan("com.tju.unify.conv.transaction.mapper")
@EnableFeignClients(basePackages = "com.tju.unify.conv.api.client")
public class ConvTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvTransactionApplication.class);
    }
}
