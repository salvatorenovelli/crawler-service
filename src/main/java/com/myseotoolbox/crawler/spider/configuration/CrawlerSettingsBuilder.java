package com.myseotoolbox.crawler.spider.configuration;

public class CrawlerSettingsBuilder {

    private int crawlIntervalDays = 1;
    private boolean ignoreRobotsTxt = false;
    private int maxConcurrentConnections = 1;
    private boolean crawlEnabled = true;
    private int crawledPageLimit = DefaultCrawlerSettings.DEFAULT_MAX_URL_PER_CRAWL;

    public static CrawlerSettingsBuilder defaultSettings() {
        return new CrawlerSettingsBuilder();
    }

    public CrawlerSettings build() {
        return new CrawlerSettings(maxConcurrentConnections, crawlEnabled, crawlIntervalDays, new FilterConfiguration(ignoreRobotsTxt), crawledPageLimit);
    }

    public CrawlerSettingsBuilder withIgnoreRobotsTxt(boolean b) {
        this.ignoreRobotsTxt = b;
        return this;
    }
}
