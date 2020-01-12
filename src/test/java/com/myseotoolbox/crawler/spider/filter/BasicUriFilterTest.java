package com.myseotoolbox.crawler.spider.filter;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class BasicUriFilterTest {

    private static final URI BASE = URI.create("http://host");

    BasicUriFilter sut = new BasicUriFilter(BASE);

    @Test
    public void shouldFilterCss() {
        assertFalse(sut.shouldCrawl(BASE, URI.create("http://host1/main.css")));
    }

    @Test
    public void shouldAllowHttpsWhenStartingFromHttp() {
        assertTrue(sut.shouldCrawl(BASE, URI.create("https://host/salve")));
    }

    @Test
    public void shouldNotCrawlDifferentPortsWhenDiscoveredInsideOrigin() {
        assertFalse(sut.shouldCrawl(BASE, URI.create("http://host:8080/salve")));
    }

    @Test
    public void shouldNotCrawlDifferentPortsWhenDiscoveredOutOrigin() {
        assertFalse(sut.shouldCrawl(URI.create("http://anotherHost"), URI.create("http://host:8080/salve")));
    }

    @Test
    public void port80IsSameAsNoPortSpecified() {
        assertTrue(sut.shouldCrawl(BASE, URI.create("http://host:80/salve")));
    }

    @Test
    public void shouldAllowSubdomainsWhenLinkingFromInside() {
        assertTrue(sut.shouldCrawl(BASE, URI.create("http://sub.host")));
    }

    @Test
    public void shouldCrawlWWW__fromNonWWW() {
        assertTrue(sut.shouldCrawl(BASE, URI.create("http://www.host")));
    }

    @Test
    public void shouldNotAllowSubdomainsWhenLinkingFromOutside() {
        assertFalse(sut.shouldCrawl(URI.create("http://anotherHost"), URI.create("http://sub.host")));
    }


    @Test
    public void shouldNotCrawlEntirelyDifferentDomains() {
        assertFalse(sut.shouldCrawl(BASE, URI.create("http://differentHost")));
    }

    @Test
    public void shouldAllowUriOutsideBaseIfDiscoveredInsideBase() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host/base"));
        assertTrue(sut.shouldCrawl(URI.create("http://host/base"), URI.create("http://host/base/1")));
        assertTrue(sut.shouldCrawl(URI.create("http://host/base"), BASE));
        assertTrue(sut.shouldCrawl(URI.create("http://host/base"), URI.create("http://host/outside")));
    }


    @Test
    public void shouldAllowUriInsideBaseEvenIfDiscoveredOutsideBase() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host/base"));
        assertTrue(sut.shouldCrawl(BASE, URI.create("http://host/base/inside")));
    }


    @Test
    public void shouldAllowHttps() {
        assertTrue(sut.shouldCrawl(BASE, URI.create("https://host/")));
    }

    @Test
    public void shouldBeAbleToDealWithNullPath() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host/base"));
        assertFalse(sut.shouldCrawl(URI.create("http://host/outside"), URI.create("mailto:info@host")));
    }

    @Test
    public void shouldOnlyAllowValidSchemes() {
        assertFalse(sut.shouldCrawl(BASE, URI.create("mailto:info@host")));
        assertFalse(sut.shouldCrawl(BASE, URI.create("ftp://host")));
        //wrong scheme
        assertFalse(sut.shouldCrawl(BASE, URI.create("ftp//host")));
    }

    @Test
    public void hostIsCaseInsensitive() {
        assertTrue(sut.shouldCrawl(BASE, URI.create("http://HOST/")));
    }
}
