package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.outboundlink.OutboundLinksListener;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
public class PageCrawlListener implements Consumer<CrawlResult> {
    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;
    private final OutboundLinksListener linksListener;

    public PageCrawlListener(MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence, OutboundLinksListener linksListener) {
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
        this.linksListener = linksListener;
    }

    @Override
    public void accept(CrawlResult crawlResult) {
        PageSnapshot snapshot = crawlResult.getPageSnapshot();
        log.debug("Persisting page crawled: {}", snapshot.getUri());
        runOrLogWarning(() -> monitoredUriUpdater.updateCurrentValue(snapshot), "Error while updating monitored uris for uri: " + snapshot.getUri());
        runOrLogWarning(() -> crawlPersistence.persistPageCrawl(snapshot), "Error while persisting crawl for uri: " + snapshot.getUri());
        runOrLogWarning(() -> linksListener.accept(crawlResult), "Error while updating outbound links for uri: " + crawlResult.getUri());
    }
}
