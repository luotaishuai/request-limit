package com.example.limit.normal.config;


public class RequestLimitException extends Exception {
    private static final long serialVersionUID = 1364225358754654702L;

    public RequestLimitException(Long time) {
        super("您的请求过于频繁,请" + time + "s后再试");
    }

    public RequestLimitException(String message) {
        super(message);
    }
}