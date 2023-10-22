package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.pagelinks.OutboundLinkRepository;
import com.myseotoolbox.crawler.pagelinks.OutboundLinksPersistenceListener;
import com.myseotoolbox.crawler.spider.PubSubEventDispatch;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlEventDispatchFactory {

    private final MonitoredUriUpdater monitoredUriUpdater;
    private final OutboundLinkRepository outboundLinkRepository;
    private final WebsiteCrawlRepository websiteCrawlRepository;
    private final PubSubEventDispatch dispatch;

    public CrawlEventDispatch get(WebsiteCrawl crawl) {
        OutboundLinksPersistenceListener outLinkPersistenceListener = new OutboundLinksPersistenceListener(crawl.getId(), crawl.getOrigin(), outboundLinkRepository);
        return new CrawlEventDispatch(crawl, monitoredUriUpdater, outLinkPersistenceListener, websiteCrawlRepository, dispatch);
    }
}
