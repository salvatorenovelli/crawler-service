package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.outboundlink.OutboundLinkRepository;
import com.myseotoolbox.crawler.outboundlink.OutboundLinksListener;
import org.springframework.stereotype.Component;

@Component
public class PageCrawlListenerFactory {

    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;
    private final OutboundLinkRepository outboundLinkRepository;

    public PageCrawlListenerFactory(MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence, OutboundLinkRepository outboundLinkRepository) {
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
        this.outboundLinkRepository = outboundLinkRepository;
    }

    public PageCrawlListener getPageCrawlListener(String crawlId) {
        return new PageCrawlListener(monitoredUriUpdater, crawlPersistence, new OutboundLinksListener(crawlId, outboundLinkRepository));
    }
}
