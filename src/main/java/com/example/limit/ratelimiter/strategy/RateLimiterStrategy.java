package com.example.limit.ratelimiter.strategy;

import com.example.limit.ratelimiter.util.AOPUtils;
import com.example.limit.ratelimiter.util.KeyFactory;
import com.example.limit.ratelimiter.config.RateLimiterMethod;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * google guava 限流实现
 *
 * @author anonymity
 * @create 2018-10-24 11:29
 **/
@Slf4j
@Service
public class RateLimiterStrategy {

    private ConcurrentMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * 秒和毫秒换算
     */
    private final long MICROSECONDS_OF_ONE_SECOND = 1000 * 1000L;

    /**
     * 限流处理入口
     */
    public Object handle(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        String key = createKey(pjp, rateLimiterMethod);
        RateLimiter rateLimiter = createLimiter(key, rateLimiterMethod);
        if (rateLimiter == null){
            log.info("rateLimiter is null, method: {}", pjp.getSignature().toLongString());
            return pjp.proceed();
        }

        long qps = rateLimiterMethod.qps();
        long timeout = MICROSECONDS_OF_ONE_SECOND / qps;

        // 拿到令牌则执行方法
        if (rateLimiter.tryAcquire(timeout, TimeUnit.MICROSECONDS)){
            return pjp.proceed();
        }

        // 被限流，如果设置降级方法，则执行降级方法
        if (StringUtils.isNotBlank(rateLimiterMethod.fallBackMethod())){
            Object object = pjp.getTarget();
            Method method = AOPUtils.getMethodFromTarget(pjp, rateLimiterMethod.fallBackMethod());

            if (method != null){
                Object result = method.invoke(object, pjp.getArgs());
                log.info("fallback method executed, class: {}, method: {}", object.getClass().getName(), rateLimiterMethod.fallBackMethod());
                return result;
            }

            log.warn("fallback method not exist, class: {}, method: {}", object.getClass().getName(), rateLimiterMethod.fallBackMethod());
        }
        log.info("request has been discarded, method: {}", pjp.getSignature().toLongString());
        return null;
    }

    /**
     * 构造RateLimiter,保证多线程环境下相同key对应的value不会被覆盖,且返回值相同
     */
    private RateLimiter createLimiter(String key, RateLimiterMethod rateLimiterMethod) {
        RateLimiter rateLimiter = limiters.get(key);
        if (rateLimiter == null){
            rateLimiter = RateLimiter.create(rateLimiterMethod.qps());
            RateLimiter putByOtherThread = limiters.putIfAbsent(key, rateLimiter);
            // 有其他线程写入了值
            if (putByOtherThread != null){
                rateLimiter = putByOtherThread;
            }
        }
        return rateLimiter;
    }

    /**
     * 构造RateLimiter关联的key
     */
    private String createKey(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) {
        // 使用注解时指定了key
        if (StringUtils.isNotBlank(rateLimiterMethod.key())){
            return rateLimiterMethod.key();
        }
        return KeyFactory.createKey(pjp);
    }
}
