package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Component
@Slf4j
public class PageCrawlListener implements Consumer<PageSnapshot> {
    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;

    public PageCrawlListener(MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence) {
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
    }

    @Override
    public void accept(PageSnapshot snapshot) {
        log.debug("Persisting page crawled: {}", snapshot.getUri());
        runOrLogWarning(() -> monitoredUriUpdater.updateCurrentValue(snapshot), "Error while updating monitored uris for uri: " + snapshot.getUri());
        runOrLogWarning(() -> crawlPersistence.persistPageCrawl(snapshot), "Error while persisting crawl for uri: " + snapshot.getUri());
    }
}
