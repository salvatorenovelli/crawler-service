package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlEventDispatchFactory {
    private final ApplicationEventPublisher eventPublisher;
    public CrawlEventDispatch get(WebsiteCrawl crawl) {
        return new CrawlEventDispatch(crawl, eventPublisher);
    }
}
