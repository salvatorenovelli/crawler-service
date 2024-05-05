package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;

import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class CrawlEventDispatchTest {

    public static final String TEST_ORIGIN = "http://origin";
    public static final PageSnapshot TEST_PAGE_SNAPSHOT = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri("http://host");
    public static final CrawlResult TEST_CRAWL_RESULT = CrawlResult.forSnapshot(TEST_PAGE_SNAPSHOT);
    public static final ObjectId TEST_CRAWL_ID = new ObjectId();
    private static final WebsiteCrawl CRAWL = WebsiteCrawlFactory.newWebsiteCrawlFor(TEST_CRAWL_ID, TEST_ORIGIN, Collections.emptyList());

    @Mock private ApplicationEventPublisher applicationEventPublisher;

    CrawlEventDispatch sut;

    @Before
    public void setUp() {
        sut = new CrawlEventDispatch(CRAWL, applicationEventPublisher);
    }

    @Test
    public void shouldNotifyPageCrawlListeners() {
        sut.onPageCrawled(TEST_CRAWL_RESULT);
        verify(applicationEventPublisher).publishEvent(new PageCrawledEvent(CRAWL, TEST_CRAWL_RESULT));
    }

    @Test
    public void shouldNotifyCrawlStartedListeners() {
        sut.onCrawlStarted();
        verify(applicationEventPublisher).publishEvent(new CrawlStartedEvent(CRAWL));
    }

    @Test
    public void shouldNotifyCrawlCompletedListeners() {
        sut.onCrawlCompleted();
        verify(applicationEventPublisher).publishEvent(new CrawlCompletedEvent(CRAWL));
    }

    @Test
    public void shouldNotifyForStatusUpdates() {
        sut.onCrawlStatusUpdate(10, 100);
        verify(applicationEventPublisher).publishEvent(new CrawlStatusUpdateEvent(10, 100, CRAWL.getId().toHexString()));
    }

}