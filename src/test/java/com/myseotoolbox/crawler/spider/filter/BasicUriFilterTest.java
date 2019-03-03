package com.myseotoolbox.crawler.spider.filter;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class BasicUriFilterTest {

    private static final URI BASE = URI.create("http://host");

    @Test
    public void shouldFilterCss() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertFalse(sut.shouldCrawl(BASE, URI.create("http://host1/main.css")));
    }

    @Test
    public void shouldAllowHttpsWhenStartingFromHttp() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertTrue(sut.shouldCrawl(BASE, URI.create("https://host/salve")));
    }

    @Test
    public void shouldNotCrawlDifferentPort() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertFalse(sut.shouldCrawl(BASE, URI.create("https://host:8080/salve")));
    }

    @Test
    public void port80IsSameAsNoPortSpecified() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertTrue(sut.shouldCrawl(BASE, URI.create("https://host:80/salve")));
    }


    @Test
    public void www() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://www.host"));
        assertFalse(sut.shouldCrawl(BASE, URI.create("https://host")));
    }

    @Test
    public void shouldAllowUriOutsideBaseIfDiscoveredInsideBase() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host/base"));
        assertTrue(sut.shouldCrawl(URI.create("http://host/base"), URI.create("http://host/base/1")));
        assertTrue(sut.shouldCrawl(URI.create("http://host/base"), URI.create("http://host")));
        assertTrue(sut.shouldCrawl(URI.create("http://host/base"), URI.create("http://host/outside")));
    }

    @Test
    public void shouldDisallowUriWithDestinationOutsideBaseWhenDiscoveredOutsideBase() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host/base"));
        assertFalse(sut.shouldCrawl(URI.create("http://host"), URI.create("http://host/outside")));
    }

    @Test
    public void shouldAllowUriInsideBaseEvenIfDiscoveredOutsideBase() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host/base"));
        assertTrue(sut.shouldCrawl(URI.create("http://host"), URI.create("http://host/base/inside")));
    }

    @Test
    public void shouldBeAbleToRecognizeChild() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host/base"));
        assertFalse(sut.shouldCrawl(URI.create("http://host"), URI.create("http://host/basedisabled")));
    }

    @Test
    public void shouldAllowHttps() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertTrue(sut.shouldCrawl(URI.create("http://host"), URI.create("https://host/")));
    }

    @Test
    public void shouldBeFineWithBaseHavingTrailingSlashes() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host/base/"));

        assertTrue(sut.shouldCrawl(URI.create("http://host/base/1"), URI.create("http://host/base/inside")));
        assertTrue(sut.shouldCrawl(URI.create("http://host/base"), URI.create("http://host/base/inside")));
        assertTrue(sut.shouldCrawl(URI.create("http://host/base/"), URI.create("http://host/base/inside")));

        assertFalse(sut.shouldCrawl(URI.create("http://host/base1/"), URI.create("http://host/basedisabled")));
    }

    @Test
    public void shouldBeAbleToDealWithNullPath() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host/base"));
        assertFalse(sut.shouldCrawl(URI.create("http://host/outside"), URI.create("mailto:info@host")));
    }

    @Test
    public void shouldOnlyAllowValidSchemes() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertFalse(sut.shouldCrawl(URI.create("http://host"), URI.create("mailto:info@host")));
        assertFalse(sut.shouldCrawl(URI.create("http://host"), URI.create("ftp://host")));
        //wrong scheme
        assertFalse(sut.shouldCrawl(URI.create("http://host"), URI.create("ftp//host")));
    }

    @Test
    public void hostIsCaseInsensitive() {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertTrue(sut.shouldCrawl(URI.create("http://host"),URI.create("http://HOST/")));
    }
}
