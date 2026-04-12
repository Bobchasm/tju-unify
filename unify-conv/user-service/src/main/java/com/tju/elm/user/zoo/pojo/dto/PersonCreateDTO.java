package com.tju.elm.user.zoo.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tju.elm.user.zoo.pojo.entity.Authority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Schema(description = "自然人用户创建请求DTO")
@AllArgsConstructor
@NoArgsConstructor
public class PersonCreateDTO {
    @Schema(description = "删除状态")
    @JsonProperty("deleted")
    private Boolean isDeleted=false;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    @Size(min = 1, max = 100, message = "用户名长度必须在1-20个字符之间")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "用户权限")
    private List<Authority> authorities;

    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式错误")
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "名")
    private String firstName;

    @Schema(description = "姓")
    private String lastName;

    @Schema(description = "性别")
    private String gender;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @Schema(description = "电话")
    private String phone;

    @Schema(description = "照片")
    private String photo;

}
