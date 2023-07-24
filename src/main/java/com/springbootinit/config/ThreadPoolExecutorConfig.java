package com.springbootinit.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor(){
        //创建线程工程，常用来给线程命名
        ThreadFactory threadLocal = new ThreadFactory(){
            private int count = 1 ;
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程"+count++);
                return thread;
            }
        } ;
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,4,100, TimeUnit.SECONDS
                ,new ArrayBlockingQueue<>(4) , threadLocal);
        //返回创建的线程池
        return threadPoolExecutor ;
    }
}