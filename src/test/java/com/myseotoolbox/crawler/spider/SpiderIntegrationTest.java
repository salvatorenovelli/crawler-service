package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.CrawlEventListener;
import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.configuration.RobotsTxtAggregation;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.spider.sitemap.SitemapReader;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.TestWebsite;
import com.myseotoolbox.crawler.testutils.testwebsite.ReceivedRequest;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.extractRoot;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
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

    @Mock private CrawlEventListener listener;
    @Mock private SitemapReader sitemapReader;


    TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();

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

        verify(listener).onPageCrawled(uri("/"));
        verify(listener).onPageCrawled(uri("/abc"));
        verify(listener).onPageCrawled(uri("/cde"));

        verifyNoMoreInteractions(listener);

    }

    @Test
    public void shouldOnlyFilterFromSpecifiedPaths() {
        givenAWebsite()
                .havingPage("/base").withLinksTo("/base/abc", "/base/cde", "/outside/fgh").and()
                .havingPage("/outside/fgh").withLinksTo("/outside/1234")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base"));
        job.start();


        verify(listener).onPageCrawled(uri("/base"));
        verify(listener).onPageCrawled(uri("/base/abc"));
        verify(listener).onPageCrawled(uri("/base/cde"));
        verify(listener).onPageCrawled(uri("/outside/fgh"));

        verifyNoMoreInteractions(listener);

    }

    @Test
    public void multipleSeedsActAsFilters() {


        givenAWebsite()
                .havingPage("/base").withLinksTo("/base/abc", "/base/cde", "/base2/fgh", "/outside/a").and()
                .havingPage("/outside/a").withLinksTo("/outside/b")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base", "/base2"));
        job.start();

        verify(listener).onPageCrawled(uri("/base"));
        verify(listener).onPageCrawled(uri("/base2"));
        verify(listener).onPageCrawled(uri("/base/abc"));
        verify(listener).onPageCrawled(uri("/base/cde"));
        verify(listener).onPageCrawled(uri("/base2/fgh"));
        verify(listener).onPageCrawled(uri("/outside/a"));

        verifyNoMoreInteractions(listener);


    }

    @Test
    public void shouldNotVisitOtherDomains() {

        givenAWebsite()
                .havingPage("/base").withLinksTo("/base/abc", "http://differentdomain")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base", "/base2"));
        job.start();

        verify(listener).onPageCrawled(uri("/base"));
        verify(listener).onPageCrawled(uri("/base2"));
        verify(listener).onPageCrawled(uri("/base/abc"));

        verifyNoMoreInteractions(listener);
    }

    @Test
    public void shouldNotVisitBlockedUriInRedirectChain() {
        TestWebsite save = givenAWebsite()
                .withRobotsTxt().userAgent("*").disallow("/blocked-by-robots").build()
                .havingRootPage().redirectingTo(301, "/blocked-by-robots").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        List<ReceivedRequest> receivedRequests = save.getRequestsReceived();

        assertThat(receivedRequests, hasSize(2));
        assertThat(receivedRequests.stream().map(ReceivedRequest::getUrl).collect(Collectors.toList()), hasItems("/robots.txt", "/"));
    }

    @Test
    public void shouldNotNotifyListenersWhenChainIsBlocked() {
        givenAWebsite()
                .withRobotsTxt().userAgent("*").disallow("/blocked-by-robots").build()
                .havingRootPage().withLinksTo("/dst1", "dst2").and()
                .havingPage("/dst2").redirectingTo(301, "/blocked-by-robots").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(listener).onPageCrawled(uri("/"));
        verify(listener).onPageCrawled(uri("/dst1"));
        verifyNoMoreInteractions(listener);
    }


    @Test
    public void shouldSanitizeTags() {
        givenAWebsite().havingRootPage().withTitle("This <b>has</b> leading spaces    ").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(listener).onPageCrawled(argThat(snapshot -> {
            assertThat(snapshot.getPageSnapshot().getTitle(), is("This has leading spaces"));
            return true;
        }));
    }

    @Test
    public void shouldTrimUrls() {
        givenAWebsite()
                .havingRootPage().withLinksTo("/dst1   ").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(listener).onPageCrawled(uri("/"));
        verify(listener).onPageCrawled(uri("/dst1"));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void integrateWithRobotsTxt() {
        givenAWebsite()
                .withRobotsTxt().userAgent("*").disallow("/disallowed").build()
                .havingRootPage().withLinksTo("/disallowed").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(listener).onPageCrawled(uri("/"));
        verifyNoMoreInteractions(listener);
    }

    private CrawlResult uri(String uri) {
        return argThat(argument -> argument.getPageSnapshot().getUri().equals(testUri(uri).toString()));
    }

    private CrawlJob buildForSeeds(List<URI> seeds) {

        //mimic WorkspaceCrawler

        URI origin = extractRoot(seeds.get(0));
        SpiderConfig spiderConfig = new SpiderConfig();

        CrawlJobFactory crawlJobFactory = spiderConfig
                .getCrawlJobFactory(testExecutorBuilder, sitemapReader);

        RobotsTxtAggregation robotsTxtAggregation = new RobotsTxtAggregation(new HTTPClient());

        RobotsTxt merged = robotsTxtAggregation.aggregate(seeds.stream().map(uri -> {
            Workspace workspace = new Workspace();
            workspace.setWebsiteUrl(uri.toString());
            return workspace;
        }).collect(Collectors.toList()));

        CrawlJobConfiguration conf = CrawlJobConfiguration
                .newConfiguration(origin)
                .withSeeds(seeds)
                .withConcurrentConnections(seeds.size())
                .withRobotsTxt(merged)
                .build();


        return crawlJobFactory.build(conf, listener);
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
