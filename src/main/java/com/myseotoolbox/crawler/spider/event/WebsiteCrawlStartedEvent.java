package com.myseotoolbox.crawler.spider.event;


import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Data;

@Data
public class WebsiteCrawlStartedEvent {
    private final WebsiteCrawl websiteCrawl;

    public static WebsiteCrawlStartedEvent from(WebsiteCrawl websiteCrawl) {
        return new WebsiteCrawlStartedEvent(websiteCrawl);
    }
}
