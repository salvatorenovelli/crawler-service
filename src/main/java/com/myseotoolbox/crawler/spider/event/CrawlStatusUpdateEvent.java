package com.myseotoolbox.crawler.spider.event;

import lombok.Data;

@Data
public class CrawlStatusUpdateEvent {
    private final int visited;
    private final int pending;
}
