package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.pagelinks.OutboundLinkRepository;
import com.myseotoolbox.crawler.pagelinks.OutboundLinksListener;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class CrawlEventsListenerFactory {

    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;
    private final OutboundLinkRepository outboundLinkRepository;
    private final WebsiteCrawlRepository websiteCrawlRepository;

    public CrawlEventsListenerFactory(MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence, OutboundLinkRepository outboundLinkRepository, WebsiteCrawlRepository websiteCrawlRepository) {
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
        this.outboundLinkRepository = outboundLinkRepository;
        this.websiteCrawlRepository = websiteCrawlRepository;
    }

    public CrawlEventListener getPageCrawlListener(ObjectId crawlId) {
        return new CrawlEventListener(crawlId, monitoredUriUpdater, crawlPersistence, new OutboundLinksListener(crawlId, outboundLinkRepository), websiteCrawlRepository);
    }
}
