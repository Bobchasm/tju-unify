package com.tju.unify.conv.common.result;

public enum ResultCodeEnum {
    /*** 通用部分 100 - 599***/
    // 成功请求
    SUCCESS("OK", "successful"),
    // 资源未找到
    NOT_FOUND("NOT_FOUND", "not found"),
    // 服务器错误
    SERVER_ERROR("GENERAL_ERROR","server error"),

    COMMON_ERROR("COMMON_ERROR","通用返回失败"),

    CLIENT_ABORT("CLIENT_ABORT","异常中止"),
    PARAM_NOT_MATCHED_GET("PARAM_NOT_MATCHED_GET","GET参数有误"),
    PARAM_NOT_MATCHED_POST("PARAM_NOT_MATCHED_POST","POST参数有误"),
    PARAM_NOT_MATCHED("PARAM_NOT_MATCHED","参数不匹配"),
    NOT_SUPPORTED("NOT_SUPPORTED","请求方式不支持"),
    NOT_KNOWN_ERROR("NOT_KNOWN_ERROR","未知错误"),
    PARAM_VERIFIED_FAILED("PARAM_VERIFIED_FAILED","参数校验不对"),
    WITHOUT_ERROR_CODE("WITHOUT_ERROR_CODE","无错误码"),
    UPLOAD_FAILED("UPLOAD_FAILED","上传失败")



    ;
    /**
     * 响应状态码
     */
    private String code;
    /**
     * 响应信息
     */
    private String message;

    ResultCodeEnum(String code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
