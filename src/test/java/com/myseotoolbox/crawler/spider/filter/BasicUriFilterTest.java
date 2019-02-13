package com.myseotoolbox.crawler.spider.filter;

import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BasicUriFilterTest {

    @Test
    public void shouldFilterCss() throws IOException {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertFalse(sut.test(URI.create("http://host1/main.css")));
    }

    @Test
    public void shouldAllowHttpsWhenStartingFromHttp() throws IOException {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertTrue(sut.test(URI.create("https://host/salve")));
    }

    @Test
    public void shouldNotCrawlDifferentPort() throws IOException {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertFalse(sut.test(URI.create("https://host:8080/salve")));
    }

    @Test
    public void port80IsSameAsNoPortSpecified() throws IOException {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://host"));
        assertTrue(sut.test(URI.create("https://host:80/salve")));
    }


    @Test
    public void www() throws IOException {
        BasicUriFilter sut = new BasicUriFilter(URI.create("http://www.host.com"));
        assertFalse(sut.test(URI.create("https://host.com")));
    }


}