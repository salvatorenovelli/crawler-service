package com.myseotoolbox.crawler.spider.event;


import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Data;

@Data
public class CrawlStartedEvent {
    private final WebsiteCrawl websiteCrawl;

    public static CrawlStartedEvent from(WebsiteCrawl websiteCrawl) {
        return new CrawlStartedEvent(websiteCrawl);
    }
}
