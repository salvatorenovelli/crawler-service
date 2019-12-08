package com.myseotoolbox.crawler.config;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration

public class AsyncWorkspaceCrawlerConfig {
    private final int maxConcurrentCrawl;

    public AsyncWorkspaceCrawlerConfig(@Value("${max-concurrent-crawl}") int maxConcurrentCrawl) {
        this.maxConcurrentCrawl = maxConcurrentCrawl;
    }

    @Bean
    @Qualifier("crawl-job-init-executor")
    public Executor getCrawlJobInit() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(maxConcurrentCrawl);
        executor.setMaxPoolSize(maxConcurrentCrawl);
        executor.setThreadNamePrefix("crawljobinit-");
        executor.initialize();
        return executor;
    }
}
