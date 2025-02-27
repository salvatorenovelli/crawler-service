package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.spider.event.WebsiteCrawlCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SitemapRepositoryGarbageCollectionListener {
    private final SitemapRepository sitemapRepository;

    @EventListener
    public void onWebsiteCrawlCompletedEvent(WebsiteCrawlCompletedEvent event) {
        sitemapRepository.purgeCrawl(event.getWebsiteCrawl());
    }
}
