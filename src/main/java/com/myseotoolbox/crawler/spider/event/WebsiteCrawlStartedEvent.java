package com.myseotoolbox.crawler.spider.event;


import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Data;

import java.time.Instant;

@Data
public class WebsiteCrawlStartedEvent {
    private final WebsiteCrawl websiteCrawl;
    private final Instant timestamp;

    public static WebsiteCrawlStartedEvent from(WebsiteCrawl websiteCrawl, Instant timestamp) {
        return new WebsiteCrawlStartedEvent(websiteCrawl, timestamp);
    }
}
