package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.HttpURLConnectionFactory;
import com.myseotoolbox.crawler.spider.UriFilter;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.filter.BasicUriFilter;
import com.myseotoolbox.crawler.spider.filter.PathFilter;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.testutils.TestWebsite;
import com.myseotoolbox.crawler.testutils.testwebsite.ReceivedRequest;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.myseotoolbox.crawler.testutils.TestCrawlJobBuilder.buildTestConfigurationForSeeds;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class SitemapServiceTest {

    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();

    private final HttpURLConnectionFactory connectionFactory = new HttpURLConnectionFactory();
    private final HttpRequestFactory requestFactory = new HttpRequestFactory(connectionFactory);
    private final SitemapReaderFactory sitemapReaderFactory = new SitemapReaderFactory(requestFactory);

    private final SitemapRepository sitemapRepository = Mockito.mock(SitemapRepository.class);
    private SitemapService sut = new SitemapService(sitemapReaderFactory, sitemapRepository);
    private BasicUriFilter basicFilter;
    private CrawlJobConfiguration curCrawlJobConfiguration;

    @Before
    public void setUp() throws Exception {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.INFO);
        testWebsiteBuilder.run();
        basicFilter = new BasicUriFilter(URI.create(testWebsiteBuilder.getBaseUriAsString()));
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }

    //This is necessary to discover child sitemaps
    @Test
    public void shouldFetchSitemapOnRootEvenIfNotInAllowedPath() {
        //given
        PathFilter uriFilter = new PathFilter(Collections.singletonList("/it/"));

        TestWebsite testWebsite = givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/en/", "/de/")
                .build().getTestWebsite();

        fetchSeeds(testUris("/sitemap.xml"), uriFilter);

        List<String> receivedRequests = testWebsite.getRequestsReceived().stream().map(ReceivedRequest::getUrl).collect(Collectors.toList());
        assertThat(receivedRequests, hasItem("/sitemap.xml"));
    }

    @Test
    public void shouldNotFetchSitemapsDiscoveredOutsideAllowedPath() {
        PathFilter uriFilter = new PathFilter(Collections.singletonList("/it/"));

        TestWebsite testWebsite = givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/en/", "/de/")
                .build().getTestWebsite();

        fetchSeeds(testUris("/sitemap.xml"), uriFilter);
        List<String> receivedRequests = testWebsite.getRequestsReceived().stream().map(ReceivedRequest::getUrl).collect(Collectors.toList());
        assertThat(receivedRequests, containsInAnyOrder("/sitemap.xml", "/it/sitemap.xml"));
    }

    @Test
    public void shouldOnlyDiscoverLinksInAllowedPaths() {
        //given
        PathFilter uriFilter = new PathFilter(Collections.singletonList("/it/"));


        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/en/", "/de/").and()
                .withSitemapOn("/it/").havingUrls("/it/location1", "/it/location2", "/en/should-not-be-discovered0").and()
                .withSitemapOn("/en/").havingUrls("/en/location1", "/en/location2", "/it/should-not-be-discovered1").and()
                .withSitemapOn("/de/").havingUrls("/de/location1", "/de/location2", "/it/should-not-be-discovered2")
                .build();

        List<URI> uris = fetchSeeds(testUris("/sitemap.xml"), uriFilter);

        assertThat(uris, containsInAnyOrder(testUri("/it/location1"), testUri("/it/location2")));
    }

    @Test
    public void shouldOnlyReturnValidUri() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2", "/should not add this")
                .build();

        List<URI> uris = fetchSeeds(testUris("/sitemap.xml"), basicFilter);

        assertThat(uris, containsInAnyOrder(testUri("/location1"), testUri("/location2")));
    }

    @Test
    public void shouldOnlyGetSameDomain() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2", "http://another-domain/")
                .build();

        List<URI> uris = fetchSeeds(testUris("/sitemap.xml"), basicFilter);

        assertThat(uris, containsInAnyOrder(testUri("/location1"), testUri("/location2")));
    }

    @Test
    public void shouldPersistCorrectSitemapCrawlResult() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2")
                .build();

        fetchSeeds(List.of(testUri("/sitemap.xml").toString()), basicFilter);

        SitemapCrawlResult expectedResult = new SitemapCrawlResult(
                curCrawlJobConfiguration.getWebsiteCrawl(),
                List.of(new SiteMap(testUri("/sitemap.xml"), Set.of(testUri("/location1"), testUri("/location2"))))
        );

        Mockito.verify(sitemapRepository).persist(Mockito.eq(expectedResult));
    }


    @Test
    public void shouldPersistSitemapCrawlResult() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2")
                .build();

        fetchSeeds(testUris("/sitemap.xml"), basicFilter);


        Mockito.verify(sitemapRepository).persist(Mockito.any(SitemapCrawlResult.class));
    }

    private List<URI> fetchSeeds(List<String> sitemapUrls, UriFilter uriFilter) {

        curCrawlJobConfiguration = buildTestConfigurationForSeeds(sitemapUrls.stream().map(URI::create).toList(), new EmptyRobotsTxt(testUri("/")));

        return sut.fetchSeedsFromSitemaps(curCrawlJobConfiguration, uriFilter)
                .sitemaps().stream().flatMap(siteMap -> siteMap.links().stream()).toList();
    }

    private URI testUri(String s) {
        return testWebsiteBuilder.buildTestUri(s);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }

    private List<String> testUris(String url) {
        return Collections.singletonList(testWebsiteBuilder.buildTestUri(url).toString());
    }

    private List<String> testUris(String... urls) {
        return Stream.of(urls).map(url -> testWebsiteBuilder.buildTestUri(url).toString()).collect(Collectors.toList());
    }
}