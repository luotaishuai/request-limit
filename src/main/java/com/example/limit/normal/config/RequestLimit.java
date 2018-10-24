package com.example.limit.normal.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Order(Ordered.HIGHEST_PRECEDENCE)
public @interface RequestLimit {
    /**
     * 允许访问的最大次数
     */
    int count() default 100;

    /**
     * 时间段，单位为毫秒，默认值一分钟
     */
    long time() default 60000;
}