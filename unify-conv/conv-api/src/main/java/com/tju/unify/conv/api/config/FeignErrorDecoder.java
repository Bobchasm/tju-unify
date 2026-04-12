package com.tju.unify.conv.api.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tju.unify.conv.common.exception.APIException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Feign异常解码器
 * 用于将远程服务的异常信息解析并传递
 */
@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() != null) {
                InputStream inputStream = response.body().asInputStream();
                String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                
                log.error("Feign调用异常: method={}, status={}, body={}", methodKey, response.status(), body);
                
                // 尝试解析为标准的HttpResult格式
                JsonNode jsonNode = objectMapper.readTree(body);
                
                if (jsonNode.has("code") && jsonNode.has("message")) {
                    String code = jsonNode.get("code").asText();
                    String message = jsonNode.get("message").asText();
                    
                    // 抛出APIException，保留原始错误信息
                    return new APIException(code, message);
                }
            }
        } catch (IOException e) {
            log.error("解析Feign异常响应失败: ", e);
        }
        
        // 如果解析失败，使用默认解码器
        return defaultDecoder.decode(methodKey, response);
    }
}
