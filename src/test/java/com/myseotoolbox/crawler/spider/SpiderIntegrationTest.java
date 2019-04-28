package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.TestWebsite;
import com.myseotoolbox.crawler.testutils.testwebsite.ReceivedRequest;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.extractRoot;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * NOTE!!! PLEASE READ
 * <p>
 * WorkspaceCrawler will group origin by domain and treat them as seeds. So far, so good.
 * the hacky bit is that the seeds are used as "allowed paths" when creating the UriFilter see CrawlJobFactory
 * which is kind of not super explicit from the user point of view
 */
@RunWith(MockitoJUnitRunner.class)
public class SpiderIntegrationTest {

    private CrawlExecutorFactory testExecutorBuilder = new CurrentThreadCrawlExecutorFactory();

    private InputStream robotsTxt = getClass().getResourceAsStream("/robots.txt");
    TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    @Mock private Consumer<PageSnapshot> crawledPagesListener;

    @Before
    public void setUp() throws Exception {
        testWebsiteBuilder.run();
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }

    @Test
    public void basicLinkFollowing() {

        givenAWebsite()
                .havingPage("/").withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(crawledPagesListener).accept(uri("/"));
        verify(crawledPagesListener).accept(uri("/abc"));
        verify(crawledPagesListener).accept(uri("/cde"));

        verifyNoMoreInteractions(crawledPagesListener);

    }

    @Test
    public void shouldOnlyFilterFromSpecifiedPaths() {
        givenAWebsite()
                .havingPage("/base").withLinksTo("/base/abc", "/base/cde", "/outside/fgh")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base"));
        job.start();


        verify(crawledPagesListener).accept(uri("/base"));
        verify(crawledPagesListener).accept(uri("/base/abc"));
        verify(crawledPagesListener).accept(uri("/base/cde"));

        verifyNoMoreInteractions(crawledPagesListener);

    }

    @Test
    public void multipleSeedsActAsFilters() {


        givenAWebsite()
                .havingPage("/base").withLinksTo("/base/abc", "/base/cde", "/base2/fgh", "/outside/a")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base", "/base2"));
        job.start();

        verify(crawledPagesListener).accept(uri("/base"));
        verify(crawledPagesListener).accept(uri("/base2"));
        verify(crawledPagesListener).accept(uri("/base/abc"));
        verify(crawledPagesListener).accept(uri("/base/cde"));
        verify(crawledPagesListener).accept(uri("/base2/fgh"));

        verifyNoMoreInteractions(crawledPagesListener);


    }

    @Test
    public void shouldNotVisitOtherDomains() {

        givenAWebsite()
                .havingPage("/base").withLinksTo("/base/abc", "http://differentdomain")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base", "/base2"));
        job.start();

        verify(crawledPagesListener).accept(uri("/base"));
        verify(crawledPagesListener).accept(uri("/base2"));
        verify(crawledPagesListener).accept(uri("/base/abc"));

        verifyNoMoreInteractions(crawledPagesListener);
    }

    @Test
    public void shouldNotVisitBlockedUriInRedirectChain() {
        TestWebsite save = givenAWebsite()
                .withRobotsTxt(robotsTxt)
                .havingRootPage().redirectingTo(301, "/blocked-by-robots").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        List<ReceivedRequest> receivedRequests = save.getRequestsReceived();

        assertThat(receivedRequests, hasSize(2));
        assertThat(receivedRequests.get(0).getUrl(), is("/robots.txt"));
        assertThat(receivedRequests.get(1).getUrl(), is("/"));
    }

    @Test
    public void shouldNotNotifyListenersWhenChainIsBlocked() {
        givenAWebsite()
                .withRobotsTxt(robotsTxt)
                .havingRootPage().withLinksTo("/dst1", "dst2").and()
                .havingPage("/dst2").redirectingTo(301, "/blocked-by-robots").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(crawledPagesListener).accept(uri("/"));
        verify(crawledPagesListener).accept(uri("/dst1"));
        verifyNoMoreInteractions(crawledPagesListener);
    }

    private PageSnapshot uri(String uri) {
        return argThat(argument -> argument.getUri().equals(testUri(uri).toString()));
    }

    private CrawlJob buildForSeeds(List<URI> seeds) {

        //mimic WorkspaceCrawler

        URI origin = extractRoot(seeds.get(0));
        SpiderConfig spiderConfig = new SpiderConfig();

        CrawlJobFactory crawlJobFactory = spiderConfig
                .getCrawlJobFactory(Mockito.mock(PageCrawlPersistence.class), Mockito.mock(MonitoredUriUpdater.class), testExecutorBuilder);

        CrawlJob job = crawlJobFactory.build(origin, seeds, 1);

        job.subscribeToPageCrawled(crawledPagesListener);
        return job;
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

    private List<URI> testSeeds(String... urls) {
        return Arrays.stream(urls).map(s -> testWebsiteBuilder.buildTestUri(s)).collect(Collectors.toList());
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }
}
