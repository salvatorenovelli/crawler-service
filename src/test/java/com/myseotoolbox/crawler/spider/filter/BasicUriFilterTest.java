package com.myseotoolbox.crawler.spider.filter;

import org.junit.Test;

import java.net.URI;

import static java.net.URI.create;
import static org.junit.Assert.*;

public class BasicUriFilterTest {

    private static final URI BASE_HTTP = create("http://host");
    private static final URI BASE_HTTPS = create("https://host");

    BasicUriFilter sutHttp = new BasicUriFilter(BASE_HTTP);
    BasicUriFilter sutHttps = new BasicUriFilter(BASE_HTTPS);

    @Test
    public void filterInvalidExtensions() {
        assertFalse(sutHttp.shouldCrawl(BASE_HTTP, create("http://host1/main.css")));
        assertFalse(sutHttp.shouldCrawl(BASE_HTTP, create("http://host1/img.png")));
    }

    @Test
    public void differentScheme() {
        assertTrue(sutHttp.shouldCrawl(BASE_HTTP, create("https://host/salve")));
        assertTrue(sutHttps.shouldCrawl(BASE_HTTPS, create("http://host/salve")));
    }

    @Test
    public void shouldNotCrawlDifferentPortsWhenDiscoveredInsideOrigin() {
        assertFalse(sutHttp.shouldCrawl(BASE_HTTP, create("http://host:8080/salve")));
    }

    @Test
    public void shouldNotCrawlDifferentPortsWhenDiscoveredOutOrigin() {
        assertFalse(sutHttp.shouldCrawl(create("http://anotherHost"), create("http://host:8080/salve")));
    }

    @Test
    public void port80IsSameAsNoPortSpecified() {
        assertTrue(sutHttp.shouldCrawl(BASE_HTTP, create("http://host:80/salve")));
    }

    @Test
    public void shouldAllowSubdomainsWhenLinkingFromInside() {
        //This is to allow discovery of "internal" broken links
        assertTrue(sutHttp.shouldCrawl(BASE_HTTP, create("http://sub.host")));
    }

    @Test
    public void shouldNotAllowSubdomainsWhenLinkingFromOutside() {
        //we crawl subdomains only to verify broken links, no value in crawling this by that logic
        assertFalse(sutHttp.shouldCrawl(create("http://anotherHost"), create("http://sub.host")));
    }

    @Test
    public void shouldCrawlWWW__fromNonWWW() {
        assertTrue(sutHttp.shouldCrawl(BASE_HTTP, create("http://www.host")));
    }

    @Test
    public void shouldCrawlNonWWW__fromWWW_nonWWWBase() {
        assertTrue(sutHttp.shouldCrawl(create("https://www.host"), create("https://host")));
        assertTrue(sutHttp.shouldCrawl(create("https://host"), create("https://host/link")));
    }

    @Test
    public void shouldCrawlNonWWW__fromWWW_wwwBase() {
        sutHttp = new BasicUriFilter(create("https://www.host"));
        assertTrue(sutHttp.shouldCrawl(create("https://www.host"), create("https://host")));
        assertTrue(sutHttp.shouldCrawl(create("https://host"), create("https://host/link")));
    }

    @Test
    public void shouldNotCrawlEntirelyDifferentDomains() {
        //we don't care about external broken links for now
        assertFalse(sutHttp.shouldCrawl(BASE_HTTP, create("http://differentHost")));
    }

    @Test
    public void shouldBeAbleToDealWithNullPath() {
        BasicUriFilter sut = new BasicUriFilter(create("http://host/base"));
        assertFalse(sut.shouldCrawl(create("http://host/outside"), create("mailto:info@host")));
    }

    @Test
    public void shouldOnlyAllowValidSchemes() {
        assertFalse(sutHttp.shouldCrawl(BASE_HTTP, create("mailto:info@host")));
        assertFalse(sutHttp.shouldCrawl(BASE_HTTP, create("ftp://host")));
        //wrong scheme
        assertFalse(sutHttp.shouldCrawl(BASE_HTTP, create("ftp//host")));
    }

    @Test
    public void hostIsCaseInsensitive() {
        assertTrue(sutHttp.shouldCrawl(BASE_HTTP, create("http://HOST/")));
    }
}
