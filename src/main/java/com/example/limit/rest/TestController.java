package com.example.limit.rest;

import com.example.limit.config.RequestLimit;
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
}