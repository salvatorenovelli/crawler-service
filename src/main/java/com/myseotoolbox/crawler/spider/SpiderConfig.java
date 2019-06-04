package com.myseotoolbox.crawler.spider;


import com.myseotoolbox.crawler.spider.sitemap.SitemapReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpiderConfig {


    @Bean
    public CrawlExecutorFactory getExecutorBuilder() {
        return new CrawlExecutorFactory();
    }

    @Bean
    public CrawlJobFactory getCrawlJobFactory(CrawlExecutorFactory crawlExecutorFactory, RobotsTxtFactory robotstxtFactory, SitemapReader sitemapReader) {
        return new CrawlJobFactory(new WebPageReaderFactory(), new WebsiteUriFilterFactory(), crawlExecutorFactory, robotstxtFactory, sitemapReader);
    }
}
