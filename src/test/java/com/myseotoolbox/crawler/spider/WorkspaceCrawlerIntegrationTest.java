package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.CrawlEventListener;
import com.myseotoolbox.crawler.CrawlEventsListenerFactory;
import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.HttpURLConnectionFactory;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WebsiteCrawlLogRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.configuration.CrawlerSettingsBuilder;
import com.myseotoolbox.crawler.spider.configuration.RobotsTxtAggregation;
import com.myseotoolbox.crawler.spider.sitemap.SitemapReader;
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
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class WorkspaceCrawlerIntegrationTest {


    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();

    private Executor executor = new CurrentThreadTestExecutorService();
    private CrawlExecutorFactory testExecutorBuilder = new CurrentThreadCrawlExecutorFactory();
    private WebPageReaderFactory webPageReaderFactory = new WebPageReaderFactory(new HttpRequestFactory(new HttpURLConnectionFactory()));
    private WebsiteUriFilterFactory uriFilterFactory = new WebsiteUriFilterFactory();
    private SitemapReader sitemapReader = new SitemapReader();
    private RobotsTxtAggregation robotsAggregation = new RobotsTxtAggregation(new HTTPClient());


    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WebsiteCrawlLogRepository websiteCrawlLogRepository;
    @Mock private CrawlEventListener listener;
    @Mock private CrawlEventsListenerFactory listenerProvider;

    private List<Workspace> allWorkspaces = new ArrayList<>();
    private CrawlJobFactory crawlJobFactory;
    private WorkspaceCrawler sut;


    @Before
    public void setUp() throws Exception {
        when(listenerProvider.getPageCrawlListener(any())).thenReturn(listener);
        when(workspaceRepository.findAll()).thenReturn(allWorkspaces);
        crawlJobFactory = new CrawlJobFactory(webPageReaderFactory, uriFilterFactory, testExecutorBuilder, sitemapReader);
        sut = new WorkspaceCrawler(workspaceRepository, crawlJobFactory, websiteCrawlLogRepository, listenerProvider, robotsAggregation, executor);
        testWebsiteBuilder.run();
        givenAWorkspace().withWebsiteUrl(testUri("/").toString()).build();
    }


    @Test
    public void shouldNotifyListenerOfAllThePageCrawled() {
        givenAWebsite().havingRootPage().withLinksTo("/page1", "page2").save();

        sut.crawlAllWorkspaces();

        verify(listener).onPageCrawled(argThat(snapshot -> snapshot.getUri().equals(testUri("/").toString())));
        verify(listener).onPageCrawled(argThat(snapshot -> snapshot.getUri().equals(testUri("/page1").toString())));
        verify(listener).onPageCrawled(argThat(snapshot -> snapshot.getUri().equals(testUri("/page2").toString())));
        verifyNoMoreInteractions(listener);

    }

    @Test
    public void shouldUseRobotsTxt() {
        givenAWebsite()
                .havingRootPage().withLinksTo("/page1", "page2")
                .withRobotsTxt().userAgent("*").disallow("/page2").build().save();

        sut.crawlAllWorkspaces();

        verify(listener).onPageCrawled(argThat(snapshot -> snapshot.getUri().equals(testUri("/").toString())));
        verify(listener).onPageCrawled(argThat(snapshot -> snapshot.getUri().equals(testUri("/page1").toString())));
        verifyNoMoreInteractions(listener);
    }


    @Test
    public void shouldDisableRobotsTxt() {

        givenAWorkspace().withWebsiteUrl(testUri("/").toString())
                .withCrawlerSettings(CrawlerSettingsBuilder.defaultSettings().withIgnoreRobotsTxt(true).build()).build();

        givenAWebsite()
                .havingRootPage().withLinksTo("/page1", "page2")
                .withRobotsTxt().userAgent("*").disallow("/page2").build().save();

        sut.crawlAllWorkspaces();

        verify(listener).onPageCrawled(argThat(snapshot -> snapshot.getUri().equals(testUri("/").toString())));
        verify(listener).onPageCrawled(argThat(snapshot -> snapshot.getUri().equals(testUri("/page1").toString())));
        verify(listener).onPageCrawled(argThat(snapshot -> snapshot.getUri().equals(testUri("/page2").toString())));
        verifyNoMoreInteractions(listener);
    }


    private class CurrentThreadCrawlExecutorFactory extends CrawlExecutorFactory {
        @Override
        public ThreadPoolExecutor buildExecutor(String namePostfix, int concurrentConnections) {
            return new CurrentThreadTestExecutorService();
        }
    }

    private URI testUri(String url) {
        return testWebsiteBuilder.buildTestUri(url);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }


    private TestWorkspaceBuilder givenAWorkspace() {
        return new TestWorkspaceBuilder(allWorkspaces, websiteCrawlLogRepository);
    }

}
