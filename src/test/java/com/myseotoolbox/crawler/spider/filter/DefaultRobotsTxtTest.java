package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.robotstxt.DefaultRobotsTxt;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultRobotsTxtTest {

    private DefaultRobotsTxt sut;

    TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    private InputStream stream = getClass().getResourceAsStream("/robots.txt");

    @Before
    public void setUp() throws Exception {
        givenAWebsite().withRobotsTxt(stream).withRobotTxtHavingRedirect();
        testWebsiteBuilder.run();
        sut = new DefaultRobotsTxt(testUri("/"));
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }


    @Test
    public void shouldGrabRobotsTxtWithRedirect() {
        assertFalse(sut.shouldCrawl(null, URI.create("http://domain/order")));
    }

    @Test
    public void shouldDisallow() {
        assertFalse(sut.shouldCrawl(null, URI.create("http://domain/order")));
    }

    @Test
    public void shouldAllow() {
        assertTrue(sut.shouldCrawl(null, URI.create("http://domain/product")));
    }

    @Test
    public void shouldUseSpecificValues() {
        assertFalse(sut.shouldCrawl(null,URI.create("http://domain/disallowed-for-seobot")));
    }

    private URI testUri(String url) {
        return testWebsiteBuilder.buildTestUri(url);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }
}