package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;

import static com.myseotoolbox.crawler.spider.configuration.AllowedPathFromSeeds.extractAllowedPathFromSeeds;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WebsiteUriFilterFactoryTest {

    WebsiteUriFilterFactory sut = new WebsiteUriFilterFactory();


    @Test
    public void shouldAllowCrawlOutsideAllowedPathIfLinkWasDiscoveredInsideAllowedPath() {

        URI origin = URI.create("http://testhost/subpath/");
        URI allowed = origin.resolve("/allowed");
        UriFilter build = sut.build(origin, extractAllowedPathFromSeeds(Collections.singletonList(allowed)), EmptyRobotsTxt.instance());

        assertTrue(build.shouldCrawl(allowed, origin.resolve("/salve")));
        assertFalse(build.shouldCrawl(origin.resolve("/outside"), origin.resolve("/salve1")));
    }

    @Test
    public void shouldNotCrawlOtherDomains() {
        URI origin = URI.create("http://testhost/subpath/");
        URI allowed = origin.resolve("/allowed");
        UriFilter build = sut.build(origin, extractAllowedPathFromSeeds(Collections.singletonList(allowed)), EmptyRobotsTxt.instance());

        assertFalse(build.shouldCrawl(allowed, URI.create("http://another-host").resolve("/allowed")));
    }
}