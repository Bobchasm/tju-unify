package com.tju.elm.user.service.impl;


import com.tju.elm.user.mapper.DeliveryAddressMapper;
import com.tju.elm.user.mapper.UserMapper;
import com.tju.elm.user.service.UserService;
import com.tju.elm.user.zoo.pojo.dto.BusinessInfoDTO;
import com.tju.elm.user.zoo.pojo.entity.DeliveryAddress;
import com.tju.elm.user.zoo.pojo.entity.User;
import com.tju.elm.user.zoo.pojo.vo.UserVO;
import com.tju.elm.user.zoo.utils.SecurityUtils;
import exception.APIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.UserContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DeliveryAddressMapper deliveryAddressMapper;


    public User getUserWithAuthorities(String username) {
        return userMapper.findByUsernameWithAuthorities(username);
    }

    public void addUser(User user) {
        userMapper.insert(user);
    }

    public void updateUser(User user) {
        userMapper.update(user);
    }

    public boolean isEmptyUserTable() {
        return userMapper.count() == 0;
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public UserVO changeUserStatus(String username) {
        String currentUsername = UserContext.getUsername();
        if (currentUsername==null) {
            throw new APIException("未获取到当前登录用户名");
        }
        User currentUser = userMapper.findByUsernameWithAuthorities(currentUsername);
        log.info("当前用户：{}", currentUser);
        User targetUser = userMapper.findByUsernameWithAuthorities(username);
        if (targetUser == null) {
            throw new APIException("目标用户不存在");
        }

        targetUser.setActivated(!targetUser.getActivated());
        targetUser.setUpdateTime(LocalDateTime.now()); // 更新时间
        targetUser.setUpdater(currentUser.getId()); // 更新人ID（当前管理员ID）

        userMapper.update(targetUser);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(targetUser, userVO);
        return userVO;
    }

    @Override
    public void deleteUser(String username) {
        User targetUser = userMapper.findByUsernameWithAuthorities(username);
        String currentUsername = UserContext.getUsername();
        if (currentUsername==null) {
            throw new APIException("未获取到当前登录用户名");
        }
        User currentUser = userMapper.findByUsernameWithAuthorities(currentUsername);
        if (targetUser == null) {
            throw new APIException("目标用户不存在");
        }
        if (targetUser.getIsDeleted()) {
            throw new APIException("用户已被删除");
        }
        if (currentUsername.equals(username)) {
            throw new APIException("不能删除当前登录的管理员账号");
        }

        targetUser.setIsDeleted(true);
        targetUser.setUpdateTime(LocalDateTime.now());
        targetUser.setUpdater(currentUser.getId());
        userMapper.update(targetUser);
    }

    @Override
    public void toggleUserActivated(String username, Boolean activated) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setActivated(activated);
        userMapper.updateActivated(user); // 更新数据库activated字段
    }

    @Override
    public List<BusinessInfoDTO> getAllActiveBusinesses() {
        List<BusinessInfoDTO> businesses = userMapper.getAllActiveBusinesses();
        return businesses;
    }

    @Override
    public List<User> getUserByIds(Set<Long> userIds) {
        return userMapper.getUserByIds(userIds);
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.findById(userId);
    }

}
