package com.xgxz.gmall.admin.aop;


import com.xgxz.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author 习惯向左
 * @create 2019-12-01 2:21
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {ArithmeticException.class})
    public Object handlerException(Exception exception){
        log.error("系统全局异常感知，信息是:{}",exception.getStackTrace());

        return new CommonResult().validateFailed("数学没学好");
    }

    @ExceptionHandler(value = {NullPointerException.class})
    public Object handlerExceptionNullPointer(Exception exception){
        log.error("系统全局异常感知，信息是:{}",exception.getStackTrace());

        return new CommonResult().validateFailed("空指针了...");
    }
}
