package com.example.limit.rest;

import com.example.limit.normal.config.RequestLimit;
import com.example.limit.ratelimiter.config.RateLimiterMethod;
import com.example.limit.ratelimiter.config.RateLimiterType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author anonymity
 */
@RestController
public class TestController {

    @RequestMapping("/hello")
    @RequestLimit(count = 2, time = 5000)
    public String test(HttpServletRequest request) {
        return "hello";
    }

    @RequestMapping("/rateLimiter")
    @RateLimiterMethod(type = RateLimiterType.COUNTER_RATELIMITER, qps = 2, fallBackMethod = "defaultFallBack")
    public String rateLimiterTest(){
        return "hello rateLimiter";
    }

    public String defaultFallBack(){
        return "被限流，执行限流降级方法";
    }
}