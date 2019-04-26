package com.github.tng.vnv.planner.aspect

import groovy.util.logging.Log
import groovy.util.logging.Slf4j
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
//@Slf4j
@Log
public class TimeLogAspect {
//    @Around("@annotation(com.github.tng.vnv.planner.aspect.Timed) && execution(public * * (..))")
    @Around("@annotation(Timed)")
    Object time(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object value;

        try {
            value = proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            long duration = System.currentTimeMillis() - start;

/*
            log.info(
                    "{}.{} tookkk {} ms",
                    proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName(),
                    proceedingJoinPoint.getSignature().getName(),
                    duration);
*/
            log.info(
                    "##vnvlog.ver2 duration: ${proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName()}." +
                            "${proceedingJoinPoint.getSignature().getName()} " +
                            " tookkk ${duration} ms");
        }

        return value;
    }
}