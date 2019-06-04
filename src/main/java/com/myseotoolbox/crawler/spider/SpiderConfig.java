package com.myseotoolbox.crawler.spider;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpiderConfig {

    @Bean
    public CrawlExecutorFactory getExecutorBuilder() {
        return new CrawlExecutorFactory();
    }

    @Bean
    public CrawlJobFactory getCrawlJobFactory(CrawlExecutorFactory crawlExecutorFactory, RobotsTxtFactory robotstxtFactory) {
        return new CrawlJobFactory(new WebPageReaderFactory(), new WebsiteUriFilterFactory(), crawlExecutorFactory, robotstxtFactory);
    }
}
