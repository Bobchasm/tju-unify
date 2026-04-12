package com.tju.elm.user.service;


import com.tju.elm.user.mapper.UserMapper;
import com.tju.elm.user.zoo.exception.UserNotActivatedException;
import com.tju.elm.user.zoo.pojo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("userDetailsService")
public class UserModelDetailsService implements UserDetailsService{
    private final UserMapper userMapper;

    public UserModelDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating user '{}'", login);

        User user = userMapper.findByUsernameWithAuthorities(login);

        if (user == null) {
            throw new UsernameNotFoundException("User " + login + " was not found in the database");
        } else if (user.getActivated() == null||!user.getActivated()) {
            throw new UserNotActivatedException("User " + login + " was not activated");
        }

        log.debug("用户 {} 的权限原始数据：{}", user.getUsername(), user.getAuthorities());
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(); // 初始化空列表，避免null
        if (user.getAuthorities() != null) { // 若权限列表不为null，处理有效权限
            grantedAuthorities = user.getAuthorities().stream()
                    // 过滤：Authority不为null 且 name不为null/空字符串
                    .filter(auth -> auth != null && StringUtils.hasText(auth.getName()))
                    .map(auth -> new SimpleGrantedAuthority(auth.getName()))
                    .collect(Collectors.toList());
        }
        // 打印处理后的权限列表
        log.debug("处理后的权限列表：{}", grantedAuthorities);

// 增加非空校验
        if (grantedAuthorities.isEmpty()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("USER")); // 默认角色
            log.warn("用户 {} 未分配任何权限，已自动添加默认权限USER", user.getUsername());
        }


        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                grantedAuthorities
        );
    }

    public void clearUserCache(String username) {
        // 因为没有使用Redis，这里不需要实现
    }
}
