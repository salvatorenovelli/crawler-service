package com.myseotoolbox.crawler.config;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration

public class AsyncWorkspaceCrawlerConfig {
    private final int maxConcurrentCrawlStart;

    public AsyncWorkspaceCrawlerConfig(@Value("${max-concurrent-crawl-start}") int maxConcurrentCrawlStart) {
        this.maxConcurrentCrawlStart = maxConcurrentCrawlStart;
    }

    @Bean
    @Qualifier("crawl-job-init-executor")
    public Executor getCrawlJobInit() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(maxConcurrentCrawlStart);
        executor.setMaxPoolSize(maxConcurrentCrawlStart);
        executor.setThreadNamePrefix("crawljobinit-");
        executor.initialize();
        return executor;
    }
}
