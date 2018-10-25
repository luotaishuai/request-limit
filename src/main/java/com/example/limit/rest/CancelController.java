package com.example.limit.rest;

import com.example.limit.entity.ViolationIp;
import com.example.limit.ratelimiter.util.IpUtils;
import com.example.limit.repo.ViolationIpRepo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author anonymity
 * @create 2018-10-25 17:42
 **/
@RestController
public class CancelController {


    @Resource
    private ViolationIpRepo violationIpRepo;

    // 去除黑名单限制, 点击某个按钮，获取ip
    @GetMapping("/cancel")
    public String cancel(HttpServletRequest request){
        String ip = IpUtils.getRealIP(request);
        List<ViolationIp> list = violationIpRepo.findByIp(ip);

        if (list.isEmpty()){
            return "没有找到这个IP";
        }

        list.stream().forEach(s -> s.setEnable(0));

        ViolationIp violationIp = new ViolationIp(ip, System.currentTimeMillis(), 1);
        list.add(violationIp);

        violationIpRepo.save(list);

        return "解除成功";
    }
}
