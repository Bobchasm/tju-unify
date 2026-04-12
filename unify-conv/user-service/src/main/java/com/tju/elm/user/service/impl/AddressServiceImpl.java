package com.tju.elm.user.service.impl;


import com.tju.elm.user.mapper.DeliveryAddressMapper;
import com.tju.elm.user.mapper.UserMapper;
import com.tju.elm.user.service.AddressService;
import com.tju.elm.user.zoo.pojo.dto.AddressCreateDTO;
import com.tju.elm.user.zoo.pojo.entity.DeliveryAddress;
import com.tju.elm.user.zoo.pojo.entity.User;
import com.tju.elm.user.zoo.pojo.vo.AddressVO;
import com.tju.elm.user.zoo.pojo.vo.UserVO;
import com.tju.elm.user.zoo.utils.SecurityUtils;
import exception.APIException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import result.HttpResult;
import result.ResultCodeEnum;
import utils.UserContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DeliveryAddressMapper deliveryAddressMapper;
    @Override
    public HttpResult<AddressVO> addDeliveryAddress(AddressCreateDTO createDTO) {
        String currentUsername = UserContext.getUsername();
        if (currentUsername == null) {
            throw new APIException("当前用户未登录");
        }
        User targetUser = userMapper.findByUsernameWithAuthorities(createDTO.getCustomer().getUsername());
        User currentUser = userMapper.findByUsernameWithAuthorities(currentUsername);
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> "ADMIN".equals(auth.getName()));
        if (currentUser == null) {
            throw new APIException("当前用户不存在");
        }
        if (targetUser == null) {
            throw  new APIException("目标用户不存在");
        }

        // 检查权限：只能新增自己的地址，或者管理员可以新增任何人的地址
        if (currentUser.getUsername().equals(targetUser.getUsername()) || isAdmin) {
            LocalDateTime now = LocalDateTime.now();
            DeliveryAddress address = new DeliveryAddress();
            BeanUtils.copyProperties(createDTO, address);
            address.setCreateTime(now);
            address.setUpdateTime(now);
            address.setCreator(currentUser.getId());
            address.setUpdater(currentUser.getId());
            address.setIsDeleted(false);
            address.setUserId(currentUser.getId());
            address.setUser(currentUser);

            deliveryAddressMapper.insert(address);
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(address, addressVO);
            UserVO userVO = new UserVO();
            if (address.getUser() != null) {
                BeanUtils.copyProperties(address.getUser(), userVO);
            }
            addressVO.setCustomer(userVO);
            return HttpResult.success(addressVO);
        }else {
            return HttpResult.failure(ResultCodeEnum.NOT_ENOUGH_PERMISSION);
        }
    }

    @Override
    public HttpResult<List<DeliveryAddress>> listDeliveryAddressByUserId(Long userId) {
        String currentUsername = UserContext.getUsername();
        if (currentUsername == null) {
            throw new APIException("当前用户未登录");
        }
        User targetUser = userMapper.findByUserIdWithAuthorities(userId);
        User currentUser = userMapper.findByUsernameWithAuthorities(currentUsername);
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> "ADMIN".equals(auth.getName()));
        if (currentUser == null) {
            throw new APIException("当前用户不存在");
        }
        if (targetUser == null) {
            throw  new APIException("目标用户不存在");
        }
        if (currentUser.getUsername().equals(targetUser.getUsername()) || isAdmin){
            return HttpResult.success(deliveryAddressMapper.listDeliveryAddressByUserId(userId));
        }else {
            return HttpResult.failure(ResultCodeEnum.NOT_ENOUGH_PERMISSION);
        }
    }

    @Override
    public HttpResult<DeliveryAddress> getDeliveryAddressById(Long id) {
        return HttpResult.success(deliveryAddressMapper.getDeliveryAddressById(id));
    }

    @Override
    public HttpResult updateDeliveryAddress(DeliveryAddress deliveryAddress) {
        String currentUsername = UserContext.getUsername();
        if (currentUsername == null) {
            throw new APIException("当前用户未登录");
        }
        User currentUser = userMapper.findByUsernameWithAuthorities(currentUsername);
        LocalDateTime now = LocalDateTime.now();
        deliveryAddress.setUpdateTime(now);
        deliveryAddress.setUpdater(currentUser.getId());
        return HttpResult.success(deliveryAddressMapper.updateDeliveryAddress(deliveryAddress));
    }

    @Override
    public HttpResult deleteDeliveryAddress(DeliveryAddress deliveryAddress) {
        deliveryAddress.setIsDeleted(true);
        deliveryAddress.setUpdateTime(LocalDateTime.now());
        String currentUsername = UserContext.getUsername();
        if (currentUsername == null) {
            throw new APIException("当前用户未登录");
        }
        Long currentUserId = userMapper.getUserIdByUsername(currentUsername);
        deliveryAddress.setUpdater(currentUserId);
        return HttpResult.success(deliveryAddressMapper.updateDeliveryAddress(deliveryAddress));
    }


    @Override
    public List<DeliveryAddress> getDeliveryAddressByIds(Set<Long> addressIds) {
        return deliveryAddressMapper.getDeliveryAddressByIds(addressIds);
    }
}
