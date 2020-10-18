package com.myseotoolbox.crawler.spider.filter.robotstxt;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class EmptyRobotsTxtTest {

    @Test
    public void shouldProvideEmptySitemap() {
        RobotsTxt sut = new EmptyRobotsTxt(null);
        assertThat(sut.getSitemaps(), hasSize(0));
    }

    @Test
    public void shouldProvideDefaultSitemap() {
        RobotsTxt sut = new EmptyRobotsTxt(URI.create("http://host"));
        assertThat(sut.getSitemaps(), containsInAnyOrder("http://host/sitemap.xml"));
    }
}