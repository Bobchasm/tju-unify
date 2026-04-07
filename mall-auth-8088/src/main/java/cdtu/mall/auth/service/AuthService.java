package cdtu.mall.auth.service;

import cdtu.mall.auth.FeignClient.UserClient;
import cdtu.mall.auth.config.JwtProperties;
import cdtu.mall.common.entity.UserInfo;
import cdtu.mall.common.utils.JwtUtils;
import cdtu.mall.user.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
//@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private UserClient userClient;

    private static Logger logger = LoggerFactory.getLogger(AuthService.class);

    public String authentication(String username, String password) {
        try {
            // 查询用户
            User user = this.userClient.queryUser(username, password);
            System.out.println(user.toString());
            if (user==null) {
                logger.info("用户信息不存在，{}", username);
                return null;
            }
//            // 获取登录用户
//            User user = resp.getBody();
            // 生成token
            String token = JwtUtils.generateToken(
                    new UserInfo(user.getUserId(), user.getUsername()),
                    jwtProp.getPrivateKey(), jwtProp.getExpire());
            return token;
        } catch (Exception e) {
            return null;
        }
    }

    public String faceAuthentication(String username,Long uid) {
        try {
            // 生成token
            String token = JwtUtils.generateToken(
                    new UserInfo(uid,username),
                    jwtProp.getPrivateKey(), jwtProp.getExpire());
            return token;
        } catch (Exception e) {
            return null;
        }
    }
}