package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings;
import com.myseotoolbox.crawler.spider.configuration.FilterConfiguration;

public class CrawlerSettingsBuilder {

    private int crawlIntervalDays = 1;
    private boolean ignoreRobotsTxt = false;
    private int maxConcurrentConnections = 1;
    private boolean crawlEnabled = true;
    private int crawledPageLimit = DefaultCrawlerSettings.DEFAULT_MAX_URL_PER_CRAWL;
    private long crawlDelayMillis = DefaultCrawlerSettings.MIN_CRAWL_DELAY_MILLIS;

    public CrawlerSettingsBuilder() {
    }

    public CrawlerSettingsBuilder(CrawlerSettings settings) {
        this.maxConcurrentConnections = settings.getMaxConcurrentConnections();
        this.crawlEnabled = settings.isCrawlEnabled();
        this.crawlIntervalDays = settings.getCrawlIntervalDays();
        this.crawlDelayMillis = settings.getCrawlDelayMillis();
        this.crawledPageLimit = settings.getCrawledPageLimit();
    }

    public static CrawlerSettingsBuilder defaultSettings() {
        return new CrawlerSettingsBuilder();
    }

    public static CrawlerSettingsBuilder from(CrawlerSettings crawlerSettings) {
        return new CrawlerSettingsBuilder(crawlerSettings);
    }

    public CrawlerSettingsBuilder withMaxConcurrentConnections(int maxConcurrentConnections) {
        this.maxConcurrentConnections = maxConcurrentConnections;
        return this;
    }

    public CrawlerSettingsBuilder withCrawlEnabled(boolean crawlEnabled) {
        this.crawlEnabled = crawlEnabled;
        return this;
    }

    public CrawlerSettingsBuilder withCrawlIntervalDays(int crawlIntervalDays) {
        this.crawlIntervalDays = crawlIntervalDays;
        return this;
    }

    public CrawlerSettingsBuilder withCrawlDelayMillis(Long crawlDelayMillis) {
        this.crawlDelayMillis = crawlDelayMillis;
        return this;
    }

    public CrawlerSettingsBuilder withCrawledPageLimit(int crawledPageLimit) {
        this.crawledPageLimit = crawledPageLimit;
        return this;
    }


    public CrawlerSettingsBuilder withIgnoreRobotsTxt(boolean b) {
        this.ignoreRobotsTxt = b;
        return this;
    }

    public CrawlerSettings build() {
        return new CrawlerSettings(maxConcurrentConnections, crawlEnabled, crawlIntervalDays, crawlDelayMillis, new FilterConfiguration(ignoreRobotsTxt), crawledPageLimit);
    }
}
