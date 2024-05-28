package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlEventDispatchFactory {
    private final ApplicationEventPublisher eventPublisher;
    private final TimeUtils timeUtils;

    public CrawlEventDispatch get(WebsiteCrawl crawl) {
        return new CrawlEventDispatch(crawl, eventPublisher, timeUtils);
    }
}
