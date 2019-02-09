package com.myseotoolbox.crawler.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Slf4j
public class AsyncMonitoredUriScanConfig {


    public static final int MAX_CONCURRENT_SCAN_COUNT = 10;

    @Bean(name = "asyncMonitoredUriScanExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(MAX_CONCURRENT_SCAN_COUNT);
        executor.setMaxPoolSize(MAX_CONCURRENT_SCAN_COUNT);
        executor.setThreadNamePrefix("scanExec-");
        executor.initialize();
        return executor;
    }


}