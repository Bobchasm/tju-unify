package com.tju.elm.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.naming.java.javaURLContextFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import java.util.TimeZone;

@Slf4j
@SpringBootApplication(
        scanBasePackages = {
                "com.tju.elm.user",
                "filters",
                "com.tju.elm.api.config",
                "result",
                "handler"
        },
        exclude = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@Import({config.CacheConfig.class,config.CommonRedisConfig.class,config.DataSourceConfig.class})
//@Import({config.CacheConfig.class,config.DataSourceConfig.class,config.CommonRedisConfig.class,config.JaegerConfig.class})
@MapperScan(
        basePackages = {"com.tju.elm.user.mapper"},
        annotationClass = org.apache.ibatis.annotations.Mapper.class
)
@EnableCaching
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.tju.elm.api.client")
public class UserServiceApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
