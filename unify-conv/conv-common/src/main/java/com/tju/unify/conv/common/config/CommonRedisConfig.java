package com.tju.unify.conv.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 通用 Redis 配置（供其他服务使用）
 * 与积分服务的 config.redis.RedisConfig 共存，互不干扰
 */
@Configuration
@Slf4j
public class CommonRedisConfig {

    @Bean(name = "commonRedisTemplate")
    @ConditionalOnMissingBean(name = "redisTemplate")  // 避免与积分服务的配置冲突
    public RedisTemplate<String, Object> commonRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {

        log.info("初始化通用 RedisTemplate（用于非积分服务）");

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // key 序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // value 序列化配置
        ObjectMapper om = new ObjectMapper();

        // 注册JavaTime模块处理日期时间
        om.registerModule(new JavaTimeModule());

        // 配置忽略未知属性
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 配置允许反序列化任何类型
        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 配置避免SubList等内部类的问题
        om.addMixIn(java.util.List.class, ListMixIn.class);
        om.addMixIn(java.util.ArrayList.class, ListMixIn.class);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(om);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    // 添加MixIn接口处理List类型
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    abstract static class ListMixIn {}
}