package com.tju.unify.conv.api.client.outer;

import com.tju.unify.conv.api.po.Person;
import com.tju.unify.conv.api.po.User;
import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


/**
 * 电商集群 网关 HTTP 客户端
 */
@Slf4j
@Component
public class ElmUserClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public ElmUserClient(@Value("${elm.gateway.url:http://bobchasm.cn:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public User getUserByName(String username) {
        String url = baseUrl + "/api/user/current" + "?username=" + username;

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", UserContext.getToken());

            HttpEntity<?> entity = new HttpEntity<>(headers);

            // 发送请求
            ResponseEntity<HttpResult<User>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<HttpResult<User>>() {}
            );

            // 处理响应
            HttpResult<User> result = response.getBody();

            if (result == null) {
                log.error("用户信息响应为空, username: {}", username);
                return null;
            }

            if (!result.getSuccess()) {
                log.error("用户信息返回失败, username: {}, code: {}, message: {}",
                        username, result.getCode(), result.getMessage());
                return null;
            }

            return result.getData();

        } catch (RestClientException e) {
            log.error("调用获取用户信息失败, username: {}", username, e);
            return null;
        }
    }

    public Person getPersonById(Long id) {
        String url = baseUrl + "/api/personInfo" + "?id=" + id;

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", UserContext.getToken());

            HttpEntity<?> entity = new HttpEntity<>(headers);

            // 发送请求
            ResponseEntity<HttpResult<Person>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<HttpResult<Person>>() {}
            );

            // 处理响应
            HttpResult<Person> result = response.getBody();

            if (result == null) {
                log.error("Person信息响应为空, id: {}", id);
                return null;
            }

            if (!result.getSuccess()) {
                log.error("Person信息返回失败, id: {}, code: {}, message: {}",
                        id, result.getCode(), result.getMessage());
                return null;
            }

            return result.getData();

        } catch (RestClientException e) {
            log.error("调用获取Person信息失败, id: {}", id, e);
            return null;
        }
    }
}