package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
@Component
@RequiredArgsConstructor
public class PageCrawledEventMonitoredUriUpdaterListener {
    private final MonitoredUriUpdater monitoredUriUpdater;

    @EventListener
    public void onPageCrawledEvent(PageCrawledEvent event) {
        PageSnapshot snapshot = event.getCrawlResult().getPageSnapshot();
        WebsiteCrawl websiteCrawl = event.getWebsiteCrawl();
        Set<URI> sitemapInboundLinks = event.getSitemapInboundLinks();
        runOrLogWarning(() -> monitoredUriUpdater.updateCurrentValue(websiteCrawl, snapshot, sitemapInboundLinks), "Error while updating monitored uris for uri: " + snapshot.getUri());
    }
}
