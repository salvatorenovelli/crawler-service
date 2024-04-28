package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import com.myseotoolbox.crawler.spider.ratelimiter.TimeBasedThrottler;

public class WebPageReaderFactory {
    private final HttpRequestFactory httpRequestFactory;
    private final ClockUtils clockUtils;

    public WebPageReaderFactory(HttpRequestFactory httpRequestFactory, ClockUtils clockUtils) {
        this.httpRequestFactory = httpRequestFactory;
        this.clockUtils = clockUtils;
    }

    public WebPageReader build(UriFilter uriFilter, long crawlDelayMillis) {
        return new WebPageReader(uriFilter, httpRequestFactory, new TimeBasedThrottler(crawlDelayMillis, clockUtils));
    }
}
