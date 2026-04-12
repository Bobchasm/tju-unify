package com.tju.unify.conv.gateway.filters;

import com.tju.unify.conv.gateway.config.AuthProperties;
import com.tju.unify.conv.gateway.security.TokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    // 电商集群接口前缀
    private static final String[] COMMERCE_CLUSTER_URL_PREFIXES = {"/api/", "/ws/"};

    private final TokenProvider tokenProvider;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String uri = request.getURI().toString();
        String path = request.getPath().toString();
        log.info("网关收到请求 URI: {}, Path: {}", uri, path);

        // 电商集群网关(跳过本网关鉴权)
        if (shouldForwardToBGateway(path)) {
            log.info("检测到电商集群接口路径: {}, 跳过鉴权", path);
            // 直接放行,这里不鉴权,电商集群自己鉴权
            return chain.filter(exchange);
        }

        if (isExclude(path)) {
            log.info("路径 {} 在白名单中,跳过认证", path);
            return chain.filter(exchange);
        }

        String username;
        try {
            String jwt = resolveToken(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                username = authentication.getName();
                log.info("用户 '{}' 认证成功, uri: {}", username, uri);
            } else {
                log.warn("未找到有效的JWT token, uri: {}", uri);
                ServerHttpResponse serverHttpResponse = exchange.getResponse();
                serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                return serverHttpResponse.setComplete();
            }
        } catch (Exception e) {
            log.error("认证失败 uri: {}, 错误: {}", uri, e.getMessage(), e);
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
            return serverHttpResponse.setComplete();
        }

        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("username", username))
                .build();

        return chain.filter(swe);
    }

    /**
     * 判断是否直接转发给电商集群
     */
    private boolean shouldForwardToBGateway(String path) {
        for (String prefix : COMMERCE_CLUSTER_URL_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
    
    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
