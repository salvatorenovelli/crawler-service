package com.myseotoolbox.crawler.spider.configuration;

import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.spider.filter.robotstxt.DefaultRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.websitecrawl.CrawlTrigger;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration.newConfiguration;
import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.MAX_CONCURRENT_CONNECTIONS;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawlJobConfigurationBuilderTest {


    private final RobotsTxt provided = new EmptyRobotsTxt(null);

    private static final URI TEST_ORIGIN = URI.create("http://testhost");
    private CrawlJobConfiguration.Builder sut = newConfiguration("testOwner", TEST_ORIGIN).withRobotsTxt(provided).withTriggerForUserInitiatedCrawlWorkspace(22);
    @Mock private HTTPClient httpClient;

    @Before
    public void setUp() throws Exception {
        when(httpClient.get(any())).thenReturn("User-agent: *\n" + "Disallow: /disabled\n");
    }

    @Test
    public void setOrigin() {
        CrawlJobConfiguration build = sut.build();
        assertThat(build.getOrigin(), is(TEST_ORIGIN));
    }

    @Test
    public void shouldLimitNumberOfConcurrentConnections() {
        CrawlJobConfiguration build = sut.withConcurrentConnections(MAX_CONCURRENT_CONNECTIONS + 100).build();
        assertThat(build.getMaxConcurrentConnections(), is(MAX_CONCURRENT_CONNECTIONS));
    }

    @Test
    public void shouldLimitCrawlFrequency() {
        CrawlJobConfiguration build = sut.withCrawlDelayMillis(500).build();
        assertThat(build.crawlDelayMillis(), is(500L));
    }

    @Test
    public void shouldFetchRobotsTxtIfDefaultIsRequired() throws Exception {
        CrawlJobConfiguration conf = newConfiguration("unitTest@myseotoolbox", TEST_ORIGIN).withTriggerForUserInitiatedCrawlWorkspace(223).withRobotsTxt(getDefault(TEST_ORIGIN)).build();
        assertThat(conf.getRobotsTxt().shouldCrawl(null, TEST_ORIGIN.resolve("/disabled")), is(false));
        assertThat(conf.getRobotsTxt().shouldCrawl(null, TEST_ORIGIN.resolve("/something")), is(true));
    }

    public RobotsTxt getDefault(URI origin) throws IOException {
        String s = httpClient.get(origin.resolve("/robots.txt"));
        return new DefaultRobotsTxt(origin.toString(), s.getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfRobotsTxtIsNotConfigured() {
        newConfiguration("unitTest@myseotoolbox", TEST_ORIGIN).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTriggerIsNotConfigured() throws IOException {
        newConfiguration("unitTest@myseotoolbox", TEST_ORIGIN).withRobotsTxt(getDefault(TEST_ORIGIN)).build();
    }

    @Test
    public void shouldBuildWebsiteCrawl() {
        CrawlJobConfiguration build = sut.build();
        assertThat(build.getWebsiteCrawl().getOrigin(), is(TEST_ORIGIN.toString()));
        //just a simple way to check that it's generating a new bson objectID
        assertThat(build.getWebsiteCrawl().getId(), isA(ObjectId.class));
    }

    @Test
    public void shouldSetScheduledTrigger() {
        CrawlJobConfiguration build = sut.withTriggerForScheduledScanOn(Arrays.asList(1, 2, 3)).build();
        assertThat(build.getWebsiteCrawl().getTrigger().getType(), is(CrawlTrigger.Type.SCHEDULED));
        assertThat(build.getWebsiteCrawl().getTrigger().getTargetWorkspaces(), containsInAnyOrder(1, 2, 3));
    }

    @Test
    public void shouldSetUserInitiatedTrigger() {
        CrawlJobConfiguration build = sut.withTriggerForUserInitiatedCrawlWorkspace(22).build();
        assertThat(build.getWebsiteCrawl().getTrigger().getType(), is(CrawlTrigger.Type.USER_INITIATED_WORKSPACE));
        assertThat(build.getWebsiteCrawl().getTrigger().getTargetWorkspaces(), containsInAnyOrder(22));
    }
}