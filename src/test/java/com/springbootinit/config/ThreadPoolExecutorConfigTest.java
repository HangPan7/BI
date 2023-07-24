package com.springbootinit.config;

import com.springbootinit.manager.AiManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ThreadPoolExecutorConfigTest {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor ;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor2 ;
    @Resource
    private AiManager aiManager ;
    @Test
    void getThreadPoolExecutor() {

        for (int i = 0 ; i<4; i++) {
            int finalI = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    aiManager.doChat("i");
                    System.out.println("哈哈哈，我没被取消");
                } catch (Exception e) {
                    System.out.println("已经被取消"+finalI);
                    e.printStackTrace();
                }
            }, threadPoolExecutor);
/*            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100);
                    if(!future.isDone()){
                        boolean cancelled = future.cancel(true);
                        if (cancelled) {
                            System.out.println("任务已取消"+finalI);
                        } else {
                            System.out.println("任务无法取消或已经完成"+finalI);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            },threadPoolExecutor2) ;*/
        }
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}