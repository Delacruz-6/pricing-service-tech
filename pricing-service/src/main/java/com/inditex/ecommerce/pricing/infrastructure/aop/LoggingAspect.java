package com.inditex.ecommerce.pricing.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.inditex.ecommerce.pricing.application..*(..))")
    public void applicationLayer() {}

    @Pointcut("execution(* com.inditex.ecommerce.pricing.infrastructure.in.rest..*(..))")
    public void restLayer() {}

    @Pointcut("execution(* com.inditex.ecommerce.pricing.infrastructure.out.adapter..*(..))")
    public void persistenceLayer() {}

    @Around("applicationLayer() || restLayer() || persistenceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String shortClass = className.substring(className.lastIndexOf('.') + 1);

        if (log.isDebugEnabled()) {
            log.debug("[{}] invocando {}({})", shortClass, methodName, formatArgs(joinPoint.getArgs()));
        }

        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            if (log.isDebugEnabled()) {
                log.debug("[{}] finalizado {}() | {}ms | resultado: {}", shortClass, methodName, elapsed, formatResult(result));
            }
            return result;
        } catch (Throwable ex) {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            log.error("[{}] error en {}() | {}ms | {}", shortClass, methodName, elapsed, ex.getMessage());
            throw ex;
        }
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.toString())
                .collect(Collectors.joining(", "));
    }

    private String formatResult(Object result) {
        if (result == null) return "null";
        String str = result.toString();
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }
}