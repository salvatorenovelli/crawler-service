package com.myseotoolbox.crawler.spider.configuration;

import lombok.Getter;

import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.*;
import static com.myseotoolbox.crawler.utils.EnsureRange.ensureRange;

@Getter
public class CrawlerSettings {

    private final int maxConcurrentConnections;
    private final boolean crawlEnabled;
    /**
     * The delay between website crawls. For example, you want to crawl this website every 2 days, or weekly
     */
    private final int crawlIntervalDays;
    /**
     * The parameter to configure how quickly this crawler can request pages from a website.
     * For example, a crawl delay of 2000ms specifies that a crawler should not request a new page more than every 2 seconds.
     */
    private final Long crawlDelayMillis;
    private final FilterConfiguration filterConfiguration;
    private final int crawledPageLimit;

    public CrawlerSettings(Integer maxConcurrentConnections, boolean crawlEnabled, Integer crawlIntervalDays, Long crawlDelayMillis, FilterConfiguration filterConfiguration, Integer crawledPageLimit) {
        this.maxConcurrentConnections = ensureRange(maxConcurrentConnections, MIN_CONCURRENT_CONNECTIONS, MAX_CONCURRENT_CONNECTIONS);
        this.crawlEnabled = crawlEnabled;
        this.crawlIntervalDays = ensureRange(crawlIntervalDays, MIN_CRAWL_INTERVAL, MAX_CRAWL_INTERVAL);
        this.crawlDelayMillis = ensureRange(crawlDelayMillis, MIN_CRAWL_DELAY_MILLIS, MAX_CRAWL_DELAY_MILLIS);
        this.filterConfiguration = filterConfiguration;
        this.crawledPageLimit = crawledPageLimit != null ? crawledPageLimit : DEFAULT_MAX_URL_PER_CRAWL;
    }
}
