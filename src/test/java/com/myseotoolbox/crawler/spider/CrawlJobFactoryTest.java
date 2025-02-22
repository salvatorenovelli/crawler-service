package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.config.WebPageReaderFactory;
import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.pagelinks.PageLink;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.spider.sitemap.SitemapCrawlResult;
import com.myseotoolbox.crawler.spider.sitemap.SitemapService;
import com.myseotoolbox.crawler.spider.sitemap.TestSitemapCrawlResultBuilder;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrawlJobFactoryTest {

    private static final List<String> SITEMAPS_FROM_ROBOTS = Collections.singletonList("http://localhost/sitemap.xml");
    private static List<URI> ONLY_ROOT = Collections.singletonList(URI.create("http://host/"));
    private static final URI TEST_ORIGIN = URI.create("http://host/");
    private static final URI TEST_FILTERED_LINK = TEST_ORIGIN.resolve("base-path/path");

    @Mock private WebPageReader reader;
    @Spy private WebsiteUriFilterFactory filtersFactory = new WebsiteUriFilterFactory();
    @Mock private RobotsTxt mockRobotsTxt;
    @Mock private CrawlEventDispatch dispatch;
    @Mock private SitemapService sitemapService;


    private CrawlJobFactory sut;
    private CrawlJobConfiguration.Builder testConf;

    @Before
    public void setUp() throws Exception {
        when(reader.snapshotPage(any())).thenAnswer(this::buildSnapshotForUri);
        when(mockRobotsTxt.getSitemaps()).thenReturn(SITEMAPS_FROM_ROBOTS);
        when(sitemapService.fetchSeedsFromSitemaps(any(), any())).thenReturn(TestSitemapCrawlResultBuilder.aSitemapCrawlResultForOrigin(TEST_ORIGIN.toString()).build());


        sut = TestCrawlJobFactoryBuilder.builder()
                .withWebPageReaderFactory(mockWebPageReaderFactory())
                .withFilterFactory(filtersFactory)
                .withSitemapService(sitemapService)
                .withCrawlEventDispatch(dispatch)
                .build();

        testConf = CrawlJobConfiguration.newConfiguration("unitTest@myseotoolbox", TEST_ORIGIN)
                .withSeeds(ONLY_ROOT).withRobotsTxt(mockRobotsTxt)
                .withTriggerForUserInitiatedCrawlWorkspace(1234);
    }

    @Test
    public void shouldOnlyCrawlOnlyFromTheSeeds() throws SnapshotException {

        CrawlJobConfiguration configuration = testConf.withSeeds(seeds("/path1", "/path2")).build();

        CrawlJob job = sut.make(configuration);
        job.start();

        verify(reader).snapshotPage(TEST_ORIGIN.resolve("/path1"));
        verify(reader).snapshotPage(TEST_ORIGIN.resolve("/path2"));
        verifyNoMoreInteractions(reader);
    }

    @Test
    public void shouldOnlyCrawlUpToMax() {

        CrawlJobConfiguration configuration = testConf
                .withMaxPagesCrawledLimit(3)
                .withSeeds(seeds("/path1", "/path2", "/path3", "/path4", "/path5", "/path6"))
                .build();
        CrawlJob job = sut.make(configuration);
        job.start();

        verify(dispatch, times(3)).onPageCrawled(any());
    }

    @Test
    public void shouldNotifyMonitoredUriUpdater() {
        CrawlJob job = sut.make(testConf.build());
        job.start();
        verify(dispatch).onPageCrawled(argThat(snapshot -> snapshot.getUri().equals(TEST_ORIGIN.toString())));
    }

    @Test
    public void shouldFilterAsSpecified() throws SnapshotException {
        CrawlJob job = sut.make(testConf.build());
        job.start();

        verify(reader).snapshotPage(TEST_ORIGIN);
        verifyNoMoreInteractions(reader);
    }

    @Test
    public void shouldTakeSeedsFromSitemap() throws SnapshotException {
        URI linkFromSitemap = TEST_ORIGIN.resolve("/fromSitemap");
        SitemapCrawlResult result = TestSitemapCrawlResultBuilder.aSitemapCrawlResultForOrigin(TEST_ORIGIN.toString()).withCurLinks(linkFromSitemap).build();
        when(sitemapService.fetchSeedsFromSitemaps(any(), any())).thenReturn(result);

        CrawlJob job = sut.make(testConf.build());
        job.start();

        verify(reader).snapshotPage(TEST_ORIGIN);
        verify(reader).snapshotPage(linkFromSitemap);
        verifyNoMoreInteractions(reader);
    }


    @Test
    public void shouldNormalizeSeedsWIthEmptyPathToRootPath() {


        SitemapCrawlResult result = TestSitemapCrawlResultBuilder.aSitemapCrawlResultForOrigin(TEST_ORIGIN.toString()).withCurLinks(TEST_ORIGIN.resolve("/fromSitemap")).build();

        when(sitemapService.fetchSeedsFromSitemaps(any(), any())).thenReturn(result);

        CrawlJobConfiguration conf = testConf.withSeeds(Collections.singletonList(URI.create("http://host"))).build();


        CrawlJob job = sut.make(conf);
        job.start();

        verify(filtersFactory).build(TEST_ORIGIN, Collections.singletonList("/"), mockRobotsTxt);
    }

    private CrawlResult buildSnapshotForUri(InvocationOnMock invocation) {
        String uri = invocation.getArgument(0).toString();
        PageSnapshot snapshot = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(uri);
        snapshot.setLinks(Collections.singletonList(new PageLink(TEST_FILTERED_LINK.toString(), Collections.emptyMap())));
        return CrawlResult.forSnapshot(snapshot);
    }

    private WebPageReaderFactory mockWebPageReaderFactory() {
        return new WebPageReaderFactory(null, null) {
            @Override
            public WebPageReader build(UriFilter uriFilter, long crawlDelayMillis) {
                return reader;
            }
        };
    }

    private List<URI> seeds(String... seeds) {
        return Arrays.stream(seeds).map(TEST_ORIGIN::resolve).collect(Collectors.toList());
    }
}
