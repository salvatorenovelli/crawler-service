package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
public class CrawlEventDispatch {

    private final WebsiteCrawl websiteCrawl;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TimeUtils timeUtils;

    public void onPageCrawled(CrawlResult crawlResult) {
        log.debug("Persisting page crawled: {} for crawl: {}", crawlResult.getUri(), websiteCrawl);
        applicationEventPublisher.publishEvent(new PageCrawledEvent(websiteCrawl, crawlResult));
    }

    public void onCrawlStarted() {
        log.debug("Crawl started event for: {}", websiteCrawl);
        applicationEventPublisher.publishEvent(WebsiteCrawlStartedEvent.from(websiteCrawl, timeUtils.now()));
    }

    public void onCrawlCompleted(int visited, int pending) {
        log.debug("Crawl completed event for: {}", websiteCrawl);
        applicationEventPublisher.publishEvent(new WebsiteCrawlCompletedEvent(websiteCrawl, visited, pending, timeUtils.now()));
    }

    public void onCrawlStatusUpdate(int visited, int pending) {
        log.trace("Crawl status update event for: {}. visited:{} pending:{}", websiteCrawl, visited, pending);
        applicationEventPublisher.publishEvent(new CrawlStatusUpdateEvent(websiteCrawl, visited, pending, timeUtils.now()));
    }
}
