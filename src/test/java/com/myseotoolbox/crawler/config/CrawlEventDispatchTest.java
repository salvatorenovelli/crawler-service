package com.myseotoolbox.crawler.config;

import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.pagelinks.OutboundLinksPersistenceListener;
import com.myseotoolbox.crawler.spider.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.PubSubEventDispatch;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.crawler.websitecrawl.CrawlStartedEvent;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class CrawlEventDispatchTest {

    public static final String TEST_ORIGIN = "http://origin" ;
    public static final URI CRAWL_ORIGIN = URI.create(TEST_ORIGIN);
    public static final CrawlStartedEvent CRAWL_STARTED_EVENT = new CrawlStartedEvent(TEST_ORIGIN, Arrays.asList("/one", "/two"));

    public static final PageSnapshot TEST_PAGE_SNAPSHOT = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri("http://host");
    public static final CrawlResult TEST_CRAWL_RESULT = CrawlResult.forSnapshot(CRAWL_ORIGIN, TEST_PAGE_SNAPSHOT);
    public static final ObjectId TEST_CRAWL_ID = new ObjectId();
    public static final String TEST_CRAWL_ID_STR = TEST_CRAWL_ID.toHexString();
    private static final WebsiteCrawl CRAWL = new WebsiteCrawl(TEST_CRAWL_ID, TEST_ORIGIN, LocalDateTime.now(), Collections.emptyList());
    @Mock private PageCrawlPersistence crawlPersistence;
    @Mock private MonitoredUriUpdater monitoredUriUpdater;
    @Mock private OutboundLinksPersistenceListener linksListener;
    @Mock private WebsiteCrawlRepository websiteCrawlRepository;
    @Mock private PubSubEventDispatch dispatch;

    CrawlEventDispatch sut;

    @Before
    public void setUp() {
        sut = new CrawlEventDispatch(CRAWL, monitoredUriUpdater, crawlPersistence, linksListener, websiteCrawlRepository, dispatch);
    }

    @Test
    public void shouldNotifyPageCrawlListeners() {
        sut.pageCrawled(TEST_CRAWL_RESULT);
        verify(monitoredUriUpdater).updateCurrentValue(CRAWL, TEST_PAGE_SNAPSHOT);
        verify(crawlPersistence).persistPageCrawl(TEST_CRAWL_ID_STR, TEST_PAGE_SNAPSHOT);
        verify(linksListener).accept(TEST_CRAWL_RESULT);
    }

    @Test
    public void shouldNotifyCrawlStartedListeners() {
        sut.crawlStarted(CRAWL_STARTED_EVENT);
        verify(websiteCrawlRepository).save(argThat(event -> {
            assertThat(event.getId(), is(TEST_CRAWL_ID));
            assertThat(event.getOrigin(), is(TEST_ORIGIN));
            assertThat(event.getSeeds(), containsInAnyOrder("/one", "/two"));
            return true;
        }));
    }

    @Test
    public void exceptionsInMonitoredUriUpdaterDoesNotPreventPersistenceOfCrawl() {
        doThrow(new RuntimeException("This should not prevent update of the other")).when(monitoredUriUpdater).updateCurrentValue(any(), any());
        doThrow(new RuntimeException("This should not prevent update of the other")).when(linksListener).accept(any());
        sut.pageCrawled(TEST_CRAWL_RESULT);
        verify(crawlPersistence).persistPageCrawl(TEST_CRAWL_ID_STR, TEST_PAGE_SNAPSHOT);
    }

    @Test
    public void exceptionsInpageCrawlPersistenceDoesNotPreventMonitoredUriUpdate() {
        doThrow(new RuntimeException("This should not prevent update of the other")).when(crawlPersistence).persistPageCrawl(any(), any());
        doThrow(new RuntimeException("This should not prevent update of the other")).when(linksListener).accept(any());
        sut.pageCrawled(TEST_CRAWL_RESULT);
        verify(monitoredUriUpdater).updateCurrentValue(CRAWL, TEST_PAGE_SNAPSHOT);
    }

    @Test
    public void exceptionsInLinksListenerDoesNotPreventMonitoredUriUpdate() {
        doThrow(new RuntimeException("This should not prevent update of the other")).when(crawlPersistence).persistPageCrawl(any(), any());
        doThrow(new RuntimeException("This should not prevent update of the other")).when(monitoredUriUpdater).updateCurrentValue(any(), any());
        sut.pageCrawled(TEST_CRAWL_RESULT);
        verify(linksListener).accept(TEST_CRAWL_RESULT);
    }

    @Test
    public void exceptionsInPageCrawlStartedShouldNotPropagate() {
        doThrow(new RuntimeException("This should not propagate")).when(websiteCrawlRepository).save(any());
        sut.crawlStarted(CRAWL_STARTED_EVENT);
    }

    @Test
    public void shouldPublishCrawlEndedOnPubSub() {
        sut.crawlEnded();
        verify(dispatch).crawlCompletedEvent(CRAWL);
    }
}