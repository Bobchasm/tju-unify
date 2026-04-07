package cdtu.mall.auth.controller;

import cdtu.mall.auth.FeignClient.UserClient;
import cdtu.mall.auth.config.JwtProperties;
import cdtu.mall.auth.service.AuthService;
import cdtu.mall.common.entity.UserInfo;
import cdtu.mall.common.utils.CookieUtils;
import cdtu.mall.common.utils.JwtUtils;
import cdtu.mall.user.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
//@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;


    /**
     * 登录授权
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Void>  authentication(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
             HttpServletRequest request,
             HttpServletResponse response) {
        // 登录校验
//        System.out.println("jin");
        String token = this.authService.authentication(username, password);
        if (StringUtils.isBlank(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // 将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改, null, true
        CookieUtils.setCookie(request, response, prop.getCookieName(),token, prop.getCookieMaxAge());
        return ResponseEntity.ok().build();
    }

    /**
     * 验证用户信息
     * @Cooki
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("CDTU_MALL_TOKAEN") String token,
                                               HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取token信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 如果成功，我们还需要刷新token
            String newToken = JwtUtils.generateToken(userInfo,
                    prop.getPrivateKey(), prop.getExpire());
            // 然后写入cookie中
            // 将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改
            CookieUtils.setCookie(request, response, prop.getCookieName(),
                    newToken, prop.getCookieMaxAge(), null, true);
            // 成功后直接返回
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            // 抛出异常，证明token无效，直接返回401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}