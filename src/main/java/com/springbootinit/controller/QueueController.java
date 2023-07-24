package com.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/queue")
@Profile({"dev","local"})
@Slf4j
public class QueueController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor ;

    @GetMapping("/add")
    public void add(String name){
        CompletableFuture.runAsync(() -> {
           log.info("正在执行的任务"+name);
            try {
                Thread.sleep(660000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },threadPoolExecutor) ;
    }
    @GetMapping("/get")
    public String get(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("队列长度" , threadPoolExecutor.getQueue().size()) ;
        map.put("已完成的线程数" ,threadPoolExecutor.getTaskCount());
        return JSONUtil.toJsonStr(map);
    }
}
