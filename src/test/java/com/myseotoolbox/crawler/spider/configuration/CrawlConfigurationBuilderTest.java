package com.myseotoolbox.crawler.spider.configuration;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CrawlConfigurationBuilderTest {


    private static final URI TEST_ORIGIN = URI.create("http://testhost");
    private CrawlConfiguration.Builder sut = CrawlConfiguration.newConfiguration(TEST_ORIGIN);


    @Test
    public void setOrigin() {
        CrawlConfiguration build = sut.build();
        assertThat(build.getOrigin(), is(TEST_ORIGIN));
        assertThat(build.getRobotsTxtConfiguration().getOrigin(), is(TEST_ORIGIN));
    }

    @Test
    public void shouldLimitNumberOfConcurrentConnections() {
        CrawlConfiguration build = sut.withConcurrentConnections(CrawlConfiguration.MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN + 100).build();
        assertThat(build.getMaxConcurrentConnections(), is(CrawlConfiguration.MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN));
    }
}