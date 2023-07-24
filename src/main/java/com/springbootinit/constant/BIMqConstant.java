package com.springbootinit.constant;

public interface BIMqConstant {
    //bi交换机
    String BI_EXCHANGE = "bi_exchange" ;

    //处理bi任务失败的交换机
    String DLX_BI_EXCHANGE = "dlx_bi_exchange" ;

    //处理bi任务失败的死信队列 DLX_BI_EXCHANGE的队列
    String DLX_QUEUE_NAME = "dlx_queue_name" ;

    //死信队列 DLX_QUEUE_NAME 的路由
    String DLX_BI_KEY = "DLX_BI_KEY" ;

    //BI_EXCHANGE的队列
    String BI_QUEUE_NAME = "bi_queue_name";

    //BI_QUEUE_NAME的路由
    String BI_KEY = "BI_KEY" ;
}
