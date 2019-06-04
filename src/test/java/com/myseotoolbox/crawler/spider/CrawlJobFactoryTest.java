package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.config.PageCrawlListener;
import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.SnapshotResult;
import com.myseotoolbox.crawler.spider.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrawlJobFactoryTest {

    private static final int MAX_CRAWLS = 100;
    private static final List<String> SITEMAPS_FROM_ROBOTS = Collections.singletonList("http://localhost/sitemap.xml");
    private static List<URI> ONLY_ROOT = Collections.singletonList(URI.create("http://host/"));
    private static final int SINGLE_THREAD = 1;
    private static final URI TEST_ORIGIN = URI.create("http://host/");
    private static final URI TEST_FILTERED_LINK = TEST_ORIGIN.resolve("base-path/path");

    @Mock private UriFilter testFilter;
    @Mock private WebPageReader reader;
    @Mock private WebsiteUriFilterFactory filtersBuilder;
    @Mock private RobotsTxt mockRobotsTxt;
    @Mock private PageCrawlListener listener;

    private CrawlExecutorFactory crawlExecutorFactory = new CurrentThreadCrawlExecutorFactory();

    CrawlJobFactory sut;
    private RobotsTxtFactory robotsTxtFactory = new RobotsTxtFactory() {
        @Override
        public RobotsTxt buildRobotsTxtFor(URI websiteOrigin) {
            return mockRobotsTxt;
        }
    };


    @Before
    public void setUp() throws Exception {

        when(filtersBuilder.build(Mockito.any(), Mockito.anyList(), any(RobotsTxt.class))).thenReturn(testFilter);
        when(reader.snapshotPage(any())).thenAnswer(this::buildSnapshotForUri);
        when(mockRobotsTxt.getSitemaps()).thenReturn(SITEMAPS_FROM_ROBOTS);


        when(testFilter.shouldCrawl(TEST_ORIGIN, TEST_FILTERED_LINK)).then(invocation -> false);

        sut = new CrawlJobFactory(mockWebPageReaderFactory(), filtersBuilder, crawlExecutorFactory, robotsTxtFactory);
    }


    @Test
    public void shouldOnlyCrawlOnlyFromTheSeeds() throws SnapshotException {
        CrawlJob job = sut.build(TEST_ORIGIN, seeds("/path1", "/path2"), SINGLE_THREAD, MAX_CRAWLS, listener);
        job.start();

        verify(reader).snapshotPage(TEST_ORIGIN.resolve("/path1"));
        verify(reader).snapshotPage(TEST_ORIGIN.resolve("/path2"));
        verifyNoMoreInteractions(reader);
    }


    @Test
    public void shouldNotifyMonitoredUriUpdater() {
        CrawlJob job = sut.build(TEST_ORIGIN, ONLY_ROOT, SINGLE_THREAD, MAX_CRAWLS, listener);
        job.start();
        verify(listener).accept(argThat(snapshot -> snapshot.getUri().equals(TEST_ORIGIN.toString())));
    }


    @Test
    public void shouldFilterAsSpecified() throws SnapshotException {
        CrawlJob job = sut.build(TEST_ORIGIN, ONLY_ROOT, SINGLE_THREAD, MAX_CRAWLS, listener);
        job.start();

        verify(testFilter).shouldCrawl(TEST_ORIGIN, TEST_FILTERED_LINK);
        verify(reader).snapshotPage(TEST_ORIGIN);
        verifyNoMoreInteractions(reader);
    }

    //Execute in the test thread instead of spawning a new one
    private class CurrentThreadCrawlExecutorFactory extends CrawlExecutorFactory {

        @Override
        public ThreadPoolExecutor buildExecutor(String namePostfix, int concurrentConnections) {
            return new CurrentThreadTestExecutorService();
        }
    }

    private SnapshotResult buildSnapshotForUri(InvocationOnMock invocation) {
        String uri = invocation.getArgument(0).toString();
        PageSnapshot snapshot = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(uri);
        snapshot.setLinks(Arrays.asList(TEST_FILTERED_LINK.toString()));
        return SnapshotResult.forSnapshot(snapshot);
    }

    private WebPageReaderFactory mockWebPageReaderFactory() {

        return new WebPageReaderFactory() {
            @Override
            public WebPageReader build(UriFilter uriFilter) {
                return reader;
            }
        };
    }

    private List<URI> seeds(String... seeds) {
        return Arrays.stream(seeds).map(TEST_ORIGIN::resolve).collect(Collectors.toList());
    }
}
