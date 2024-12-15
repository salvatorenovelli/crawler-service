package com.myseotoolbox.crawler.config;

import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.UriFilter;
import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import com.myseotoolbox.crawler.spider.ratelimiter.TimeBasedThrottler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebPageReaderFactory {
    private final HttpRequestFactory httpRequestFactory;
    private final ClockUtils clockUtils;

    public WebPageReader build(UriFilter uriFilter, long crawlDelayMillis) {
        return new WebPageReader(uriFilter, httpRequestFactory, new TimeBasedThrottler(crawlDelayMillis, clockUtils));
    }
}
