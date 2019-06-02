package com.myseotoolbox.crawler.spider.sitemap;

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
import java.util.List;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public class SiteMapTest {

    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();

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