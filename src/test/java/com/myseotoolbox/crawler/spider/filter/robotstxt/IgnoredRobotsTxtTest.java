package com.myseotoolbox.crawler.spider.filter.robotstxt;

import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class IgnoredRobotsTxtTest {

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
    public void shouldIgnoreFilteringRules() throws IOException {
        givenAWebsite()
                .withRobotsTxt().userAgent("*").disallow("/order").build()
                .withRobotTxtHavingRedirect();

        IgnoredRobotsTxt sut = buildSut();

        assertTrue(sut.shouldCrawl(null, testUri("/order")));
    }

    @Test
    public void shouldStillServeSitemap() throws IOException {
        givenAWebsite()
                .withRobotsTxt()
                .reportingSitemapOn("http://host/non-standard-sitemap0.xml", "http://host/non-standard-sitemap1.xml").build();

        IgnoredRobotsTxt sut = buildSut();

        assertThat(sut.getSitemaps(), containsInAnyOrder("http://host/non-standard-sitemap0.xml", "http://host/non-standard-sitemap1.xml"));
    }


    private URI testUri(String url) {
        return testWebsiteBuilder.buildTestUri(url);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }

    private IgnoredRobotsTxt buildSut() throws IOException {
        URI origin = testUri("/").resolve("/robots.txt");
        HTTPClient httpClient = new HTTPClient();
        byte[] content = httpClient.get(origin).getBytes();
        return new IgnoredRobotsTxt(origin.toString(), content);
    }
}