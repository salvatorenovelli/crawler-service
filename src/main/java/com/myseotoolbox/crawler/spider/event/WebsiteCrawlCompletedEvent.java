package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebsiteCrawlCompletedEvent {
    private SimpleWebsiteCrawl websiteCrawl;

    public WebsiteCrawlCompletedEvent(WebsiteCrawl websiteCrawl) {
        this.websiteCrawl = new SimpleWebsiteCrawl(websiteCrawl.getId().toHexString(), websiteCrawl.getOrigin());
    }
}

@AllArgsConstructor
@NoArgsConstructor
@Data
class SimpleWebsiteCrawl {
    private String id;
    private String origin;
}