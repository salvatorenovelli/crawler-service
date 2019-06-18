package com.myseotoolbox.crawler.spider.configuration;

import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawlJobConfigurationBuilderTest {


    private RobotsTxt provided = EmptyRobotsTxt.instance();

    private static final URI TEST_ORIGIN = URI.create("http://testhost");
    private CrawlJobConfiguration.Builder sut = CrawlJobConfiguration.newConfiguration(TEST_ORIGIN).withRobotsTxt(provided);
    @Mock private HTTPClient httpClient;


    @Test
    public void setOrigin() {
        CrawlJobConfiguration build = sut.build();
        assertThat(build.getOrigin(), is(TEST_ORIGIN));
    }

    @Test
    public void shouldLimitNumberOfConcurrentConnections() {
        CrawlJobConfiguration build = sut.withConcurrentConnections(CrawlJobConfiguration.MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN + 100).build();
        assertThat(build.getMaxConcurrentConnections(), is(CrawlJobConfiguration.MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN));
    }

    @Test
    public void shouldFetchRobotsTxtIfDefaultIsRequired() throws Exception {
        when(httpClient.get(any())).thenReturn("User-agent: *\n" + "Disallow: /disabled\n");
        CrawlJobConfiguration conf = CrawlJobConfiguration.newConfiguration(TEST_ORIGIN).withDefaultRobotsTxt(httpClient).build();
        assertThat(conf.getRobotsTxt().shouldCrawl(null, TEST_ORIGIN.resolve("/disabled")), is(false));
        assertThat(conf.getRobotsTxt().shouldCrawl(null, TEST_ORIGIN.resolve("/something")), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfRobotsTxtIsNotConfigured() {
        CrawlJobConfiguration.newConfiguration(TEST_ORIGIN).build();
    }
}