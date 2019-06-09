package com.myseotoolbox.crawler.config;

import com.myseotoolbox.crawler.PageCrawlListener;
import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class PageCrawlListenerTest {


    public static final PageSnapshot TEST_PAGE_SNAPSHOT = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri("http://host");
    @Mock private PageCrawlPersistence crawlPersistence;
    @Mock private MonitoredUriUpdater monitoredUriUpdater;

    PageCrawlListener sut;

    @Before
    public void setUp() {
        sut = new PageCrawlListener(monitoredUriUpdater, crawlPersistence);
    }

    @Test
    public void shouldNotify() {
        sut.accept(TEST_PAGE_SNAPSHOT);
        verify(monitoredUriUpdater).updateCurrentValue(TEST_PAGE_SNAPSHOT);
        verify(crawlPersistence).persistPageCrawl(TEST_PAGE_SNAPSHOT);
    }

    @Test
    public void exceptionsInMonitoredUriUpdaterDoesNotPreventPersistenceOfCrawl() {
        doThrow(new RuntimeException("This should not prevent update of the other")).when(monitoredUriUpdater).updateCurrentValue(any());
        sut.accept(TEST_PAGE_SNAPSHOT);
        verify(crawlPersistence).persistPageCrawl(TEST_PAGE_SNAPSHOT);
    }

    @Test
    public void exceptionsInpageCrawlPersistenceDoesNotPreventMonitoredUriUpdate() {
        doThrow(new RuntimeException("This should not prevent update of the other")).when(crawlPersistence).persistPageCrawl(any());
        sut.accept(TEST_PAGE_SNAPSHOT);
        verify(monitoredUriUpdater).updateCurrentValue(TEST_PAGE_SNAPSHOT);
    }
}