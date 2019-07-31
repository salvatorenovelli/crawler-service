package com.myseotoolbox.crawler.spider.configuration;

import lombok.Getter;

import static com.myseotoolbox.crawler.utils.EnsureRange.ensureRange;

@Getter
public class CrawlerSettings {

    public static final int MIN_CRAWL_INTERVAL = 1;
    public static final int MAX_CRAWL_INTERVAL = 365;
    public static final int MIN_CONCURRENT_CONNECTIONS = 1;
    public static final int MAX_CONCURRENT_CONNECTIONS = 5;
    public static final int DEFAULT_MAX_URL_PER_CRAWL = 10000;
    public static final int DEFAULT_CONCURRENT_CONNECTIONS = 1;

    private final int maxConcurrentConnections;
    private final boolean crawlEnabled;
    private final int crawlIntervalDays;
    private final FilterConfiguration filterConfiguration;

    public CrawlerSettings(Integer maxConcurrentConnections, boolean crawlEnabled, Integer crawlIntervalDays, FilterConfiguration filterConfiguration) {
        this.maxConcurrentConnections = ensureRange(maxConcurrentConnections, MIN_CONCURRENT_CONNECTIONS, MAX_CONCURRENT_CONNECTIONS);
        this.crawlEnabled = crawlEnabled;
        this.crawlIntervalDays = ensureRange(crawlIntervalDays, MIN_CRAWL_INTERVAL, MAX_CRAWL_INTERVAL);
        this.filterConfiguration = filterConfiguration;
    }
}
