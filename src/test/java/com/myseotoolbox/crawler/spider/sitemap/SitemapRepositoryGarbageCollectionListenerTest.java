package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.spider.event.WebsiteCrawlCompletedEvent;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Set;

import static com.myseotoolbox.testutils.TestWebsiteCrawlFactory.newWebsiteCrawlFor;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SitemapRepositoryGarbageCollectionListenerTest {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean private SitemapRepository sitemapRepository;

    @Test
    void shouldCallPurgeCrawlWhenCrawlIsCompleted() {
        WebsiteCrawl websiteCrawl = newWebsiteCrawlFor("http://origin", Set.of());
        WebsiteCrawlCompletedEvent event = new WebsiteCrawlCompletedEvent(websiteCrawl, 3, Instant.EPOCH);

        eventPublisher.publishEvent(event);

        verify(sitemapRepository, timeout(1000)).purgeCrawl(websiteCrawl);
    }
}