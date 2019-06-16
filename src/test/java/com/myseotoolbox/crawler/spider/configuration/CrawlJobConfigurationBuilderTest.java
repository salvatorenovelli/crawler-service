package com.myseotoolbox.crawler.spider.configuration;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CrawlJobConfigurationBuilderTest {


    private static final URI TEST_ORIGIN = URI.create("http://testhost");
    private CrawlJobConfiguration.Builder sut = CrawlJobConfiguration.newConfiguration(TEST_ORIGIN);


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
}