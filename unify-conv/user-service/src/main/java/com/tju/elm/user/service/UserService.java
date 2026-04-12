package com.tju.elm.user.service;


import com.tju.elm.user.zoo.pojo.dto.BusinessInfoDTO;
import com.tju.elm.user.zoo.pojo.entity.DeliveryAddress;
import com.tju.elm.user.zoo.pojo.entity.User;
import com.tju.elm.user.zoo.pojo.vo.UserVO;

import java.util.List;
import java.util.Set;

public interface UserService {
    User getUserWithAuthorities(String username);

    void addUser(User user);

    void updateUser(User user);

    boolean isEmptyUserTable();

    User findByUsername(String username);

    UserVO changeUserStatus(String username);

    void deleteUser(String username);

    void toggleUserActivated(String username, Boolean activated);

    List<BusinessInfoDTO> getAllActiveBusinesses();

    List<User> getUserByIds(Set<Long> userIds);

    User getUserById(Long userId);

}