package com.springbootinit.bizmq;

import com.springbootinit.constant.BIMqConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BIMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate ;

    public void sedMessage(String message){
        rabbitTemplate.convertAndSend(BIMqConstant.BI_EXCHANGE ,BIMqConstant.BI_KEY , message);
    }
}
