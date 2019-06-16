package com.myseotoolbox.crawler.spider.configuration;

public class CrawlerSettingsBuilder {

    private int crawlIntervalDays = 1;
    private boolean ignoreRobotsTxt = false;
    private int maxConcurrentConnections = 1;
    private boolean crawlEnabled = true;

    public static CrawlerSettingsBuilder defaultSettings() {
        return new CrawlerSettingsBuilder();
    }

    public CrawlerSettings build() {
        return new CrawlerSettings(maxConcurrentConnections, crawlEnabled, crawlIntervalDays, new FilterConfiguration(ignoreRobotsTxt));
    }

    public CrawlerSettingsBuilder withIgnoreRobotsTxt(boolean b) {
        this.ignoreRobotsTxt = b;
        return this;
    }
}
