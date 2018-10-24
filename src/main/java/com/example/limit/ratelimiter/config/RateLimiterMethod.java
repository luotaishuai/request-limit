package com.example.limit.ratelimiter.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流方法
 *
 * @author anonymity
 * @create 2018-10-24 11:05
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiterMethod {

    /**
     * 限流算法类型，默认采用google guava
     */
    RateLimiterType type() default RateLimiterType.GUAVA_RATELIMITER;

    /**
     * 限流的QPS值
     */
    long qps();

    /**
     * 请求被限流后降级处理方法（该方法签名需要和被限流的方法保持一致）
     */
    String fallBackMethod() default "";

    /**
     * 每个RateLimiter的标识,key相同的情况下则使用同一个RateLimiter
     */
    String key() default "";
}
