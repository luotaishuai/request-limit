package com.example.limit.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 违规操作IP表
 * @author anonymity
 * @create 2018-10-25 15:40
 **/
@Data
@Entity
public class ViolationIp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String ip;
    private Long createTime;
    private Integer enable = 0;// 没有限制：0，有限制：1
    private Integer opt = 0; // 系统操作：0，人员操作：1

    public ViolationIp() {
    }

    public ViolationIp(String ip, Long createTime, Integer opt) {
        this.ip = ip;
        this.createTime = createTime;
        this.opt = opt;
    }
}
