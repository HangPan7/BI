package com.springbootinit.manager;

import com.springbootinit.common.ErrorCode;
import com.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * redis通用限流器RedisLimiter
 */
@Service
public class RedisLimiterManager {

    @Resource
    RedissonClient redissonClient ;

    /**
     *
     * @param key 区分用户，用来创建不同的限流器
     */
    public void doRateLimit(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //采用 令牌桶的方法限流
        rateLimiter.trySetRate(RateType.OVERALL ,2 ,1 , RateIntervalUnit.SECONDS) ;
        boolean b = rateLimiter.tryAcquire(1);
        if(!b){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST) ;
        }
    }

}
