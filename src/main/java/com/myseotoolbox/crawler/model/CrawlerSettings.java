package com.myseotoolbox.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CrawlerSettings {
    private int maxConcurrentConnections = 1;
    private boolean crawlEnabled;
}
