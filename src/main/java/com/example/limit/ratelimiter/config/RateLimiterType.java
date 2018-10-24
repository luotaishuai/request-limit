package com.example.limit.ratelimiter.config;

/**
 * 限流算法类型
 *
 * @author anonymity
 * @create 2018-10-24 11:08
 **/
public enum  RateLimiterType {
    // google guava 提供的RateLimiter实现
    GUAVA_RATELIMITER,
    // 计数器算法限流实现
    COUNTER_RATELIMITER;
}
