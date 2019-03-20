package com.myseotoolbox.crawler.model;

import lombok.Data;

@Data
public class CrawlerSettings {

    public static final int MIN_CRAWL_INTERVAL = 0;
    public static final int MAX_CRAWL_INTERVAL = 365;
    public static final int MIN_CONCURRENT_CONNECTIONS = 1;
    public static final int MAX_CONCURRENT_CONNECTIONS = 5;

    private int maxConcurrentConnections;
    private boolean crawlEnabled;
    private int crawlIntervalDays;

    public CrawlerSettings(int maxConcurrentConnections, boolean crawlEnabled, int crawlIntervalDays) {
        this.maxConcurrentConnections = maxConcurrentConnections;
        this.crawlEnabled = crawlEnabled;
        this.crawlIntervalDays = crawlIntervalDays;
    }
}
