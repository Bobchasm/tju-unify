package com.tju.elm.user.zoo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户搜索DTO")
public class UserSearchDTO {
    @Schema(description = "搜索关键词（匹配用户名、手机号、邮箱）")
    private String keyword;

    @Schema(description = "用户状态（0-所有，1-启用，2-禁用）")
    private Integer status;
}