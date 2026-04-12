package com.tju.unify.conv.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";

    @Getter
    private final Set<String> routeIds = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void initRouteConfigListener() {
        log.info("========== 初始化动态路由监听器 ==========");

        try {
            String configInfo = nacosConfigManager.getConfigService()
                    .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                        @Override
                        public Executor getExecutor() {
                            return null;
                        }

                        @Override
                        public void receiveConfigInfo(String configInfo) {
                            log.info("监听到路由配置变更");
                            updateRoutes(configInfo);
                        }
                    });

            if (configInfo != null && !configInfo.isEmpty()) {
                updateRoutes(configInfo);
            }
        } catch (NacosException e) {
            log.error("初始化路由监听器失败", e);
        }
    }

    public void updateRoutes(String configInfo) {
        if (configInfo == null || configInfo.isEmpty()) {
            log.warn("路由配置为空");
            return;
        }

        log.info("开始更新路由配置");

        // 在新线程中执行,避免阻塞Nacos监听器
        new Thread(() -> {
            try {
                List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);

                if (routeDefinitions == null || routeDefinitions.isEmpty()) {
                    log.warn("解析路由配置为空");
                    return;
                }

                log.info("解析到 {} 条路由", routeDefinitions.size());

                // 删除旧路由
                for (String routeId : routeIds) {
                    try {
                        routeDefinitionWriter.delete(Mono.just(routeId)).block();
                    } catch (Exception e) {
                        log.error("删除路由失败: {}", routeId, e);
                    }
                }
                routeIds.clear();

                // 添加新路由
                for (RouteDefinition definition : routeDefinitions) {
                    try {
                        routeDefinitionWriter.save(Mono.just(definition)).block();
                        routeIds.add(definition.getId());
                        log.info("添加路由成功: {}", definition.getId());
                    } catch (Exception e) {
                        log.error("添加路由失败: {}", definition.getId(), e);
                    }
                }

                // 发布刷新事件
                applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
                log.info("路由更新完成，共 {} 条路由", routeIds.size());

            } catch (Exception e) {
                log.error("路由更新失败", e);
            }
        }, "route-update-thread").start();
    }

    @PreDestroy
    public void destroy() {
        log.info("清理路由");
        for (String routeId : routeIds) {
            try {
                routeDefinitionWriter.delete(Mono.just(routeId)).block();
            } catch (Exception e) {
                log.error("清理路由失败: {}", routeId, e);
            }
        }
        routeIds.clear();
    }
}