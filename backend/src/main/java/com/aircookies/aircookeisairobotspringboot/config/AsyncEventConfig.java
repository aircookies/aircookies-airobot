package com.aircookies.aircookeisairobotspringboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncEventConfig {

    /**
     * 自定义事件处理线程池
     */
    @Bean("eventTaskExecutor")
    public Executor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 队列大小
        executor.setQueueCapacity(50);
        // 允许的线程空闲时间
        executor.setKeepAliveSeconds(60);
        // 线程名前缀
        executor.setThreadNamePrefix("event-handler-");
        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
        // 等所有任务结束后才关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待终止的最大时间(秒)
        executor.setAwaitTerminationSeconds(60);

        // 初始化
        executor.initialize();
        return executor;
    }
}
