package com.example.limit.config;

import lombok.Data;

@Data
public class RequestData {
    private Integer count;
    private Long time;
    private Long limit;
    public RequestData(Integer count, Long time, Long limit) {
        this.count = count;
        this.time = time;
        this.limit = limit;
    }
    public RequestData() {
    }
    public void incr(){
        if (count != null) {
            this.count += 1;
        }
    }
}