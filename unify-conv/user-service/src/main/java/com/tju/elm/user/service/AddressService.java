package com.tju.elm.user.service;


import com.tju.elm.user.zoo.pojo.dto.AddressCreateDTO;
import com.tju.elm.user.zoo.pojo.entity.DeliveryAddress;
import com.tju.elm.user.zoo.pojo.vo.AddressVO;
import jakarta.validation.Valid;
import result.HttpResult;

import java.util.List;
import java.util.Set;

public interface AddressService {
    HttpResult<AddressVO> addDeliveryAddress(@Valid AddressCreateDTO createDTO);

    HttpResult<List<DeliveryAddress>> listDeliveryAddressByUserId(Long userId);

    HttpResult<DeliveryAddress> getDeliveryAddressById(Long id);

    HttpResult updateDeliveryAddress(DeliveryAddress deliveryAddress);

    HttpResult deleteDeliveryAddress(DeliveryAddress deliveryAddress);


    List<DeliveryAddress> getDeliveryAddressByIds(Set<Long> addressIds);
}
