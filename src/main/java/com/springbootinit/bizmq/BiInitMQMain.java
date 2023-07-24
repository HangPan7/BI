package com.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;


public class BiInitMQMain {
    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setPassword("密码");
        connectionFactory.setUsername("用户名");
        connectionFactory.setHost("IP");
        connectionFactory.setPort(5672);
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            String BI_EXCHANGE = "bi_exchange" ;
            String DLX_BI_EXCHANGE = "dlx_bi_exchange" ;
            //先创建死信交换机和普通交换机
            channel.exchangeDeclare(BI_EXCHANGE,"direct") ;
            channel.exchangeDeclare(DLX_BI_EXCHANGE,"direct") ;

            //先创建死信队列
            String DLX_QUEUE_NAME = "dlx_queue_name" ;
            channel.queueDeclare(DLX_QUEUE_NAME , true ,false ,false,null) ;
            //将死信队列绑定到死信交换机
            channel.queueBind(DLX_QUEUE_NAME , DLX_BI_EXCHANGE , "DLX_BI_KEY") ;

            //创建普通的队列先绑定死信队列和死信交换机然后绑定普通的队列
            Map<String, Object> map = new HashMap<>();
            map.put("x-dead-letter-exchange" , DLX_BI_EXCHANGE) ;
            map.put("x-dead-letter-routing-key" , "DLX_BI_KEY") ;
            String BI_QUEUE_NAME = "bi_queue_name";
            //map 参数，不仅可以设置死信队列，还可以设置队列的过期时间
            channel.queueDeclare(BI_QUEUE_NAME ,true ,false ,false, map) ;
            channel.queueBind(BI_QUEUE_NAME , BI_EXCHANGE , "BI_KEY") ;
        } catch (Exception e) {
        }
    }
}
