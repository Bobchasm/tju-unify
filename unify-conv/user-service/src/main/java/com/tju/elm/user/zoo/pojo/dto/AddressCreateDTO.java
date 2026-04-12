package com.tju.elm.user.zoo.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "地址创建请求DTO")
public class AddressCreateDTO {
    @Schema(description = "是否删除")
    @JsonProperty("deleted") // 映射JSON的deleted字段
    private Boolean isDeleted = false; // 默认未删除

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人性别（0-女，1-男）")
    private Integer contactSex;

    @Schema(description = "联系人电话")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String contactTel;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "所属用户（创建地址时通常关联当前登录用户，前端可选传username）")
    private CustomerSimpleDTO customer;

    @Data
    @Schema(description = "用户简化信息（地址关联用）")
    public static class CustomerSimpleDTO {
        @Schema(description = "用户ID（可选，优先用ID关联）")
        private Long id;

        @Schema(description = "用户名（必填，用于关联用户）")
        @NotBlank(message = "用户名不能为空")
        private String username;
    }

}
