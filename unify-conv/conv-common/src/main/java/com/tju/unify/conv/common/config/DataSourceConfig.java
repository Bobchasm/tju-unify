package com.tju.unify.conv.common.config;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(DynamicDataSourceAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.datasource.dynamic", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(DynamicDataSourceAutoConfiguration.class)
public class DataSourceConfig {

    @Bean
    public ReadWriteSplitInterceptor readWriteSplitInterceptor() {
        return new ReadWriteSplitInterceptor();
    }
}
