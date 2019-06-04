package com.myseotoolbox.crawler.config;

import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Component
public class PageCrawlListener implements Consumer<PageSnapshot> {
    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;

    public PageCrawlListener(MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence) {
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
    }

    @Override
    public void accept(PageSnapshot snapshot) {
        runOrLogWarning(() -> monitoredUriUpdater.updateCurrentValue(snapshot), "Error while updating monitored uris for uri: " + snapshot.getUri());
        runOrLogWarning(() -> crawlPersistence.persistPageCrawl(snapshot), "Error while persisting crawl for uri: " + snapshot.getUri());
    }
}
