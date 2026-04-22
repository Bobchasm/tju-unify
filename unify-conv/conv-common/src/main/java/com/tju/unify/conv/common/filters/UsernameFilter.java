package com.tju.unify.conv.common.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import com.tju.unify.conv.common.utils.UserContext;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UsernameFilter implements Filter {

    private final AntPathMatcher matcher = new AntPathMatcher();

    private static final String[] EXCLUDE_PATHS = {
            "/api/**",
            "/ws/**",
            "/**/v3/api-docs/**",
            "/**/swagger-ui/**",
            "/**/swagger-ui.html",
            "/unify-api/news/schoolNews/**"
    };


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        if (shouldExclude(path)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 从请求头获取用户信息
            String username = httpRequest.getHeader("username");
            String token = httpRequest.getHeader("Authorization");
            // 设置到 ThreadLocal
            if (username != null && !username.isEmpty()) {
                UserContext.setUsername(username);
                log.info("设置用户上下文: username={}", username);
            }
            if (token != null && !token.isEmpty()) {
                UserContext.setToken(token);
                log.info("设置用户上下文: token={}", token);
            }

            // 继续执行过滤器链
            chain.doFilter(request, response);
        } finally {
            // 清理 ThreadLocal 和 MDC，防止内存泄漏
            MDC.clear();
        }
    }

    private boolean shouldExclude(String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }

        for (String excludePath : EXCLUDE_PATHS) {
            if (matcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}