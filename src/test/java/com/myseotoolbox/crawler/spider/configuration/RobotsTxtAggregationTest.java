package com.myseotoolbox.crawler.spider.configuration;

import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.spider.TestWorkspaceBuilder;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.testutils.testwebsite.RobotsTxtBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.myseotoolbox.crawler.spider.configuration.CrawlerSettingsBuilder.defaultSettings;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RobotsTxtAggregationTest {

    @Mock private HTTPClient httpClient;
    RobotsTxtAggregation sut;
    private List<Workspace> allWorkspaces = new ArrayList<>();


    @Before
    public void setUp() {
        sut = new RobotsTxtAggregation(httpClient);
        givenRobotTxtFor("http://host1").userAgent("*").disallow("/blocked-by-robots").build();
    }

    @Test
    public void singleConfShouldIgnoreRobots() {
        givenAWorkspace().withWebsiteUrl("http://host1").withCrawlerSettings(defaultSettings().withIgnoreRobotsTxt(true).build()).build();
        RobotsTxt aggregate = sut.mergeConfigurations(allWorkspaces);
        assertTrue(aggregate.shouldCrawl(uri("http://host1"), uri("http://host1/blocked-by-robots")));
    }

    @Test
    public void singleConfShouldUseRobotsTxtContent() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        RobotsTxt robots = sut.mergeConfigurations(allWorkspaces);
        assertFalse(robots.shouldCrawl(uri("http://host1"), uri("http://host1/blocked-by-robots")));
    }


    @Test
    public void multipleConfShouldIgnoreRobots() {
        givenAWorkspace().withWebsiteUrl("http://host1/b").build();
        givenAWorkspace().withWebsiteUrl("http://host1/a").withCrawlerSettings(defaultSettings().withIgnoreRobotsTxt(true).build()).build();

        RobotsTxt robots = sut.mergeConfigurations(allWorkspaces);
        assertTrue(robots.shouldCrawl(uri("http://host1"), uri("http://host1/blocked-by-robots")));
    }


    @Test
    public void shouldHandleNullFilterConfiguration() {
        givenAWorkspace().withWebsiteUrl("http://host1").withCrawlerSettings(null).build();
        RobotsTxt robots = sut.mergeConfigurations(allWorkspaces);
        assertFalse(robots.shouldCrawl(uri("http://host1"), uri("http://host1/blocked-by-robots")));
    }

    private RobotsTxtBuilder givenRobotTxtFor(String uri) {
        return new RobotsTxtBuilder(s -> configureMockHttpClientFor(uri, s));
    }

    private OngoingStubbing<String> configureMockHttpClientFor(String uri, String s) {
        try {
            return when(httpClient.get(URI.create(uri).resolve("/robots.txt"))).thenReturn(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TestWorkspaceBuilder givenAWorkspace() {
        return new TestWorkspaceBuilder(allWorkspaces, null);
    }

    private URI uri(String s) {
        return URI.create(s);
    }
}