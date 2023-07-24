package com.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class ReceiveLogs {
  private static final String EXCHANGE_NAME = "logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");


    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    //需要创建交换机，和队列 ，并且绑定交换机和队列
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    String queueName = "一号队列";
    channel.queueDeclare(queueName , true , false ,false ,null) ;
    channel.queueBind(queueName, EXCHANGE_NAME, "");
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [x] Received '" + message + "'");
    };
    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

    //需要
    Channel channel2 = connection.createChannel();
    channel2.exchangeDeclare(EXCHANGE_NAME, "fanout");
    String queueName2 = "二号队列号队列";
    channel2.queueDeclare(queueName2 , true , false ,false ,null) ;
    channel.queueBind(queueName, EXCHANGE_NAME, "");
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [x] Received '" + message + "'");
    };
    channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
  }
}