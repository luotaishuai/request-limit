package com.example.limit.normal.config;

import com.example.limit.common.Const;
import com.example.limit.common.RedisService;
import com.example.limit.entity.ViolationIp;
import com.example.limit.repo.ViolationIpRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class RequestLimitContract {

    @Resource
    private RedisService redisService;
    @Resource
    private ViolationIpRepo violationIpRepo;

    private Map<String, RequestData> redisTemplate = null;

    @PostConstruct
    public void listeningMap() {
        redisTemplate = new HashMap<>();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5, new BasicThreadFactory.Builder().namingPattern("request-thead-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (redisTemplate != null && redisTemplate.size() > 0) {
                        for (Map.Entry<String, RequestData> m : redisTemplate.entrySet()) {
                            RequestData value = m.getValue();
                            if (System.currentTimeMillis() - value.getTime() >= value.getLimit()) {
                                redisTemplate.remove(m.getKey());
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("listeningMap error:{}", e.getMessage(), e);
                }
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }

    @Before("(within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *))&& @annotation(limit)")
//    @Before("@annotation(limit)")
    public void requestLimit(final JoinPoint joinPoint, RequestLimit limit) throws RequestLimitException {
        try {
            Object[] args = joinPoint.getArgs();
            HttpServletRequest request = null;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof HttpServletRequest) {
                    request = (HttpServletRequest) args[i];
                    break;
                }
            }
            if (request == null) {
                return;
            }
            String ip = request.getRemoteAddr();
            String url = request.getRequestURL().toString();
            String key = "req_limit_".concat(url).concat(ip);
            if (redisTemplate.get(key) == null || redisTemplate.get(key).getCount() == 0) {
                redisTemplate.put(key, new RequestData(1, System.currentTimeMillis(), limit.time()));
            } else {
                redisTemplate.get(key).incr();
            }
            RequestData requestData = redisTemplate.get(key);

            if (requestData.getCount() > limit.count()) {
                log.warn("用户IP[{}]访问地址[{}]超过了规定时间访问限定的次数[{}]", ip, url, limit.count());
                long time = (requestData.getLimit() - (System.currentTimeMillis() - requestData.getTime())) / 1000;
                time = time < 1 ? 1 : time;

                String count = (String) redisService.get(ip);
                if (count != null) {
                    if (Integer.parseInt(count) < 20){
                        redisService.set(ip, String.valueOf(Integer.parseInt(count) + 1));
                    } else {
                        redisService.set(ip, Const.ANGEL_WORD);

                        ViolationIp violationIp = new ViolationIp(ip, System.currentTimeMillis(), 0);
                        violationIpRepo.save(violationIp);
                    }
                } else {
                    redisService.set(ip, "1");
                }

                throw new RequestLimitException(time);
            }
        } catch (RequestLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("发生异常", e);
        }
    }
}