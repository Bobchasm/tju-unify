package com.tju.unify.conv.common.exception;


import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.common.result.ResultCodeEnum;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public void clientAbortExceptionHandler(ClientAbortException clientAbortException) {
        log.warn(ResultCodeEnum.CLIENT_ABORT.getMessage());
    }

    // query参数不全
    @ExceptionHandler
    public HttpResult<Object> apiExceptionHandler(MissingServletRequestParameterException paramException) {
        return HttpResult.failure(ResultCodeEnum.PARAM_NOT_MATCHED_GET);
    }
    // body参数不全
    @ExceptionHandler
    public HttpResult<Object> apiExceptionHandler(HttpMessageNotReadableException messageNotReadableException) {
        return HttpResult.failure(ResultCodeEnum.PARAM_NOT_MATCHED_POST);
    }
    // 参数不匹配
    @ExceptionHandler
    public HttpResult<Object> apiExceptionHandler(MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
        log.error(methodArgumentTypeMismatchException.getMessage());
        return HttpResult.failure(ResultCodeEnum.PARAM_NOT_MATCHED);
    }
    // 请求类型不支持
    @ExceptionHandler
    public HttpResult<Object> apiExceptionHandler(HttpRequestMethodNotSupportedException httpRequestMethodNotSupportedException) {
        return HttpResult.failure(ResultCodeEnum.NOT_SUPPORTED);
    }
    // 参数校验不对
    @ExceptionHandler
    public HttpResult<Object> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return HttpResult.failure("PARAM_VERIFIED_FAILED",errors.toString());
    }

    /**
     * 捕获APIException
     * @param ex APIException异常
     */
    @ExceptionHandler(APIException.class)
    public HttpResult<Object> handleApiException(APIException ex) {
        log.error("========== GlobalExceptionHandler 捕获到 APIException ==========");
        log.error("错误码：{}", ex.getCode());
        log.error("错误信息：{}", ex.getMessage());
        log.error("堆栈信息：", ex);
        
        String code = ex.getCode();
        if (code != null && !code.isEmpty()) {
            return HttpResult.failure(code, ex.getMessage());
        }
        return HttpResult.failure(ResultCodeEnum.WITHOUT_ERROR_CODE.getCode(), ex.getMessage());
    }

    /**
     * 捕获其他业务逻辑异常
     * @param ex 异常
     */
    @ExceptionHandler(Exception.class)
    public HttpResult<Object> exceptionHandler(Exception ex) {
        log.error("========== GlobalExceptionHandler 捕获到通用异常 ==========");
        log.error("异常为：{}",ex.getClass());
        log.error("堆栈信息：", ex);
        
        if (ex.getMessage() == null){//异常信息为空 避免空指针异常
            return HttpResult.failure(ResultCodeEnum.NOT_KNOWN_ERROR);
        }
        // 其他提示
        if(ex.getMessage().length() > 50){
            return HttpResult.failure(ResultCodeEnum.NOT_KNOWN_ERROR);
        }
        // 无错误码 返回默认和message
        return HttpResult.failure(ResultCodeEnum.WITHOUT_ERROR_CODE.getCode(),ex.getMessage());
    }

}
