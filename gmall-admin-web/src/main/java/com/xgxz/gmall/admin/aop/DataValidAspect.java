package com.xgxz.gmall.admin.aop;

import com.xgxz.gmall.to.CommonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

/**
 * @author 习惯向左
 * @create 2019-12-01 1:30
 *
 * 数据校验切面
 * 通知：
 *      前置通知
 *      后置通知
 *      返回通知
 *      异常通知
 *
 *      正常执行：前置通知 --> 返回通知 --> 后置通知
 *      异常执行：前置通知 --> 异常通知 --> 后置通知
 *
 *      环绕通知：4合1
 */
@Aspect
@Component
public class DataValidAspect {

    @Around("execution(* com.xgxz.gmall.admin..*controller .*(..))")
    public Object validAround(ProceedingJoinPoint point) throws Throwable {

        Object proceed = null;
        Object[] args = point.getArgs();
        for(Object obj : args){
            if (obj instanceof BindingResult){
                BindingResult result = (BindingResult) obj;
                if (result.getErrorCount() > 0){
                    // 框架自动校验检测到错了
                    return new CommonResult().validateFailed(result);
                }
            }
        }
        // 1、前置通知
        proceed = point.proceed(point.getArgs());

        return proceed;
    }
}
