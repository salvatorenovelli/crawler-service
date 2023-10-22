package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.pagelinks.OutboundLinksPersistenceListener;
import com.myseotoolbox.crawler.spider.PubSubEventDispatch;
import com.myseotoolbox.crawler.websitecrawl.CrawlStartedEvent;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
@RequiredArgsConstructor
public class CrawlEventDispatch {

    private final WebsiteCrawl websiteCrawl;
    private final MonitoredUriUpdater monitoredUriUpdater;
    private final OutboundLinksPersistenceListener outLinkPersistenceListener;
    private final WebsiteCrawlRepository websiteCrawlRepository;
    private final PubSubEventDispatch pubSubEventDispatch;


    public void pageCrawled(CrawlResult crawlResult) {
        PageSnapshot snapshot = crawlResult.getPageSnapshot();
        log.debug("Persisting page crawled: {}", snapshot.getUri());
        runOrLogWarning(() -> monitoredUriUpdater.updateCurrentValue(websiteCrawl, snapshot), "Error while updating monitored uris for uri: " + snapshot.getUri());
        runOrLogWarning(() -> pubSubEventDispatch.pageCrawlCompletedEvent(websiteCrawl.getId().toHexString(), snapshot), "Error while persisting crawl for uri: " + snapshot.getUri());
        runOrLogWarning(() -> outLinkPersistenceListener.accept(crawlResult), "Error while persisting outbound links for uri: " + crawlResult.getUri());
    }

    public void crawlStarted(CrawlStartedEvent event) {
        runOrLogWarning(() -> websiteCrawlRepository.save(WebsiteCrawl.fromCrawlStartedEvent(websiteCrawl.getId(), event)), "Error while persisting CrawlStartedEvent: " + event);
    }

    public void crawlEnded() {
        pubSubEventDispatch.websiteCrawlCompletedEvent(websiteCrawl);
    }
}
