package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class CrawlEventDispatch {

    private final WebsiteCrawl websiteCrawl;
    private final ApplicationEventPublisher applicationEventPublisher;


    public void onPageCrawled(CrawlResult crawlResult) {
        log.debug("Persisting page crawled: {}", crawlResult.getUri());
        applicationEventPublisher.publishEvent(new PageCrawledEvent(websiteCrawl, crawlResult));
    }

    public void onCrawlStarted() {
        log.debug("Crawl started event for: {}", websiteCrawl);
        applicationEventPublisher.publishEvent(new CrawlStartedEvent(websiteCrawl));
    }

    public void onCrawlCompleted() {
        log.debug("Crawl completed event for: {}", websiteCrawl);
        applicationEventPublisher.publishEvent(new CrawlCompletedEvent(websiteCrawl));
    }

    public void crawlEnded() {
        pubSubEventDispatch.websiteCrawlCompletedEvent(websiteCrawl);
    }
}
