package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.spider.filter.robotstxt.DefaultRobotsTxt;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static com.myseotoolbox.crawler.httpclient.HttpGetRequest.BOT_NAME;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class DefaultRobotsTxtTest {

    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();


    @Before
    public void setUp() throws Exception {
        testWebsiteBuilder.run();
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }


    @Test
    public void shouldDisallow() throws IOException {
        givenAWebsite()
                .withRobotsTxt().userAgent("*").disallow("/order").build()
                .withRobotTxtHavingRedirect();

        DefaultRobotsTxt sut = buildSut();

        assertFalse(sut.shouldCrawl(null, testUri("/order")));
    }

    @Test
    public void shouldAllow() throws IOException {

        givenAWebsite()
                .withRobotsTxt().userAgent("*").disallow("/order").build()
                .withRobotTxtHavingRedirect();

        DefaultRobotsTxt sut = buildSut();

        assertTrue(sut.shouldCrawl(null, URI.create("/product")));
    }

    @Test
    public void shouldUseSpecificValues() throws IOException {

        givenAWebsite()
                .withRobotsTxt().userAgent("*").disallow("/order").build()
                .withRobotsTxt().userAgent(BOT_NAME).disallow("/disallowed-for-seobot").build()
                .withRobotTxtHavingRedirect();

        DefaultRobotsTxt sut = buildSut();

        assertFalse(sut.shouldCrawl(null, testUri("/disallowed-for-seobot")));
    }

    @Test
    public void shouldGrabRobotsTxtWithRedirect() throws IOException {
        givenAWebsite()
                .withRobotsTxt().userAgent("*").disallow("/order").build()
                .withRobotTxtHavingRedirect();

        DefaultRobotsTxt sut = buildSut();
        assertFalse(sut.shouldCrawl(null, testUri("/order")));
    }

    @Test
    public void shouldServeSitemaps() throws IOException {
        givenAWebsite()
                .withRobotsTxt()
                .reportingSitemapOn("http://host/non-standard-sitemap0.xml", "http://host/non-standard-sitemap1.xml").build();

        DefaultRobotsTxt sut = buildSut();

        assertThat(sut.getSitemaps(), containsInAnyOrder("http://host/non-standard-sitemap0.xml", "http://host/non-standard-sitemap1.xml"));
    }

    private URI testUri(String url) {
        return testWebsiteBuilder.buildTestUri(url);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }

    private DefaultRobotsTxt buildSut() throws IOException {
        URI origin = testUri("/").resolve("/robots.txt");
        HTTPClient httpClient = new HTTPClient();
        byte[] content = httpClient.get(origin).getBytes();
        return new DefaultRobotsTxt(origin.toString(), content);
    }
}