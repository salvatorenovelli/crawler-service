package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.pagelinks.OutboundLinksListener;
import com.myseotoolbox.crawler.websitecrawl.CrawlStartedEvent;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
public class CrawlEventListener {
    private final ObjectId crawlId;
    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;
    private final OutboundLinksListener linksListener;
    private final WebsiteCrawlRepository websiteCrawlRepository;

    public CrawlEventListener(ObjectId crawlId, MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence, OutboundLinksListener linksListener, WebsiteCrawlRepository websiteCrawlRepository) {
        this.crawlId = crawlId;
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
        this.linksListener = linksListener;
        this.websiteCrawlRepository = websiteCrawlRepository;
    }

    public void onPageCrawled(CrawlResult crawlResult) {
        PageSnapshot snapshot = crawlResult.getPageSnapshot();
        log.debug("Persisting page crawled: {}", snapshot.getUri());
        runOrLogWarning(() -> monitoredUriUpdater.updateCurrentValue(snapshot), "Error while updating monitored uris for uri: " + snapshot.getUri());
        runOrLogWarning(() -> crawlPersistence.persistPageCrawl(snapshot), "Error while persisting crawl for uri: " + snapshot.getUri());
        runOrLogWarning(() -> linksListener.accept(crawlResult), "Error while updating outbound links for uri: " + crawlResult.getUri());
    }

    public void onCrawlStart(CrawlStartedEvent event) {
        runOrLogWarning(() -> websiteCrawlRepository.save(WebsiteCrawl.fromCrawlStartedEvent(crawlId, event)), "Error while persisting CrawlStartedEvent: " + event);
    }
}
