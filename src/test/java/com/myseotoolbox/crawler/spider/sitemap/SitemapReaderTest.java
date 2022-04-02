package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SitemapReaderTest {

    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    private SitemapReader sut = new SitemapReader();

    @Before
    public void setUp() throws Exception {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.INFO);
        testWebsiteBuilder.run();
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }


    @Test
    public void shouldGetSeedsFromSitemapsIncludingFiltering() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2", "/outside/shouldnotaddthis")
                .build();

        List<URI> uris = sut.fetchSeedsFromSitemaps(testUri("/"), testUris("/sitemap.xml"), Collections.singletonList("/"));

        assertThat(uris, hasItems(testUri("/location1"), testUri("/location2")));
    }

    @Test
    public void shouldOnlyReturnValidUri() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2", "/should not add this")
                .build();

        List<URI> uris = sut.fetchSeedsFromSitemaps(testUri("/"), testUris("/sitemap.xml"), Collections.singletonList("/"));

        assertThat(uris, hasItems(testUri("/location1"), testUri("/location2")));
    }

    @Test
    public void shouldOnlyGetSameDomain() {
        givenAWebsite()
                .withSitemapOn("/")
                .havingUrls("/location1", "/location2", "http://another-domain/")
                .build();

        List<URI> uris = sut.fetchSeedsFromSitemaps(testUri("/"), testUris("/sitemap.xml"), Collections.singletonList("/"));

        assertThat(uris, hasItems(testUri("/location1"), testUri("/location2")));
    }

    @Test
    public void shouldOnlyFetchSitemapsFromAllowedPaths() {

        givenAWebsite()
                .withSitemapOn("/en/gb/")
                .havingUrls("/en/gb/1", "/en/gb/2", "/it/it/1").and()
                .withSitemapOn("/it/it/")
                .havingUrls("/it/it/2", "/en/gb/3")
                .build();

        List<URI> uris = sut.fetchSeedsFromSitemaps(testUri("/"),
                testUris("/en/gb/sitemap.xml", "/it/it/sitemap.xml"), Collections.singletonList("/en/gb/"));

        // note that allowedPaths is not intended to filter the discovered URLS in the sitemaps but only the sitemap links provided by the sitemapsUrls and the ones discovered recursively
        // however, this is only the current implementation, and I'm not sure what would be the impact of filtering the URLS at this level
        // might make sense, but it's not the current implementation

        assertThat(uris, Matchers.contains(testUri("/en/gb/1"), testUri("/en/gb/2"), testUri("/it/it/1")));

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