package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.CrawlEventDispatchFactory;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.repository.MonitoredUriRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.testutils.TestCrawlJobBuilder;
import com.myseotoolbox.crawler.testutils.TestWebsite;
import com.myseotoolbox.crawler.testutils.TestWorkspaceBuilder;
import com.myseotoolbox.crawler.testutils.testwebsite.ReceivedRequest;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import com.myseotoolbox.utils.ItemMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * NOTE!!! PLEASE READ
 * <p>
 * WorkspaceCrawler will group origin by domain and treat them as seeds. So far, so good.
 * the hacky bit is that the seeds are used as "allowed paths" when creating the UriFilter see CrawlJobFactory
 * which is kind of not super explicit from the user point of view
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpiderIntegrationTest {


    @Autowired MonitoredUriRepository monitoredUriRepository;
    @Autowired private WorkspaceRepository workspaceRepository;
    @Autowired private CrawlEventDispatchFactory dispatchFactory;

    private CrawlEventDispatch dispatchSpy;


    TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    TestCrawlJobBuilder testCrawlJobBuilder;

    @Before
    public void setUp() throws Exception {
        testWebsiteBuilder.run();
        dispatchSpy = Mockito.spy(dispatchFactory.buildFor(TestWebsiteCrawlFactory.newWebsiteCrawlFor(testUri("/").toString(), Collections.emptyList())));
        testCrawlJobBuilder = new TestCrawlJobBuilder(dispatchSpy);
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
        monitoredUriRepository.deleteAll();
    }

    @Test
    public void basicLinkFollowing() {

        givenAWebsite()
                .havingPage("/").withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(dispatchSpy).onPageCrawled(uri("/"));
        verify(dispatchSpy).onPageCrawled(uri("/abc"));
        verify(dispatchSpy).onPageCrawled(uri("/cde"));

        verify(dispatchSpy, atMost(3)).onPageCrawled(any());

    }

    @Test
    public void shouldEmitStatusUpdate() {
        givenAWebsite()
                .havingPage("/").withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(dispatchSpy, times(3)).onCrawlStatusUpdate(anyInt(), anyInt());
    }

    @Test
    public void urlsWithFragmentsShouldBeNormalized() {
        givenAWebsite()
                .havingPage("/").withLinksTo("/another-page", "/another-page#reviews")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(dispatchSpy).onPageCrawled(uri("/"));
        verify(dispatchSpy).onPageCrawled(uri("/another-page"));
        verify(dispatchSpy, atMost(2)).onPageCrawled(any());
    }

    @Test
    public void websiteWithoutRobotsCanHaveSitemap() {
        givenAWebsite()
                .withSitemapOn("/").havingUrls("/link1", "/link2").build()
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(dispatchSpy).onPageCrawled(uri("/"));
        verify(dispatchSpy).onPageCrawled(uri("/link1"));
        verify(dispatchSpy).onPageCrawled(uri("/link2"));
    }

    @Test
    public void shouldFilterBadExtensionFromSitemap() {
        givenAWebsite()
                .withSitemapOn("/").havingUrls("/link1", "/link2.png").build()
                .save();
        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(dispatchSpy, never()).onPageCrawled(uri("/link2.png"));
    }


    @Test
    public void shouldOnlyDiscoverLinksFromAllowedPaths() {
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/en/gb/", "/it/it/").and()
                .withSitemapOn("/en/gb/").havingUrls("/en/gb/1", "/en/gb/2", "/it/it/1").and()
                .withSitemapOn("/it/it/").havingUrls("/it/it/2", "/en/gb/3")
                .build().getTestWebsite();

        CrawlJob job = buildForSeeds(testSeeds("/en/gb/"));
        job.start();


        verify(dispatchSpy, never()).onPageCrawled(uri("/en/gb/3"));
        verify(dispatchSpy, never()).onPageCrawled(uri("/it/it/2"));

    }

    @Test
    public void shouldOnlyFetchSitemapsFromAllowedPaths() {
        TestWebsite testWebsite = givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/en/gb/", "/it/it/").and()
                .withSitemapOn("/en/gb/").havingUrls("/en/gb/1", "/en/gb/2", "/it/it/1").and()
                .withSitemapOn("/it/it/").havingUrls("/it/it/2", "/en/gb/3")
                .build().getTestWebsite();

        CrawlJob job = buildForSeeds(testSeeds("/en/gb/"));
        job.start();

        assertThat(testWebsite.getRequestsReceivedAsUrls(), not(hasItems("/it/it/sitemap.xml")));
    }

    @Test
    public void sitemapUrlsWithFragmentShouldBeNormalized() {
        givenAWebsite()
                .withSitemapOn("/").havingUrls("/another-page", "/another-page#reviews").and()
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(dispatchSpy).onPageCrawled(uri("/"));
        verify(dispatchSpy).onPageCrawled(uri("/another-page"));
        verify(dispatchSpy, atMost(2)).onPageCrawled(any());
    }

    @Test
    public void shouldOnlyFilterFromSpecifiedPaths() {
        givenAWebsite()
                .havingPage("/base/").withLinksTo("/base/abc", "/base/cde", "/outside/fgh").and()
                .havingPage("/outside/fgh").withLinksTo("/outside/1234")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base/"));
        job.start();


        verify(dispatchSpy).onPageCrawled(uri("/base/"));
        verify(dispatchSpy).onPageCrawled(uri("/base/abc"));
        verify(dispatchSpy).onPageCrawled(uri("/base/cde"));
        verify(dispatchSpy).onPageCrawled(uri("/outside/fgh"));

        verify(dispatchSpy, atMost(4)).onPageCrawled(any());
    }


    @Test
    public void shouldCrawlOutsideSeedsIfComingFromInside() {
        givenAWebsite()
                .havingPage("/base/").withLinksTo("/outside", "/").and()
                .havingPage("/outside").withLinksTo("/outside/1234")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base/"));
        job.start();

        verify(dispatchSpy).onPageCrawled(uri("/"));
        verify(dispatchSpy).onPageCrawled(uri("/base/"));
        verify(dispatchSpy).onPageCrawled(uri("/outside"));

        verify(dispatchSpy, atMost(3)).onPageCrawled(any());
    }

    @Test
    public void multipleSeedsActAsFilters() {


        givenAWebsite()
                .havingPage("/base/").withLinksTo("/base/abc", "/base/cde", "/base2/fgh", "/outside/a").and()
                .havingPage("/outside/a").withLinksTo("/outside/b")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base/", "/base2/"));
        job.start();

        verify(dispatchSpy).onPageCrawled(uri("/base/"));
        verify(dispatchSpy).onPageCrawled(uri("/base2/"));
        verify(dispatchSpy).onPageCrawled(uri("/base/abc"));
        verify(dispatchSpy).onPageCrawled(uri("/base/cde"));
        verify(dispatchSpy).onPageCrawled(uri("/base2/fgh"));
        verify(dispatchSpy).onPageCrawled(uri("/outside/a"));

        verify(dispatchSpy, atMost(6)).onPageCrawled(any());


    }

    @Test
    public void shouldNotVisitOtherDomains() {
        givenAWebsite()
                .havingPage("/base").withLinksTo("/base/abc", "http://differentdomain")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/base", "/base2"));
        job.start();

        verify(dispatchSpy).onPageCrawled(uri("/base"));
        verify(dispatchSpy).onPageCrawled(uri("/base2"));
        verify(dispatchSpy).onPageCrawled(uri("/base/abc"));

        verify(dispatchSpy, atMost(3)).onPageCrawled(any());
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

        verify(dispatchSpy).onPageCrawled(uri("/"));
        verify(dispatchSpy).onPageCrawled(uri("/dst1"));
        verify(dispatchSpy, atMost(2)).onPageCrawled(any());
    }


    @Test
    public void shouldSanitizeTags() {
        givenAWebsite().havingRootPage().withTitle("This <b>has</b> leading spaces    ").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(dispatchSpy).onPageCrawled(argThat(snapshot -> {
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

        verify(dispatchSpy).onPageCrawled(uri("/"));
        verify(dispatchSpy).onPageCrawled(uri("/dst1"));
        verify(dispatchSpy, atMost(2)).onPageCrawled(any());
    }

    @Test
    public void integrateWithRobotsTxt() {
        givenAWebsite()
                .withRobotsTxt().userAgent("*").disallow("/disallowed").build()
                .havingRootPage().withLinksTo("/disallowed").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(dispatchSpy).onPageCrawled(uri("/"));
        verify(dispatchSpy, atMost(1)).onPageCrawled(any());
    }

    @Test
    public void websiteWithRedirectDestinationWithSpacesShouldResolveLinksProperly() {
        givenAWebsite()
                .havingRootPage().redirectingTo(301, "/link withspaces/base").and()
                .havingPage("/link withspaces/base").withLinksTo("relative").save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(dispatchSpy).onPageCrawled(uri("/"));
        verify(dispatchSpy).onPageCrawled(uri("/link%20withspaces/relative"));
        verify(dispatchSpy, atMost(2)).onPageCrawled(any());
    }

    @Test
    public void fileInWebsiteUrlDoesNotMeanPath() {
        givenAWorkspaceWithSeqNumber(1).withCrawlOrigin(testUri("/path1/index.html").toString()).save();
        givenAWorkspaceWithSeqNumber(3).withCrawlOrigin(testUri("/").toString()).save();

        givenAWebsite()
                .havingPage("/").withLinksTo("/path1", "/path1/1", "/path3/3")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        assertThat(monitoredUriRepository.findAllByWorkspaceNumber(1), snapshotsForUris("/path1", "/path1/1"));
        assertThat(monitoredUriRepository.findAllByWorkspaceNumber(3), snapshotsForUris("/", "/path1", "/path1/1", "/path3/3"));
    }


    @Test
    public void shouldUpdateRelevantWorkspaces() {
        givenAWorkspaceWithSeqNumber(1).withCrawlOrigin(testUri("/path1/").toString()).save();
        givenAWorkspaceWithSeqNumber(2).withCrawlOrigin(testUri("/path2/").toString()).save();
        givenAWorkspaceWithSeqNumber(3).withCrawlOrigin(testUri("/").toString()).save();

        givenAWebsite()
                .havingPage("/").withLinksTo("/path1", "/path1/1", "/path3/3")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        assertThat(monitoredUriRepository.findAllByWorkspaceNumber(1), snapshotsForUris("/path1", "/path1/1"));
        assertThat(monitoredUriRepository.findAllByWorkspaceNumber(2), hasSize(0));
        assertThat(monitoredUriRepository.findAllByWorkspaceNumber(3), snapshotsForUris("/", "/path1", "/path1/1", "/path3/3"));
    }

    private Matcher<Iterable<? extends MonitoredUri>> snapshotsForUris(String... uris) {
        Collection<Matcher<? super MonitoredUri>> collect = Stream.of(uris)
                .map(this::testUri)
                .map(URI::toString)
                .map(uri -> ItemMatcher.<MonitoredUri>getItemMatcher(monitoredUri -> monitoredUri.getUri().equals(uri), uri))
                .collect(Collectors.toList());

        return containsInAnyOrder(collect);
    }


    private CrawlResult uri(String uri) {
        return argThat(argument -> argument.getPageSnapshot().getUri().equals(testUri(uri).toString()));
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

    private com.myseotoolbox.crawler.testutils.TestWorkspaceBuilder givenAWorkspaceWithSeqNumber(int seqNumber) {
        return new TestWorkspaceBuilder(workspaceRepository, seqNumber);
    }

    private CrawlJob buildForSeeds(List<URI> uris) {
        return testCrawlJobBuilder.buildForSeeds(uris);
    }
}
