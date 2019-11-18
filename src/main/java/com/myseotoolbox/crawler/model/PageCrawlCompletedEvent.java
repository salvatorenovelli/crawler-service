package com.myseotoolbox.crawler.model;

import lombok.Data;

@Data
public class PageCrawlCompletedEvent {
    private final String websiteCrawlId;
    private final PageSnapshot curVal;
}
