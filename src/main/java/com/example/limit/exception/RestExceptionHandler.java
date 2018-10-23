package com.example.limit.exception;

import com.example.limit.common.RestResp;
import com.example.limit.config.RequestLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * 全局异常
 * @author anonymity
 * @create 2018-10-23 17:15
 **/
@ControllerAdvice(annotations = RestController.class)
@ResponseBody
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler
    @ResponseStatus
    public RestResp runtimeExceptionHandler(Exception e){
        if (e instanceof RuntimeException) {
            return RestResp.fail(e.getCause().getMessage());
        }
        return RestResp.fail("500统一处理异常");
    }
}
