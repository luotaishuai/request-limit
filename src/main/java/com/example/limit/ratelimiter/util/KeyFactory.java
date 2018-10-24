package com.example.limit.ratelimiter.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import javax.servlet.http.HttpServletRequest;

/**
 * key生成工厂
 *
 * @author anonymity
 * @create 2018-10-24 11:34
 **/
public class KeyFactory {

    /**
     * 根据Annotation注解的需要限流的方法,生成RateLimiter关联的key
     */
    public static String createKey(JoinPoint point){
        StringBuilder sb = new StringBuilder();
        appendType(sb, getType(point));
        Signature signature = point.getSignature();

        if (signature instanceof MethodSignature){
            MethodSignature ms = (MethodSignature) signature;
            sb.append("#");
            sb.append(ms.getMethod().getName());
            sb.append("(");
            appendTypes(sb, ms.getMethod().getParameterTypes());
            sb.append(")");
        }
        return sb.toString();
/*
        上面的key会重复，使用下面的时候controller 方法的参数中要加 HttpServletRequest才能获取到request
        Object[] args = point.getArgs();
        HttpServletRequest request = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof HttpServletRequest) {
                request = (HttpServletRequest) args[i];
                break;
            }
        }
        if (request == null) {
            return null;
        }
        String ip = IpUtils.getRealIP(request);
        String url = request.getRequestURL().toString();
        String key = "req_limit_".concat(url).concat(ip);*/
    }

    private static void appendType(StringBuilder sb, Class<?> type) {
        if (type.isArray()){
            appendType(sb, type.getComponentType());
            sb.append("[]");
        } else {
            sb.append(type.getName());
        }
    }

    private static void appendTypes(StringBuilder sb, Class<?>[] types) {
        for (int size = types.length, i = 0; i < size; i++) {
            appendType(sb, types[i]);
            if (i < size - 1) {
                sb.append(",");
            }
        }
    }

    private static Class<?> getType(JoinPoint point) {
        if (point.getSourceLocation() != null){
            return point.getSourceLocation().getWithinType();
        } else {
            return point.getSignature().getDeclaringType();
        }
    }
}
