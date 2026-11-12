package com.aircookies.aircookeisairobotspringboot.aspect;

import com.aircookies.aircookeisairobotspringboot.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class ApiOperationLogAspect {

    @Pointcut("@annotation(com.aircookies.aircookeisairobotspringboot.aspect.ApiOperationLog)")
    public void apiOperationLog() {
    }

    /**
     * 环绕
     *
     * @param joinPoint 切点
     * @return
     * @throws Throwable
     */
    @Around("apiOperationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 请求开始时间
        long startTime = System.currentTimeMillis();

        // 获取被请求的类和方法
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();

        // 请求入参
        Object[] args = joinPoint.getArgs();
        // 入参转Json字符串
        String argsJsonStr = Arrays.stream(args).map(JsonUtil::toJson).collect(Collectors.joining(", "));

        // 功能描述信息
        String description = getApiOperationLogDescription(joinPoint);

        // 打印请求参数
        log.info("=================== 请求开始: [{}], 入参: [{}], 请求类: [{}], 请求方法: [{}] ====================",
                description, argsJsonStr, className, methodName);

        // 执行切点方法
        Object result = joinPoint.proceed();

        // 执行耗时
        long executionTime = System.currentTimeMillis() - startTime;

        // 打印相关信息
        log.info("=================== 请求结束: [{}], 耗时: {}ms, 出参: {} ====================",
                description, executionTime, JsonUtil.toJson(result));

        return result;
    }

    /**
     * 获取注解的描述信息
     *
     * @param joinPoint 切点
     * @return
     */
    public String getApiOperationLogDescription(ProceedingJoinPoint joinPoint) {
        // 1. 从切点获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 2. 使用方法签名获取方法
        Method method = signature.getMethod();

        // 3. 从方法中提取注解
        ApiOperationLog apiOperationLog = method.getAnnotation(ApiOperationLog.class);

        // 4. 从注解中获取描述信息
        return apiOperationLog.description();
    }
}
