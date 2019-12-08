package com.myseotoolbox.crawler.config;


import com.myseotoolbox.crawler.spider.ConcurrentCrawlsSemaphore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ConcurrentCrawlsSemaphore getConcurrentCrawlsSemaphore(@Value("${max-concurrent-crawl}") int maxConcurrentCrawl) {
        return new ConcurrentCrawlsSemaphore(maxConcurrentCrawl);
    }
}
