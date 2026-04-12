package com.tju.unify.conv.api.config;

import com.tju.unify.conv.common.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j
@Configuration
public class DefaultFeignConfig {

    @Bean
    public RequestInterceptor usernameRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {

                String currentPath = getCurrentRequestPath();
                if (shouldExcludeCurrentRequest(currentPath)) {
                    return;
                }

                String targetUrl = requestTemplate.url();
                if (isRegistrationCall(targetUrl, currentPath)) {
                    return;
                }

                String username = UserContext.getUsername();
                if (username != null && !username.isEmpty()) {
                    requestTemplate.header("username", username);
                }
            }

            private String getCurrentRequestPath() {
                try {
                    ServletRequestAttributes attributes =
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        return request.getRequestURI();
                    }
                } catch (Exception e) {
                }
                return null;
            }

            private boolean shouldExcludeCurrentRequest(String path) {
                if (path == null)
                    return false;

                // 与 UsernameFilter 保持一致
                String[] excludePaths = {
                        "/api/auth",
                        "/api/register",
                        "/**/v3/api-docs/**",
                        "/**/swagger-ui/**",
                        "/**/swagger-ui.html",
                        "/api/upload",
                        "/ws/**",
                        "/api/ai/chat/health",
                        "/api/businesses/search",
                        "/api/businesses/carousel",
                        "/api/admin/countUser",
                        "/api/points/account/create"
                };

                for (String excludePath : excludePaths) {
                    if (path.startsWith(excludePath)) {
                        return true;
                    }
                }
                return false;
            }

            private boolean isRegistrationCall(String targetUrl, String currentPath) {
                // 如果是注册请求调用积分账户创建
                if (currentPath != null && currentPath.contains("/register") &&
                        targetUrl != null && targetUrl.contains("/account/create")) {
                    return true;
                }
                return false;
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

}