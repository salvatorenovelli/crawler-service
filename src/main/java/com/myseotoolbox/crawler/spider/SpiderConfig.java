package com.myseotoolbox.crawler.spider;


import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpiderConfig {

    @Bean
    public CrawlJobFactory getCrawlJobFactory(PageCrawlPersistence crawlPersistence, MonitoredUriUpdater monitoredUriUpdater) {
        return new CrawlJobFactory(new WebPageReaderFactory(), new WebsiteUriFilterBuilder(), new ExecutorBuilder(), monitoredUriUpdater, crawlPersistence);
    }
}
