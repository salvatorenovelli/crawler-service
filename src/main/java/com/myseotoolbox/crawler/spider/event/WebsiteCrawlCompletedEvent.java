package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
public class WebsiteCrawlCompletedEvent {
    private final WebsiteCrawl websiteCrawl;
    private final Instant timestamp;
}