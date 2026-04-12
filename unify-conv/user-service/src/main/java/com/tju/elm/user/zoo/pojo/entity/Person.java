package com.tju.elm.user.zoo.pojo.entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "个人信息实体类")
public class Person {
    @Schema(description = "个人信息ID")
    private Long id;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "名")
    private String firstName;

    @Schema(description = "姓")
    private String lastName;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "照片")
    private String photo;

    @Schema(description = "关联的用户")
    private User user;
}