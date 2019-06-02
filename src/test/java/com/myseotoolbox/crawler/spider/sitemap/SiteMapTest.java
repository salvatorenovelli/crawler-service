package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.testutils.TestWebsite;
import com.myseotoolbox.crawler.testutils.testwebsite.ReceivedRequest;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SiteMapTest {

    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    private TestWebsite testWebsite;

    @Before
    public void setUp() throws Exception {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.INFO);
        testWebsite = testWebsiteBuilder.run();
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }

    @Test
    public void canGetUrisFromSimpleSiteMap() throws IOException {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2")
                .build();

        SiteMap siteMap = new SiteMap(uri("/sitemap.xml"));
        List<String> urls = siteMap.getUris();


        assertThat(urls, hasItems(uri("/location1"), uri("/location2")));
    }

    @Test
    public void shouldDeduplicateUrls() throws MalformedURLException {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2", "/location2")
                .build();

        SiteMap siteMap = new SiteMap(uri("/sitemap.xml"));
        List<String> urls = siteMap.getUris();

        assertThat(urls, hasSize(2));
        assertThat(urls, hasItems(uri("/location1"), uri("/location2")));
    }

    @Test
    public void canRecursivelyResolveSiteMapUrls() throws MalformedURLException {
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/uk/")
                .and()
                .withSitemapOn("/it/")
                .havingUrls("/it/1", "/it/2")
                .and()
                .withSitemapOn("/uk/")
                .havingUrls("/uk/1", "/uk/2").build();

        SiteMap siteMap = new SiteMap(uri("/sitemap.xml"));
        List<String> urls = siteMap.getUris();

        assertThat(urls, hasItems(uri("/it/1"), uri("/it/2"), uri("/uk/1"), uri("/uk/2")));
    }


    @Test
    public void shouldNotGetUrlsFromFilteredSitemap() throws MalformedURLException {
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/uk/")
                .and()
                .withSitemapOn("/it/")
                .havingUrls("/it/1", "/it/2")
                .and()
                .withSitemapOn("/uk/")
                .havingUrls("/uk/1", "/uk/2").build();


        SiteMap siteMap = new SiteMap(uri("/sitemap.xml"), Collections.singletonList("/it"));
        List<String> urls = siteMap.getUris();

        assertThat(urls, hasSize(2));
        assertThat(urls, hasItems(uri("/it/1"), uri("/it/2")));

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


        SiteMap siteMap = new SiteMap(uri("/sitemap.xml"), Collections.singletonList("/it/"));
        List<String> urls = siteMap.getUris();

        assertThat(urls, hasSize(2));
        assertThat(urls, hasItems(uri("/it/1"), uri("/it/2")));
    }

    @Test
    public void shouldNotFetchUnnecessarySitemaps() throws MalformedURLException {
        givenAWebsite()
                .withSitemapIndexOn("/")
                .havingChildSitemaps("/it/", "/uk/")
                .and()
                .withSitemapOn("/it/")
                .havingUrls("/it/1", "/it/2")
                .and()
                .withSitemapOn("/uk/")
                .havingUrls("/uk/1", "/uk/2").build();

        SiteMap siteMap = new SiteMap(uri("/sitemap.xml"), Collections.singletonList("/it"));
        siteMap.getUris();

        List<String> requestsReceived = testWebsite.getRequestsReceived().stream().map(ReceivedRequest::getUrl).collect(Collectors.toList());
        assertThat(requestsReceived, hasSize(2));
        assertThat(requestsReceived, hasItems("/sitemap.xml", "/it/sitemap.xml"));
    }

    @Test
    public void shouldListUrlWithDifferentDomains() throws MalformedURLException {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2", "https://differentdomain/location2")
                .build();

        SiteMap siteMap = new SiteMap(uri("/sitemap.xml"));
        List<String> uris = siteMap.getUris();

        assertThat(uris, hasSize(2));
        assertThat(uris, hasItems(uri("/location1"), uri("/location2")));
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


        SiteMap siteMap = new SiteMap(uri("/sitemap.xml"));
        siteMap.getUris();

        assertThat(differentDomainWebsite.getRequestsReceived(), hasSize(0));
    }

    private String uri(String s) {
        return testUri(s).toString();
    }

    private URI testUri(String url) {
        return testWebsiteBuilder.buildTestUri(url);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }

}