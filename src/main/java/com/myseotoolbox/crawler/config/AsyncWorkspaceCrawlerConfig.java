package com.myseotoolbox.crawler.config;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration

public class AsyncWorkspaceCrawlerConfig {
    public static final int MAX_CONCURRENT_CRAWL_START = 10;

    @Bean
    @Qualifier("crawl-job-init-executor")
    public Executor getCrawlJobInit() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(MAX_CONCURRENT_CRAWL_START);
        executor.setMaxPoolSize(MAX_CONCURRENT_CRAWL_START);
        executor.setThreadNamePrefix("crawljobinit-");
        executor.initialize();
        return executor;
    }
}
