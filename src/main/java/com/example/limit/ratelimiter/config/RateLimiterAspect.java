package com.example.limit.ratelimiter.config;

import com.example.limit.ratelimiter.strategy.CounterRateLimiterStrategy;
import com.example.limit.ratelimiter.strategy.RateLimiterStrategy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 限流处理切面
 *
 * @author anonymity
 * @create 2018-10-24 11:24
 **/
@Aspect
@Component
@Order(1)
public class RateLimiterAspect {

    @Resource
    private RateLimiterStrategy rateLimiterStrategy;
    @Resource
    private CounterRateLimiterStrategy counterRateLimiterStrategy;

    @Around("@annotation(rateLimiterMethod)")
    public Object method(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        Object result;
        switch (rateLimiterMethod.type()){
            case GUAVA_RATELIMITER:
                result = rateLimiterStrategy.handle(pjp, rateLimiterMethod);
                break;
            case COUNTER_RATELIMITER:
                result = counterRateLimiterStrategy.handle(pjp, rateLimiterMethod);
                break;
            default:
                result = null;
                break;
        }
        return result;
    }
}
