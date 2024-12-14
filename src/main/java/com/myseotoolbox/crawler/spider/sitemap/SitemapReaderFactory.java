package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.spider.UriFilter;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SitemapReaderFactory {
    private final HttpRequestFactory requestFactory;

    public SiteMapReader getSitemapReaderFor(CrawlJobConfiguration configuration, UriFilter filter) {
        return new SiteMapReader(
                configuration.getOrigin(),
                configuration.getRobotsTxt().getSitemaps(),
                filter,
                configuration.getCrawledPageLimit(), requestFactory);
    }
}
