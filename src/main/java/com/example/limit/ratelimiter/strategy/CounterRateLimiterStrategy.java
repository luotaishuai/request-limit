package com.example.limit.ratelimiter.strategy;

import com.example.limit.ratelimiter.util.AOPUtils;
import com.example.limit.ratelimiter.util.KeyFactory;
import com.example.limit.ratelimiter.config.RateLimiterMethod;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 计数器算法限流实现
 *
 * @author anonymity
 * @create 2018-10-24 13:54
 **/
@Slf4j
@Service
public class CounterRateLimiterStrategy {

    /**
     * google guava cache 来存储计数器，过期时间设置为2s，（保证1s内的计数器是有效的）
     */
    private ConcurrentMap<String, LoadingCache<Long, AtomicLong>> counters = new ConcurrentHashMap<>();

    /**
     * 限流处理入口
     */
    public Object handle(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) throws Throwable {
        String key = createKey(pjp, rateLimiterMethod);
        LoadingCache<Long, AtomicLong> counter = createCounter(key, rateLimiterMethod);

        // 获取当前时间戳， 然后去秒数作为key进行数统计和限流
        long currentSecond = System.currentTimeMillis() / 1000;
        long qps = rateLimiterMethod.qps();

        AtomicLong atomicLong = counter.get(currentSecond);

        if (atomicLong == null) {
            log.info("counter is null, method: {}", pjp.getSignature().toLongString());
            return pjp.proceed();
        }

        if (atomicLong.incrementAndGet() <= qps) {
            return pjp.proceed();
        }

        // 被限流， 如果设置了降级方法， 则执行降级方法
        if (StringUtils.isNotBlank(rateLimiterMethod.fallBackMethod())) {
            Object object = pjp.getTarget();
            Method method = AOPUtils.getMethodFromTarget(pjp, rateLimiterMethod.fallBackMethod());

            if (method != null) {
                Object result = method.invoke(object, pjp.getArgs());
                log.info("fallBack method executed, class: {}, method: {}", object.getClass().getName(), rateLimiterMethod.fallBackMethod());
                return result;
            }
            log.warn("fallBack method not exist, class: {}, method: {}", object.getClass().getName(), rateLimiterMethod.fallBackMethod());
        }
        log.info("request has been discarded, method: {}", pjp.getSignature().toLongString());
        return null;
    }

    /**
     * 构造计数器,保证多线程环境下相同key对应的value不会被覆盖,且返回值相同
     */
    private LoadingCache<Long, AtomicLong> createCounter(String key, RateLimiterMethod rateLimiterMethod) {
        LoadingCache<Long, AtomicLong> loadingCache = counters.get(key);
        if (loadingCache == null) {
            // Guava Cache来存储计数器，过期时间设置为2秒（保证1秒内的计数器是有效的）
            loadingCache = CacheBuilder
                    .newBuilder()
                    .expireAfterWrite(2, TimeUnit.SECONDS)
                    .build(new CacheLoader<Long, AtomicLong>() {
                        @Override
                        public AtomicLong load(Long aLong) throws Exception {
                            return new AtomicLong(0);
                        }
                    });
            LoadingCache<Long, AtomicLong> putByOtherThread = counters.putIfAbsent(key, loadingCache);
            // 有其他线程写入了值
            if (putByOtherThread != null) {
                loadingCache = putByOtherThread;
            }
        }
        return loadingCache;
    }

    /**
     * 构造counter关联的key
     */
    private String createKey(ProceedingJoinPoint pjp, RateLimiterMethod rateLimiterMethod) {
        // 使用注解时指定了key
        if (StringUtils.isNotBlank(rateLimiterMethod.key())) {
            return rateLimiterMethod.key();
        }
        return KeyFactory.createKey(pjp);
    }
}
