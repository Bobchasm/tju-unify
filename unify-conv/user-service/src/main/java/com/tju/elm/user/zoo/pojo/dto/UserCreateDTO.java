package com.tju.elm.user.zoo.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tju.elm.user.zoo.pojo.entity.Authority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 20, message = "用户名长度必须在1-20个字符之间")
    private String username;

    private String password; // 可选

    @JsonProperty("deleted") // 映射JSON的deleted字段
    private Boolean isDeleted = false; // 默认未删除

    private List<Authority> authorities; // 权限列表

}
