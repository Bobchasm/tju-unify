package com.tju.elm.user.zoo.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//这是商家信息
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessInfoDTO {
    private Long userId;
    private String username;
    private String phone;
    private String photo;
}
