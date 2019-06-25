package com.myseotoolbox.crawler.config;

import com.myseotoolbox.crawler.CrawlEventListener;
import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.outboundlink.OutboundLinksListener;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.crawler.websitecrawl.CrawlStartedEvent;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class CrawlEventListenerTest {

    public static final String TEST_ORIGIN = "http://origin";
    public static final CrawlStartedEvent CRAWL_STARTED_EVENT = new CrawlStartedEvent(TEST_ORIGIN, Arrays.asList("/one", "/two"));

    public static final PageSnapshot TEST_PAGE_SNAPSHOT = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri("http://host");
    public static final CrawlResult TEST_CRAWL_RESULT = CrawlResult.forSnapshot(TEST_PAGE_SNAPSHOT);
    public static final ObjectId TEST_CRAWL_ID = new ObjectId();
    @Mock private PageCrawlPersistence crawlPersistence;
    @Mock private MonitoredUriUpdater monitoredUriUpdater;
    @Mock private OutboundLinksListener linksListener;
    @Mock private WebsiteCrawlRepository websiteCrawlRepository;

    CrawlEventListener sut;

    @Before
    public void setUp() {
        sut = new CrawlEventListener(TEST_CRAWL_ID, monitoredUriUpdater, crawlPersistence, linksListener, websiteCrawlRepository);
    }

    @Test
    public void shouldNotifyPageCrawlListeners() {
        sut.onPageCrawled(TEST_CRAWL_RESULT);
        verify(monitoredUriUpdater).updateCurrentValue(TEST_PAGE_SNAPSHOT);
        verify(crawlPersistence).persistPageCrawl(TEST_PAGE_SNAPSHOT);
        verify(linksListener).accept(TEST_CRAWL_RESULT);
    }

    @Test
    public void shouldNotifyCrawlStartedListeners() {
        sut.onCrawlStart(CRAWL_STARTED_EVENT);
        verify(websiteCrawlRepository).save(argThat(event -> {
            assertThat(event.getId(), is(TEST_CRAWL_ID));
            assertThat(event.getOrigin(), is(TEST_ORIGIN));
            assertThat(event.getSeeds(), containsInAnyOrder("/one", "/two"));
            return true;
        }));
    }

    @Test
    public void exceptionsInMonitoredUriUpdaterDoesNotPreventPersistenceOfCrawl() {
        doThrow(new RuntimeException("This should not prevent update of the other")).when(monitoredUriUpdater).updateCurrentValue(any());
        doThrow(new RuntimeException("This should not prevent update of the other")).when(linksListener).accept(any());
        sut.onPageCrawled(TEST_CRAWL_RESULT);
        verify(crawlPersistence).persistPageCrawl(TEST_PAGE_SNAPSHOT);
    }

    @Test
    public void exceptionsInpageCrawlPersistenceDoesNotPreventMonitoredUriUpdate() {
        doThrow(new RuntimeException("This should not prevent update of the other")).when(crawlPersistence).persistPageCrawl(any());
        doThrow(new RuntimeException("This should not prevent update of the other")).when(linksListener).accept(any());
        sut.onPageCrawled(TEST_CRAWL_RESULT);
        verify(monitoredUriUpdater).updateCurrentValue(TEST_PAGE_SNAPSHOT);
    }

    @Test
    public void exceptionsInLinksListenerDoesNotPreventMonitoredUriUpdate() {
        doThrow(new RuntimeException("This should not prevent update of the other")).when(crawlPersistence).persistPageCrawl(any());
        doThrow(new RuntimeException("This should not prevent update of the other")).when(monitoredUriUpdater).updateCurrentValue(any());
        sut.onPageCrawled(TEST_CRAWL_RESULT);
        verify(linksListener).accept(TEST_CRAWL_RESULT);
    }

    @Test
    public void exceptionsInPageCrawlStartedShouldNotPropagate() {
        doThrow(new RuntimeException("This should not propagate")).when(websiteCrawlRepository).save(any());
        sut.onCrawlStart(CRAWL_STARTED_EVENT);
    }
}