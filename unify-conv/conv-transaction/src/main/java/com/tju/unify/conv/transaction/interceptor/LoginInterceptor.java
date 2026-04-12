package com.tju.unify.conv.transaction.interceptor;


import cdtu.mall.common.entity.UserInfo;
import cdtu.mall.common.utils.CookieUtils;
import cdtu.mall.common.utils.JwtUtils;
import com.tju.unify.conv.transaction.config.JwtProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    private JwtProperties jwtProperties;

    // 定义一个线程域，存放登录用户
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public LoginInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       System.out.println(request.getCookies());
       System.out.println(request.toString());
        // 查询token
        String token = CookieUtils.getCookieValue(request, "CDTU_MALL_TOKAEN");
//        token="eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiIxODA2MDMzMTIxIiwiZXhwIjoxNTkxMjM3MzMwfQ.Z3LYQHHQ6ndI9TeNaO-s8BizEBODYdt8RpuWpAULAbQDAMgr0h2v9b3NbqFARIMBk0Z7Ip4Akev9r2WUUC23Xt1SGpqpzS3E1TqxE0YwONOYE-ilUpY9xybexDdklqy1t_5nZ290swHml_DkKVTR4KBKrjYDg6supq0Sp4TsGT0; eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiIxODA2MDMzMTIxIiwiZXhwIjoxNTkxMjM3MzMzfQ.fARUD44BUKkKnxGMIiVEG2z65Nr8bLnXulrqlC5o209-IvZjiirg390Re7jws5vO-CvLx47PzzWoqHrn4KdjwGZzh__3r4bhSodvJAL48AFCmre8ew89teg0grS_rZQEgDLXqkDhiyVF-_ywwmvTS2SHirML_5SnbLhBohjEakA=";
//        token="eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiIxODA2MDMzMTIxIiwiZXhwIjoxNTkxMjM3MzMwfQ.Z3LYQHHQ6ndI9TeNaO-s8BizEBODYdt8RpuWpAULAbQDAMgr0h2v9b3NbqFARIMBk0Z7Ip4Akev9r2WUUC23Xt1SGpqpzS3E1TqxE0YwONOYE-ilUpY9xybexDdklqy1t_5nZ290swHml_DkKVTR4KBKrjYDg6supq0Sp4TsGT0";
        System.out.println(token);
        if (StringUtils.isBlank(token)) {
            // 未登录,返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        // 有token，查询用户信息
        try {
            // 解析成功，证明已经登录
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            // 放入线程域
            tl.set(user);
            return true;
        } catch (Exception e){
            // 抛出异常，证明未登录,返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }

    public static UserInfo getLoginUser() {
        return tl.get();
    }
}