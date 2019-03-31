package com.myseotoolbox.crawler.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import static com.myseotoolbox.crawler.utils.EnsureRange.ensureRange;

@Getter@Setter@EqualsAndHashCode
public class CrawlerSettings {

    public static final int MIN_CRAWL_INTERVAL = 1;
    public static final int MAX_CRAWL_INTERVAL = 365;
    public static final int MIN_CONCURRENT_CONNECTIONS = 1;
    public static final int MAX_CONCURRENT_CONNECTIONS = 5;

    private int maxConcurrentConnections;
    private boolean crawlEnabled;
    private int crawlIntervalDays;

    public CrawlerSettings(Integer maxConcurrentConnections, boolean crawlEnabled, Integer crawlIntervalDays) {
        this.maxConcurrentConnections = ensureRange(maxConcurrentConnections, MIN_CONCURRENT_CONNECTIONS, MAX_CONCURRENT_CONNECTIONS);
        this.crawlEnabled = crawlEnabled;
        this.crawlIntervalDays = ensureRange(crawlIntervalDays, MIN_CRAWL_INTERVAL, MAX_CRAWL_INTERVAL);
    }
}
