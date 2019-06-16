package com.myseotoolbox.crawler.spider;


import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.HttpURLConnectionFactory;
import com.myseotoolbox.crawler.spider.sitemap.SitemapReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpiderConfig {


    private HttpURLConnectionFactory connectionFactory = new HttpURLConnectionFactory();
    private HttpRequestFactory httpRequestFactory = new HttpRequestFactory(connectionFactory);

    @Bean
    public HttpRequestFactory getHttpRequestFactory() {
        return httpRequestFactory;
    }

    @Bean
    public CrawlExecutorFactory getExecutorBuilder() {
        return new CrawlExecutorFactory();
    }

    @Bean
    public CrawlJobFactory getCrawlJobFactory(CrawlExecutorFactory crawlExecutorFactory, SitemapReader sitemapReader) {
        return new CrawlJobFactory(new WebPageReaderFactory(httpRequestFactory), new WebsiteUriFilterFactory(), crawlExecutorFactory, sitemapReader);
    }
}
