package com.tju.unify.conv.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.lettuce.core.dynamic.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * Caffeine本地缓存配置
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(256)
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)  // 本地5分钟
                .recordStats());
        log.info("Caffeine本地缓存配置：5分钟过期，最大1000条");
        return cacheManager;
    }


    /**
     * Redis缓存配置
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.activateDefaultTyping(
                om.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()
                ))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(om)
                ))
                .entryTtl(Duration.ofMinutes(10))       // Redis 10分钟
                .computePrefixWith(cacheName -> "elm:" + cacheName + ":")  // key前缀
                .disableCachingNullValues();            // 不缓存null

        log.info("Redis缓存配置：10分钟过期，key前缀格式：elm:{cacheName}:");
        return config;
    }

    /**
     * Redis缓存管理器
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(
            RedisConnectionFactory redisConnectionFactory,
            RedisCacheConfiguration redisCacheConfiguration) {

        RedisCacheManager manager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .transactionAware()
                .build();

        log.info("Redis缓存管理器初始化完成");
        return manager;
    }


    /**
     * 主缓存管理器（两级缓存）
     */
    @Bean
    @Primary
    public CacheManager cacheManager(
            @org.springframework.beans.factory.annotation.Qualifier("caffeineCacheManager")
            CacheManager caffeineCacheManager,
            @org.springframework.beans.factory.annotation.Qualifier("redisCacheManager")
            CacheManager redisCacheManager) {

        log.info("初始化两层缓存管理器：Caffeine(5分钟) + Redis(10分钟)");
        return new TwoLevelCacheManager(
                (CaffeineCacheManager) caffeineCacheManager,
                (RedisCacheManager) redisCacheManager
        );
    }

    public static class TwoLevelCacheManager implements CacheManager {
        private final CaffeineCacheManager caffeineCacheManager;
        private final RedisCacheManager redisCacheManager;

        public TwoLevelCacheManager(CaffeineCacheManager caffeineCacheManager,
                                    RedisCacheManager redisCacheManager) {
            this.caffeineCacheManager = caffeineCacheManager;
            this.redisCacheManager = redisCacheManager;
        }

        @Override
        public Cache getCache(String name) {
            Cache caffeineCache = caffeineCacheManager.getCache(name);
            Cache redisCache = redisCacheManager.getCache(name);
            return new TwoLevelCache(name, caffeineCache, redisCache);
        }

        @Override
        public Collection<String> getCacheNames() {
            Set<String> names = new LinkedHashSet<>();
            names.addAll(caffeineCacheManager.getCacheNames());
            names.addAll(redisCacheManager.getCacheNames());
            return names;
        }

        // 两级缓存实现类
        static class TwoLevelCache implements Cache {
            private final String name;
            private final Cache caffeineCache;
            private final Cache redisCache;
            private static final ConcurrentMap<Integer, ReentrantLock> KEY_LOCKS =
                    new ConcurrentHashMap<>();

            private ReentrantLock getLock(Object key) {
                int bucket = Math.abs(key.hashCode()) % 32;
                return KEY_LOCKS.computeIfAbsent(bucket, k -> new ReentrantLock());
            }

            public TwoLevelCache(String name, Cache caffeineCache, Cache redisCache) {
                this.name = name;
                this.caffeineCache = caffeineCache;
                this.redisCache = redisCache;
            }

            @Override
            public String getName() { return name; }

            @Override
            public Object getNativeCache() { return this; }

            @Override
            public ValueWrapper get(Object key) {
                // 先查本地
                ValueWrapper value = caffeineCache.get(key);
                if (value != null) return value;

                // 再查Redis
                value = redisCache.get(key);
                if (value != null) {
                    caffeineCache.put(key, value.get());  // 回填
                }
                return value;
            }

            @Override
            public <T> T get(Object key, Class<T> type) {
                T value = caffeineCache.get(key, type);
                if (value != null) return value;

                value = redisCache.get(key, type);
                if (value != null) {
                    caffeineCache.put(key, value);  // 回填
                }
                return value;
            }

            @Override
            public <T> T get(Object key, Callable<T> valueLoader) {
                // 无锁查询
                ValueWrapper cachedValue = caffeineCache.get(key);
                if (cachedValue != null) return (T) cachedValue.get();

                cachedValue = redisCache.get(key);
                if (cachedValue != null) {
                    T value = (T) cachedValue.get();
                    caffeineCache.put(key, value);
                    return value;
                }

                // 加锁加载
                ReentrantLock lock = getLock(key);
                try {
                    if (lock.tryLock(1, TimeUnit.SECONDS)) {
                        try {
                            // 双重检查
                            cachedValue = caffeineCache.get(key);
                            if (cachedValue != null) return (T) cachedValue.get();

                            cachedValue = redisCache.get(key);
                            if (cachedValue != null) {
                                T value = (T) cachedValue.get();
                                caffeineCache.put(key, value);
                                return value;
                            }

                            // 执行加载
                            T value = valueLoader.call();
                            put(key, value);
                            return value;
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        log.warn("缓存加载锁超时，key: {}", key);
                        return null;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("缓存加载失败: " + key, e);
                }
            }

            @Override
            public void put(Object key, Object value) {
                caffeineCache.put(key, value);
                redisCache.put(key, value);
            }

            @Override
            public void evict(Object key) {
                caffeineCache.evict(key);
                redisCache.evict(key);
            }

            @Override
            public void clear() {
                caffeineCache.clear();
                redisCache.clear();
            }
        }
    }
}