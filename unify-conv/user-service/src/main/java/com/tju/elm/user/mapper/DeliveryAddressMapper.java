package com.tju.elm.user.mapper;

import com.tju.elm.user.zoo.pojo.entity.DeliveryAddress;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface DeliveryAddressMapper {

    @Select("SELECT * FROM delivery_address WHERE id=#{id}")
    public DeliveryAddress getDeliveryAddressById(Long id);

    public int updateDeliveryAddress(DeliveryAddress deliveryAddress);

    void insert(DeliveryAddress address);

    @Select("SELECT * FROM delivery_address WHERE user_id=#{userId} AND is_deleted=0")
    List<DeliveryAddress> listDeliveryAddressByUserId(Long userId);

    @Select("""
        <script>
            SELECT * FROM delivery_address da
                <where>
                    da.is_deleted = 0
                    AND
                    da.id in
                    <foreach collection='addressIds' item='addressId' open='(' separator=',' close=')'>
                        #{addressId}
                    </foreach>
                </where>
        </script>
    """)
    List<DeliveryAddress> getDeliveryAddressByIds(Set<Long> addressIds);
}
