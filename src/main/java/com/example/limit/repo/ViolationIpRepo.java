package com.example.limit.repo;

import com.example.limit.entity.ViolationIp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 违规访问的Ip
 * @author anonymity
 * @create 2018-10-25 15:44
 **/
@Repository
public interface ViolationIpRepo extends CrudRepository<ViolationIp, Long> {

    List<ViolationIp> findByIp(String realIp);

}
