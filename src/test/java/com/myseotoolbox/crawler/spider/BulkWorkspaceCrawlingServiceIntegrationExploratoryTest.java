package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.CrawlEventDispatchFactory;
import com.myseotoolbox.crawler.config.WebPageReaderFactory;
import com.myseotoolbox.crawler.httpclient.*;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.CrawlDelayExpired;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import com.myseotoolbox.crawler.spider.configuration.RobotsTxtAggregation;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.ratelimiter.SystemClockUtils;
import com.myseotoolbox.crawler.spider.ratelimiter.TestClockUtils;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class BulkWorkspaceCrawlingServiceIntegrationExploratoryTest {

    private ClockUtils testClockUtils = new TestClockUtils();

    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();

    private final Executor executor = new CurrentThreadTestExecutorService();
    private RobotsTxtAggregation robotsAggregation = new RobotsTxtAggregation(new HTTPClient());


    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private CrawlDelayExpired crawlDelayExpired;
    @Mock private CrawlEventDispatch dispatch;

    private List<Workspace> allWorkspaces = new ArrayList<>();
    private CrawlJobFactory crawlJobFactory;
    private BulkWorkspaceCrawlingService sut;


    @Before
    public void setUp() throws Exception {
        when(workspaceRepository.findAll()).thenReturn(allWorkspaces);
        when(crawlDelayExpired.isCrawlDelayExpired(any())).thenReturn(true);

        crawlJobFactory = TestCrawlJobFactoryBuilder.builder()
                .withCrawlEventDispatch(dispatch)
                .withCLockUtils(testClockUtils)
                .build();
        sut = new BulkWorkspaceCrawlingService(workspaceRepository, crawlJobFactory, crawlDelayExpired, robotsAggregation, executor);
        testWebsiteBuilder.run();
    }

    //    @Test
    public void crawlSingleUrlExploratory() {
        WebPageReaderFactory webPageReaderFactory = new WebPageReaderFactory(new HttpRequestFactory(new HttpURLConnectionFactory()), new SystemClockUtils());
        WebPageReader build = webPageReaderFactory.build((sourceUri, discoveredLink) -> true, 0);

        CrawlResult crawlResult = null;
        try {
            crawlResult = build.snapshotPage(URI.create("https://"));
        } catch (SnapshotException e) {
            System.out.println(e.getPartialSnapshot().getRedirectChainElements());
        }

        System.out.println(crawlResult);
    }

    @Test
    public void shouldNotifyListenerOfAllThePageCrawled() {
        givenAWorkspace().withWebsiteUrl(testUri("/").toString()).build();
        givenAWebsite().havingRootPage().withLinksTo("/page1", "page2").save();

        sut.crawlAllWorkspaces();

        verify(dispatch).onPageCrawled(crawlResultFor("/"));
        verify(dispatch).onPageCrawled(crawlResultFor("/page1"));
        verify(dispatch).onPageCrawled(crawlResultFor("/page2"));
        verify(dispatch, atMost(3)).onPageCrawled(any());
    }

    @Test
    public void shouldUseRobotsTxt() {
        givenAWorkspace().withWebsiteUrl(testUri("/").toString()).build();
        givenAWebsite()
                .havingRootPage().withLinksTo("/page1", "page2")
                .withRobotsTxt().userAgent("*").disallow("/page2").build().save();

        sut.crawlAllWorkspaces();

        verify(dispatch).onPageCrawled(crawlResultFor("/"));
        verify(dispatch).onPageCrawled(crawlResultFor("/page1"));
        verify(dispatch, atMost(2)).onPageCrawled(any());
    }


    @Test
    public void shouldNotCrawlNoFollow() {
        givenAWorkspace().withWebsiteUrl(testUri("/").toString()).build();
        givenAWebsite()
                .havingRootPage()
                .withLinksTo("/page1")
                .withNoFollowLinksTo("/page2").save();

        sut.crawlAllWorkspaces();

        verify(dispatch).onPageCrawled(crawlResultFor("/"));
        verify(dispatch).onPageCrawled(crawlResultFor("/page1"));
        verify(dispatch, atMost(2)).onPageCrawled(any());
    }

    @Test
    public void shouldDisableRobotsTxt() {

        givenAWorkspace().withWebsiteUrl(testUri("/").toString())
                .withCrawlerSettings(CrawlerSettingsBuilder.defaultSettings().withIgnoreRobotsTxt(true).build()).build();

        givenAWebsite()
                .havingRootPage().withLinksTo("/page1", "page2")
                .withRobotsTxt().userAgent("*").disallow("/page2").build().save();

        sut.crawlAllWorkspaces();

        verify(dispatch).onPageCrawled(crawlResultFor("/"));
        verify(dispatch).onPageCrawled(crawlResultFor("/page1"));
        verify(dispatch).onPageCrawled(crawlResultFor("/page2"));
        verify(dispatch, atMost(3)).onPageCrawled(any());

    }

    @Test
    public void shouldOnlyCrawlInsidePath() {
        givenAWorkspace().withWebsiteUrl(testUri("/path/").toString()).build();

        givenAWebsite()
                .havingPage("/path/").withLinksTo("/path/first", "/path/subpath/second", "/outside/something").and()
                .havingPage("/outside/something").withLinksTo("/outside/shouldnotcrawlthis")
                .save();
        sut.crawlAllWorkspaces();


        verify(dispatch).onPageCrawled(crawlResultFor("/path/"));
        verify(dispatch).onPageCrawled(crawlResultFor("/path/first"));
        verify(dispatch).onPageCrawled(crawlResultFor("/path/subpath/second"));
        verify(dispatch).onPageCrawled(crawlResultFor("/outside/something"));//still crawl this as the link originates on a page where we want to check for broken links, for example
        verify(dispatch, times(4)).onPageCrawled(any());

    }

    @Test
    public void ifTheSeedsHaveFilenamesAtTheEndTheyShouldNotBeConsideredPath() {

        givenAWorkspace().withWebsiteUrl(testUri("/path/index.html").toString()).build();
        givenAWebsite()
                .havingPage("/path/index.html").withLinksTo("/path/first").save();
        sut.crawlAllWorkspaces();


        verify(dispatch).onPageCrawled(crawlResultFor("/path/index.html"));
        verify(dispatch).onPageCrawled(crawlResultFor("/path/first"));
        verify(dispatch, atMost(2)).onPageCrawled(any());
    }

    @Test
    public void shouldCrawlWithinMaxFrequency() {
        givenAWorkspace()
                .withWebsiteUrl(testUri("/").toString())
                .withCrawlDelayMillis(500)
                .build();

        givenAWebsite()
                .havingRootPage()
                .withLinksTo("/page1", "/page2", "/page3", "/page4")
                .save();

        sut.crawlAllWorkspaces();

        assertThat(testClockUtils.currentTimeMillis(), is(2000L));
    }

    private CrawlResult crawlResultFor(String s) {
        return argThat(snapshot -> snapshot.getUri().equals(testUri(s).toString()));
    }

    private URI testUri(String url) {
        return testWebsiteBuilder.buildTestUri(url);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }

    private TestWorkspaceBuilder givenAWorkspace() {
        return new TestWorkspaceBuilder(allWorkspaces);
    }

}
