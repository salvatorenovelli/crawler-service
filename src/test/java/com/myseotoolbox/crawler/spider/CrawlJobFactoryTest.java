package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawlJobFactoryTest {

    public static final int SINGLE_THREAD = 1;

    @Mock private UriFilter testFilter;
    @Mock private WebPageReader reader;
    @Mock private WebsiteUriFilterBuilder filtersBuilder;
    @Mock private MonitoredUriUpdater monitoredUriUpdater;
    @Mock private PageCrawlPersistence crawlPersistence;


    CrawlJobFactory sut;
    private ExecutorBuilder executorBuilder = new CurrentThreadExecutorBuilder();
    public static final URI TEST_ORIGIN = URI.create("http://host/base-path/");


    @Before
    public void setUp() throws Exception {

        when(filtersBuilder.buildForOrigin(Mockito.any())).thenReturn(testFilter);
        when(reader.snapshotPage(any())).thenAnswer(invocation -> PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(invocation.getArgument(0).toString()));

        sut = new CrawlJobFactory(reader, filtersBuilder, executorBuilder, monitoredUriUpdater, crawlPersistence);
    }

    @Test
    public void shouldCrawlProvidedOrigin() throws SnapshotException {
        CrawlJob job = sut.build(TEST_ORIGIN, SINGLE_THREAD);
        job.start();
        verify(reader).snapshotPage(TEST_ORIGIN);
    }

    @Test
    public void shouldNotifyMonitoredUriUpdater() {
        CrawlJob job = sut.build(TEST_ORIGIN, SINGLE_THREAD);
        job.start();
        verify(monitoredUriUpdater).updateCurrentValue(argThat(snapshot -> snapshot.getUri().equals(TEST_ORIGIN.toString())));
    }

    @Test
    public void shouldNotifyCrawlPersistence() {
        CrawlJob job = sut.build(TEST_ORIGIN, SINGLE_THREAD);
        job.start();
        verify(crawlPersistence).persistPageCrawl(argThat(snapshot -> snapshot.getUri().equals(TEST_ORIGIN.toString())));
    }


    //Execute in the test thread instead of spawning a new one
    private class CurrentThreadExecutorBuilder extends ExecutorBuilder {
        @Override
        public ExecutorService buildExecutor(int concurrentConnections) {
            return new CurrentThreadTestExecutorService();
        }
    }
}
