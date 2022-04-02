package com.myseotoolbox.crawler.spider.configuration;

import lombok.Getter;

import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.*;
import static com.myseotoolbox.crawler.utils.EnsureRange.ensureRange;

@Getter
public class CrawlerSettings {

    private final int maxConcurrentConnections;
    private final boolean crawlEnabled;
    private final int crawlIntervalDays;
    private final FilterConfiguration filterConfiguration;
    private final int crawledPageLimit;

    public CrawlerSettings(Integer maxConcurrentConnections, boolean crawlEnabled, Integer crawlIntervalDays, FilterConfiguration filterConfiguration, Integer crawledPageLimit) {
        this.maxConcurrentConnections = ensureRange(maxConcurrentConnections, MIN_CONCURRENT_CONNECTIONS, MAX_CONCURRENT_CONNECTIONS);
        this.crawlEnabled = crawlEnabled;
        this.crawlIntervalDays = ensureRange(crawlIntervalDays, MIN_CRAWL_INTERVAL, MAX_CRAWL_INTERVAL);
        this.filterConfiguration = filterConfiguration;
        this.crawledPageLimit = crawledPageLimit != null ? crawledPageLimit : DEFAULT_MAX_URL_PER_CRAWL;
    }
}
