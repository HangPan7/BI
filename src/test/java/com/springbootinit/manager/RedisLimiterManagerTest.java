package com.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisLimiterManagerTest {

    @Resource
    RedisLimiterManager redisLimiterManager ;

    @Test
    void doRateLimit() throws InterruptedException {
        System.out.println(1+"1");
        System.out.println("1"+1);
        System.out.println(1+1);
            for(int i = 0 ; i<2 ;i++){
                redisLimiterManager.doRateLimit("1");
                System.out.println("成功"+i);
            }
            Thread.sleep(10000);
        for (int i = 0; i < 5; i++) {
            redisLimiterManager.doRateLimit("1");
            System.out.println("成功");
        }
    }
}