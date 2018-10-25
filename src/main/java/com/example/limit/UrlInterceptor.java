package com.example.limit;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.limit.common.Const;
import com.example.limit.common.RedisService;
import com.example.limit.entity.ViolationIp;
import com.example.limit.ratelimiter.util.IpUtils;
import com.example.limit.repo.ViolationIpRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author anonymity
 * @create 2018-10-24 16:44
 **/
@Slf4j
@Configuration
public class UrlInterceptor implements HandlerInterceptor {

    @Resource
    private RedisService redisService;
    @Resource
    private ViolationIpRepo violationIpRepo;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String ip = IpUtils.getRealIP(request);
        List<ViolationIp> violationIpList = violationIpRepo.findByIp(ip);
        if (!violationIpList.isEmpty()) {
            if (violationIpList.get(0).getEnable() == 1) {
                printResponse(response, Const.GOD_WORD);
                return false;
            }

            if (violationIpList.size() > 1) {
                long max1 = violationIpList.stream().mapToLong(s -> s.getCreateTime()).max().getAsLong();
                List<ViolationIp> newList = violationIpList.stream().filter(s -> s.getCreateTime() != max1).collect(Collectors.toList());
                long max2 = newList.stream().mapToLong(s -> s.getCreateTime()).max().getAsLong();
                // 两次违规操作间隔小于20s
                if ((max2 - max1) / 1000 < 20) {
                    violationIpList.stream().forEach(block -> block.setEnable(1));
                    violationIpRepo.save(violationIpList);
                    printResponse(response, Const.GOD_WORD);
                    redisService.remove(ip);
                    return false;
                }
            }
        }

        String count = (String) redisService.get(ip);
        if (count != null && count.contains(Const.ANGEL_WORD)) {
            printResponse(response, count);

            redisService.remove(ip);
            return false;
        }
        return true;
    }

    private void printResponse(HttpServletResponse response, String count) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json;charset=utf-8");
        PrintWriter printWriter = response.getWriter();

        Map<String, Object> map = new HashMap<>();
        map.put("status", -1);
        map.put("message", count);
        map.put("data", null);
        String result = JSONObject.toJSONString(map, SerializerFeature.WriteMapNullValue);

        printWriter.write(result);
        printWriter.flush();
        printWriter.close();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

}
