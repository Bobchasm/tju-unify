package com.tju.unify.conv.errand.service;

import com.tju.unify.conv.api.client.outer.ElmUserClient;
import com.tju.unify.conv.api.po.User;
import com.tju.unify.conv.common.exception.APIException;
import com.tju.unify.conv.common.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ErrandCurrentUserService {

    @Autowired
    private ElmUserClient elmUserClient;

    public Long requireUserId() {
        String username = UserContext.getUsername();
        if (!StringUtils.hasText(username)) {
            throw new APIException("未获取到用户信息");
        }
        User user = elmUserClient.getUserByName(username);
        if (user == null || user.getId() == null) {
            throw new APIException("用户不存在");
        }
        return user.getId();
    }
}
