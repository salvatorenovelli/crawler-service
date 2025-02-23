package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.TestTimeUtils;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CrawlEventDispatchTest {

    public static final String TEST_ORIGIN = "http://origin";
    public static final URI EXPECTED_SITEMAP_LINK = URI.create(TEST_ORIGIN + "/sitemap.xml");
    public static final PageSnapshot TEST_PAGE_SNAPSHOT = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri("http://host");
    public static final CrawlResult TEST_CRAWL_RESULT = CrawlResult.forSnapshot(TEST_PAGE_SNAPSHOT);
    private static final WebsiteCrawl CRAWL = TestWebsiteCrawlFactory.newWebsiteCrawlFor(TEST_ORIGIN, Collections.emptyList());

    @Mock private ApplicationEventPublisher applicationEventPublisher;
    @Mock private PageCrawledEventFactory pageCrawledEventFactory;

    CrawlEventDispatch sut;

    @Before
    public void setUp() {
        sut = new CrawlEventDispatch(CRAWL, applicationEventPublisher, new TestTimeUtils(), pageCrawledEventFactory);
        when(pageCrawledEventFactory.make(CRAWL, TEST_CRAWL_RESULT)).thenReturn(new PageCrawledEvent(CRAWL, TEST_CRAWL_RESULT, Set.of(EXPECTED_SITEMAP_LINK)));
    }

    @Test
    public void shouldNotifyPageCrawlListeners() {
        sut.onPageCrawled(TEST_CRAWL_RESULT);
        verify(applicationEventPublisher).publishEvent(new PageCrawledEvent(CRAWL, TEST_CRAWL_RESULT, Set.of(EXPECTED_SITEMAP_LINK)));
    }

    @Test
    public void shouldNotifyCrawlStartedListeners() {
        sut.onCrawlStarted();
        verify(applicationEventPublisher).publishEvent(WebsiteCrawlStartedEvent.from(CRAWL, Instant.EPOCH));
    }

    @Test
    public void shouldNotifyCrawlCompletedListeners() {
        sut.onCrawlCompleted(5);
        verify(applicationEventPublisher).publishEvent(new WebsiteCrawlCompletedEvent(CRAWL, 5, Instant.EPOCH));
    }

    @Test
    public void shouldNotifyForStatusUpdates() {
        sut.onCrawlStatusUpdate(10, 100);
        verify(applicationEventPublisher).publishEvent(new CrawlStatusUpdateEvent(CRAWL, 10, 100, Instant.EPOCH));
    }

}