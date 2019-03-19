package com.myseotoolbox.crawler.model;

import lombok.Data;

@Data
public class CrawlerSettings {
    private Integer maxConcurrentConnections;
    private boolean crawlEnabled;
    private int crawlIntervalDays;

    public CrawlerSettings(Integer maxConcurrentConnections, boolean crawlEnabled) {
        this.maxConcurrentConnections = maxConcurrentConnections != null ? maxConcurrentConnections : 1;
        this.crawlEnabled = crawlEnabled;
    }
}
