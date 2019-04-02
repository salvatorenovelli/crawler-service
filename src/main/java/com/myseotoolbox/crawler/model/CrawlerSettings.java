package com.myseotoolbox.crawler.model;

import lombok.Getter;

import static com.myseotoolbox.crawler.utils.EnsureRange.ensureRange;

@Getter
public class CrawlerSettings {

    public static final int MIN_CRAWL_INTERVAL = 1;
    public static final int MAX_CRAWL_INTERVAL = 365;
    public static final int MIN_CONCURRENT_CONNECTIONS = 1;
    public static final int MAX_CONCURRENT_CONNECTIONS = 5;

    private final int maxConcurrentConnections;
    private final boolean crawlEnabled;
    private final int crawlIntervalDays;

    public CrawlerSettings(Integer maxConcurrentConnections, boolean crawlEnabled, Integer crawlIntervalDays) {
        this.maxConcurrentConnections = ensureRange(maxConcurrentConnections, MIN_CONCURRENT_CONNECTIONS, MAX_CONCURRENT_CONNECTIONS);
        this.crawlEnabled = crawlEnabled;
        this.crawlIntervalDays = ensureRange(crawlIntervalDays, MIN_CRAWL_INTERVAL, MAX_CRAWL_INTERVAL);
    }
}
