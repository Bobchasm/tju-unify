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

    KEY_MISSED("KEY_MISSED","配置键不存在"),
    CLIENT_ABORT("CLIENT_ABORT","异常中止"),
    PARAM_NOT_MATCHED_GET("PARAM_NOT_MATCHED_GET","GET参数有误"),
    PARAM_NOT_MATCHED_POST("PARAM_NOT_MATCHED_POST","POST参数有误"),
    PARAM_NOT_MATCHED("PARAM_NOT_MATCHED","参数不匹配"),
    NOT_SUPPORTED("NOT_SUPPORTED","请求方式不支持"),
    NOT_KNOWN_ERROR("NOT_KNOWN_ERROR","未知错误"),
    PARAM_VERIFIED_FAILED("PARAM_VERIFIED_FAILED","参数校验不对"),
    WITHOUT_ERROR_CODE("WITHOUT_ERROR_CODE","无错误码"),

    ADDRESS_PERMISSION_DENIED("ADDRESS_PERMISSION_DENIED","权限不足，无法为该用户添加地址"),
    ADDRESS_MISSED("ADDRESS_MISSED","地址不存在"),
    NOT_ENOUGH_PERMISSION("NOT_ENOUGH_PERMISSION","权限不足"),
    VALUE_MISSED("VALUE_MISSED","值不存在"),
    FOOD_MISSED("FOOD_MISSED","商品不存在"),
    USER_DENIED("USER_DENIED","无法对别的用户进行操作"),
    CART_MISSED("CART_MISSED","购物车商品不存在"),
    FOOD_UNSHELVED("CART_MISSED","商品已下架"),
    FOOD_STATUS_SET_FAILED("FOOD_STATUS_SET_FAILED","商品上下架失败"),
    USER_MISSED("USER_MISSED","用户不存在"),
    USER_VALUE_MISSED("USER_VALUE_MISSED","管理员未指定用户"),
    CART_EMPTY("CART_EMPTY","购物车为空"),

    ORDER_MISSED("ORDER_MISSED","订单不存在"),
    ORDER_STATUS_UNMATCHED("ORDER_STATUS_UNMATCHED","订单状态错误"),
    ORDER_PAY_FAILED("ORDER_PAY_FAILED","订单支付失败"),
    ORDER_ACCEPT_FAILED("ORDER_ACCEPT_FAILED","接单失败"),
    ORDER_CANCEL_DENY("ORDER_CANCEL_DENY","无法取消已被接收的订单"),
    ORDER_CANCEL_FAILED("ORDER_CANCEL_FAILED","订单取消失败"),
    ORDER_SUBMIT_FAILED("ORDER_SUBMIT_FAILED","下单失败"),

    QUANTITY_ILLEGAL("QUANTITY_ILLEGAL","数量不合法"),

    BUSINESS_MISSED("BUSINESS_MISSED","商家不存在"),
    UNAUTHORIZED("UNAUTHORIZED","无权限"),
    USER_UNMATCHED("USER_UNMATCHED","无法查看其他用户信息"),


    VIRTUAL_WALLET_MISSED("VIRTUAL_WALLET_MISSED","您还未开通钱包"),
    VIRTUAL_WALLET_OPENED("VIRTUAL_WALLET_MISSED","您的钱包已开通"),
    TOUSER_VIRTUAL_WALLET_MISSED("TOUSER_VIRTUAL_WALLET_MISSED","收款方还未开通钱包，请尝试其他支付方式"),
    BALANCE_LIMIT("BALANCE_LIMIT","钱包余额不足"),

    UPLOAD_FAILED("UPLOAD_FAILED","上传失败")

    /*** 这里可以根据不同模块用不同的区级分开错误码，例如:  ***/

    // 1000～1999 区间表示用户模块错误
    // 2000～2999 区间表示订单模块错误
    // 3000～3999 区间表示商品模块错误
    // 。。。

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
