package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Data;

import java.time.Instant;

@Data
public class CrawlStatusUpdateEvent {
    private final WebsiteCrawl websiteCrawl;
    private final int visited;
    private final int pending;
    private final Instant timestamp;
}
