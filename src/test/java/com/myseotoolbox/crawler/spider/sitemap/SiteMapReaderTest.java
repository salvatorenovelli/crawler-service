package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.HttpURLConnectionFactory;
import com.myseotoolbox.crawler.spider.filter.PathFilter;
import com.myseotoolbox.crawler.testutils.TestWebsite;
import com.myseotoolbox.crawler.testutils.testwebsite.ReceivedRequest;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.DEFAULT_MAX_URL_PER_CRAWL;
import static com.myseotoolbox.crawler.spider.sitemap.SiteMapListMatcherBuilder.isSitemapList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;

public class SiteMapReaderTest {

    public static final List<String> ALLOW_ALL = singletonList("/");
    private HttpURLConnectionFactory connectionFactory = new HttpURLConnectionFactory();
    private HttpRequestFactory httpRequestFactory = new HttpRequestFactory(connectionFactory);
    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    private TestWebsite testWebsite;

    private URI origin;
    SiteMapReader siteMapReader;

    @Before
    public void setUp() throws Exception {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.INFO);
        testWebsite = testWebsiteBuilder.run();
        origin = testUri("/");
        siteMapReader = new SiteMapReader(origin, singletonList(uri("/sitemap.xml").toString()), new PathFilter(singletonList("/")), DEFAULT_MAX_URL_PER_CRAWL, httpRequestFactory);
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }

    @Test
    public void canGetUrisFromSimpleSiteMap() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2")
                .build();

        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();


        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(1)
                        .havingSitemapFor(testUri("/sitemap.xml"))
                        .withLinks(uri("/location1"), uri("/location2")).build()
        );

    }

    @Test
    public void shouldDeduplicateUrls() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2", "/location2")
                .build();

        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();


        assertThat(sitemaps,
                isSitemapList()
                        .havingSitemapFor(testUri("/sitemap.xml"))
                        .withLinks(uri("/location1"), uri("/location2")).build()
        );
    }


    @Test
    public void canRecursivelyResolveSiteMapUrls() throws MalformedURLException {
        givenAWebsite()
                .withSitemapIndexOn("/").havingChildSitemaps("/it/", "/uk/")
                .and()
                .withSitemapOn("/it/").havingUrls("/it/1", "/it/2")
                .and()
                .withSitemapOn("/uk/").havingUrls("/uk/1", "/uk/2")
                .build();

        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();


        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(2)
                        .havingSitemapFor(testUri("/it/sitemap.xml"))
                        .withLinks(uri("/it/1"), uri("/it/2"))
                        .and()
                        .havingSitemapFor(testUri("/uk/sitemap.xml"))
                        .withLinks(uri("/uk/1"), uri("/uk/2"))
                        .build()
        );

    }


    @Test
    public void shouldNotGetUrlsFromFilteredSitemap() {
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/uk/")
                .and()
                .withSitemapOn("/it/")
                .havingUrls("/it/1", "/it/2")
                .and()
                .withSitemapOn("/uk/")
                .havingUrls("/uk/1", "/uk/2", "/it/3").build();


        SiteMapReader siteMapReader = testSiteMap(origin, testUris("/sitemap.xml"), singletonList("/it/"));

        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(1)
                        .havingSitemapFor(testUri("/it/sitemap.xml"))
                        .withLinks(uri("/it/1"), uri("/it/2"))
                        .build()
        );

    }

    @Test
    public void shouldNotFetchUnnecessarySitemaps() {
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/uk/").and()
                .withSitemapOn("/it/").havingUrls("/it/1", "/it/2", "/uk/").and()
                .withSitemapOn("/uk/").havingUrls("/uk/1", "/uk/2")
                .build();

        SiteMapReader siteMapReader = testSiteMap(origin.resolve("/it/"), testUris("/sitemap.xml"), singletonList("/it/"));
        siteMapReader.fetchSitemaps();

        List<String> requestsReceived = testWebsite.getRequestsReceived().stream().map(ReceivedRequest::getUrl).collect(toList());
        assertThat(requestsReceived, IsIterableContainingInAnyOrder.containsInAnyOrder("/sitemap.xml", "/it/sitemap.xml"));
    }

    @Test
    public void shouldNotListUrlsOutsideAllowedPath() {
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it_sitemap.xml", "/uk_sitemap.xml").and()
                .withSitemapOn("/it_sitemap.xml").havingUrls("/it/1", "/it/2", "/uk/0").and()
                .withSitemapOn("/uk_sitemap.xml").havingUrls("/uk/1", "/uk/2", "/it/3")
                .build();

        SiteMapReader siteMapReader = testSiteMap(origin, testUris("/sitemap.xml"), singletonList("/it/"));
        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(2)
                        .havingSitemapFor(testUri("/it_sitemap.xml"))
                        .withLinks(uri("/it/1"), uri("/it/2"))
                        .and()
                        .havingSitemapFor(testUri("/uk_sitemap.xml"))
                        .withLinks(uri("/it/3"))
                        .build()
        );
    }

    @Test
    public void shouldNotListUrlWithDifferentDomains() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2", "https://differentdomain/location2")
                .build();

        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();


        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(1)
                        .havingSitemapFor(testUri("/sitemap.xml"))
                        .withLinks(uri("/location1"), uri("/location2"))
                        .build()
        );


    }

    @Test
    public void shouldNotFetchSitemapsOnDifferentDomains() throws Exception {

        TestWebsiteBuilder differentDomain = TestWebsiteBuilder.build();
        TestWebsite differentDomainWebsite = differentDomain.run();


        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", differentDomain.buildTestUri("/differentDomain/").toString())
                .and()
                .withSitemapOn("/it/")
                .havingUrls("/it/1", "/it/2").build();


        siteMapReader.fetchSitemaps();

        assertThat(differentDomainWebsite.getRequestsReceived(), hasSize(0));
    }

    @Test
    public void shouldBeTolerantToTrailingSlashInAllowedPaths() throws MalformedURLException {
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/uk/")
                .and()
                .withSitemapOn("/it/")
                .havingUrls("/it/1", "/it/2")
                .and()
                .withSitemapOn("/uk/")
                .havingUrls("/uk/1", "/uk/2").build();


        SiteMapReader siteMapReader = testSiteMap(origin, testUris("/it/sitemap.xml"), singletonList("/it/"));
        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(1)
                        .havingSitemapFor(testUri("/it/sitemap.xml"))
                        .withLinks(uri("/it/1"), uri("/it/2"))
                        .build()
        );
    }

    @Test
    public void shouldFetchMultipleSitemaps() {
        givenAWebsite()
                .withSitemapOn("/one/")
                .havingUrls("/one/1", "/one/2")
                .and()
                .withSitemapOn("/two/")
                .havingUrls("/two/1", "/two/2").build();

        SiteMapReader siteMapReader = testSiteMap(origin, testUris("/one/sitemap.xml", "/two/sitemap.xml"), singletonList("/"));
        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(2)
                        .havingSitemapFor(testUri("/one/sitemap.xml"))
                        .withLinks(uri("/one/1"), uri("/one/2"))
                        .and()
                        .havingSitemapFor(testUri("/two/sitemap.xml"))
                        .withLinks(uri("/two/1"), uri("/two/2"))
                        .build()
        );

    }


    @Test
    public void shouldFailGracefullyWithMalformedUriInSitemapIndex() {
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/invalid url/")
                .havingChildSitemaps("/valid-url/").and()
                .withSitemapOn("/valid-url/")
                .havingUrls("/valid-url/1", "/valid-url/2")
                .build();

        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(1)
                        .havingSitemapFor(testUri("/valid-url/sitemap.xml"))
                        .withLinks(uri("/valid-url/1"), uri("/valid-url/2"))
                        .build()
        );

    }

    @Test
    public void shouldFailGracefullyWithMalformedUriInSitemap() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/valid-url1", "/valid-url2", "/invalid url")
                .build();

        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();


        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(1)
                        .havingSitemapFor(testUri("/sitemap.xml"))
                        .withLinks(uri("/valid-url1"), uri("/valid-url2"))
                        .build()
        );
    }


    @Test
    public void shouldNotFetchSitemapOutsideOrigin() throws Exception {
        givenAWebsite().withSitemapOn("/").havingUrls("/correct-domain-url").build();

        TestWebsiteBuilder wrongWebsiteBuilder = TestWebsiteBuilder.build();
        TestWebsite wrongWebsite = wrongWebsiteBuilder.withSitemapOn("/").havingUrls("/wrong-domain-url").build().run();


        SiteMapReader sut = testSiteMap(testUri("/"), singletonList(wrongWebsiteBuilder.buildTestUri("/sitemap.xml").toString()), singletonList("/"));
        List<SiteMapData> sitemaps = sut.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList().withSitemapCount(0).build()
        );

        assertThat(wrongWebsite.getRequestsReceived().size(), is(0));

    }

    @Test
    public void shouldNotFetchSitemapOutsideOriginWhenLinkedFromSitemapIndex() throws Exception {

        TestWebsiteBuilder wrongWebsiteBuilder = TestWebsiteBuilder.build();
        TestWebsite wrongWebsite = wrongWebsiteBuilder.withSitemapOn("/").havingUrls("/wrong-domain-url").build().run();

        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/correct/", wrongWebsiteBuilder.buildTestUri("/wrong-sitemap.xml").toString()).and()
                .withSitemapOn("/correct/sitemap.xml").havingUrls("/correct/correct-domain-url").build();


        SiteMapReader sut = testSiteMap(testUri("/"), singletonList(testUri("/sitemap.xml").toString()), singletonList("/"));
        List<SiteMapData> sitemaps = sut.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(1)
                        .havingSitemapFor(testUri("/correct/sitemap.xml"))
                        .withLinks(uri("/correct/correct-domain-url"))
                        .build()
        );

        assertThat(wrongWebsite.getRequestsReceived().size(), is(0));

    }

    @Test
    public void shouldFetchAllSitemapsInRootEvenIfOriginIsNotRoot() {
        // oddly specific...

        origin = testUri("/it/");

        SiteMapReader siteMapReader = testSiteMap(origin, testUris("/1_index_sitemap.xml"), singletonList("/it/"));

        givenAWebsite()
                .withSitemapIndexOn("/1_index_sitemap.xml")
                .havingChildSitemaps("/1_gb_0_sitemap.xml", "/1_it_0_sitemap.xml")
                .and()
                .withSitemapOn("/1_gb_0_sitemap.xml")
                .havingUrls("/gb/1", "/gb/2", "/it/0")
                .and()
                .withSitemapOn("/1_it_0_sitemap.xml")
                .havingUrls("/it/1", "/it/2")
                .build();


        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(2)
                        .havingSitemapFor(testUri("/1_gb_0_sitemap.xml"))
                        .withLinks(uri("/it/0"))
                        .and()
                        .havingSitemapFor(testUri("/1_it_0_sitemap.xml"))
                        .withLinks(uri("/it/1"), uri("/it/2"))
                        .build()
        );

    }


    @Test
    public void shouldLimitTheSize() {
        String[] tooManyUrls = IntStream.range(0, 200).mapToObj(i -> testUri("/" + i).toString()).toArray(String[]::new);
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls(tooManyUrls)
                .build();

        SiteMapReader siteMapReader = new SiteMapReader(origin, testUris("/sitemap.xml"), new PathFilter(singletonList("/")), 100, httpRequestFactory);

        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(1)
                        .havingSitemapFor(testUri("/sitemap.xml"))
                        .withLinkCount(100)
                        .build()
        );


    }


    @Test
    public void shouldStopRecursiveFetchIfAboveLimit() {
        String[] tooManyUrls = IntStream.range(0, 222).mapToObj(i -> testUri("/" + i).toString()).toArray(String[]::new);
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/uk/").and()
                .withSitemapOn("/it/")
                .havingUrls(tooManyUrls).and()
                .withSitemapOn("/uk/")
                .havingUrls(tooManyUrls)
                .build();

        SiteMapReader siteMapReader = new SiteMapReader(origin, testUris("/sitemap.xml"), new PathFilter(singletonList("/")), 100, httpRequestFactory);

        siteMapReader.fetchSitemaps();

        assertThat(testWebsite.getRequestsReceived().stream().map(ReceivedRequest::getUrl).collect(toList()),
                containsInAnyOrder("/sitemap.xml", "/it/sitemap.xml"));
    }


    @Test
    public void shouldAllowWWWButNotOtherSubdomains() throws UnknownHostException {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls(testUri("api", "/locationApi").toString(), testUri("www", "/locationWww").toString(), "/location2")
                .build();


        List<SiteMapData> sitemaps = siteMapReader.fetchSitemaps();

        assertThat(sitemaps,
                isSitemapList()
                        .withSitemapCount(1)
                        .havingSitemapFor(testUri("/sitemap.xml"))
                        .withLinks(uri("www", "/locationWww"), uri("/location2"))
                        .build()
        );
    }

    private URI uri(String s) {
        return testUri(s);
    }

    private URI uri(String subDomain, String path) {
        return testUri(subDomain, path);
    }

    private List<String> testUris(String... s) {
        return Arrays.stream(s).map(this::testUri).map(URI::toString).collect(toList());
    }


    private URI testUri(String url) {
        return testWebsiteBuilder.buildTestUri("", url);
    }

    private URI testUri(String subDomain, String url) {
        return testWebsiteBuilder.buildTestUri(subDomain, url);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }

    private SiteMapReader testSiteMap(URI origin, List<String> sitemaps, List<String> allowedPaths) {
        return new SiteMapReader(origin, sitemaps, new PathFilter(allowedPaths), DEFAULT_MAX_URL_PER_CRAWL, httpRequestFactory);
    }

}