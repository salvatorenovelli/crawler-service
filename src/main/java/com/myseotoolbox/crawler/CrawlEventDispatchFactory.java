package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.pagelinks.OutboundLinkRepository;
import com.myseotoolbox.crawler.pagelinks.OutboundLinksPersistenceListener;
import com.myseotoolbox.crawler.spider.CrawlEventDispatch;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class CrawlEventDispatchFactory {

    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;
    private final OutboundLinkRepository outboundLinkRepository;
    private final WebsiteCrawlRepository websiteCrawlRepository;

    public CrawlEventDispatchFactory(MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence, OutboundLinkRepository outboundLinkRepository, WebsiteCrawlRepository websiteCrawlRepository) {
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
        this.outboundLinkRepository = outboundLinkRepository;
        this.websiteCrawlRepository = websiteCrawlRepository;
    }

    public CrawlEventDispatch get(ObjectId crawlId) {
        return new CrawlEventDispatch(crawlId, monitoredUriUpdater, crawlPersistence, new OutboundLinksPersistenceListener(crawlId, outboundLinkRepository), websiteCrawlRepository);
    }
}
