package com.inditex.ecommerce.pricing.infrastructure.aop;

import com.inditex.ecommerce.pricing.application.exception.PriceNotFoundException;
import com.inditex.ecommerce.pricing.domain.exception.DomainValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

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

    @Pointcut("within(com.inditex.ecommerce.pricing.application.mapper..*)"
            + " || within(com.inditex.ecommerce.pricing.infrastructure.out.persistence.mapper..*)")
    public void mapperLayer() {}

    @Around("(applicationLayer() || restLayer() || persistenceLayer()) && !mapperLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            if (log.isDebugEnabled()) {
                log.debug(InvocationLogFormatter.formatInvocation(signature, args, elapsed, result));
            }
            return result;
        } catch (Throwable ex) {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            String formatted = InvocationLogFormatter.formatError(signature, args, elapsed, ex.getMessage());
            if (isExpectedFailure(ex)) {
                log.debug(formatted);
            } else {
                log.error(formatted);
            }
            throw ex;
        }
    }

    private static boolean isExpectedFailure(Throwable ex) {
        return ex instanceof PriceNotFoundException
                || ex instanceof DomainValidationException
                || ex instanceof MissingServletRequestParameterException
                || ex instanceof MethodArgumentTypeMismatchException;
    }
}
