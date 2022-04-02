package com.myseotoolbox.crawler.spider.configuration;

import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.spider.filter.robotstxt.DefaultRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;

import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.MAX_CONCURRENT_CONNECTIONS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawlJobConfigurationBuilderTest {


    private RobotsTxt provided = new EmptyRobotsTxt(null);

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
        CrawlJobConfiguration build = sut.withConcurrentConnections(MAX_CONCURRENT_CONNECTIONS + 100).build();
        assertThat(build.getMaxConcurrentConnections(), is(MAX_CONCURRENT_CONNECTIONS));
    }

    @Test
    public void shouldFetchRobotsTxtIfDefaultIsRequired() throws Exception {
        when(httpClient.get(any())).thenReturn("User-agent: *\n" + "Disallow: /disabled\n");
        CrawlJobConfiguration conf = CrawlJobConfiguration.newConfiguration(TEST_ORIGIN).withRobotsTxt(getDefault(TEST_ORIGIN)).build();
        assertThat(conf.getRobotsTxt().shouldCrawl(null, TEST_ORIGIN.resolve("/disabled")), is(false));
        assertThat(conf.getRobotsTxt().shouldCrawl(null, TEST_ORIGIN.resolve("/something")), is(true));
    }

    public RobotsTxt getDefault(URI origin) throws IOException {
        String s = httpClient.get(origin.resolve("/robots.txt"));
        return new DefaultRobotsTxt(origin.toString(), s.getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfRobotsTxtIsNotConfigured() {
        CrawlJobConfiguration.newConfiguration(TEST_ORIGIN).build();
    }
}