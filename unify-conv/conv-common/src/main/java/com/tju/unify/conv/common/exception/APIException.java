package com.tju.unify.conv.common.exception;


import lombok.Getter;
import com.tju.unify.conv.common.result.ResultCodeEnum;

// 接口错误时统一抛出APIException 由全局异常处理器统一捕获
@Getter
public class APIException extends RuntimeException {
    private String code;

    public APIException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    public APIException(String message) {
        super(message);
    }

    public APIException(String code, String message) {
        super(message);
        this.code = code;
    }
}
