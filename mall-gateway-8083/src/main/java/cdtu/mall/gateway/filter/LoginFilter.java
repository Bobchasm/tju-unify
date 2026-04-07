package cdtu.mall.gateway.filter;

import cdtu.mall.common.utils.JwtUtils;
import cdtu.mall.gateway.config.FilterProperties;
import cdtu.mall.gateway.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * - 获取cookie中的token
 * - 通过JWT对token进行校验
 * - 通过：则放行；不通过：则重定向到登录页
 */

@Component
public class LoginFilter implements GlobalFilter {

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private FilterProperties filterProp;

    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

    private boolean isAllowPath(String requestURI) {
        // 定义一个标记
        boolean flag = false;
        // 遍历允许访问的路径
        for (String path : this.filterProp.getAllowPaths()) {
            // 然后判断是否是符合
            if(requestURI.contains(path)){
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestURI = exchange.getRequest().getURI().getPath();
        if (isAllowPath(requestURI)) {
            return chain.filter(exchange);
        }

        // 获取token
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(jwtProp.getCookieName());
        String token = cookie != null ? cookie.getValue() : null;
        // 校验
        try {
            // 校验通过什么都不做，即放行
            JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey());
            return chain.filter(exchange);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
            logger.error("非法访问，未登录，地址：{}", exchange.getRequest().getRemoteAddress(), e);
            return exchange.getResponse().setComplete();
        }
    }
}