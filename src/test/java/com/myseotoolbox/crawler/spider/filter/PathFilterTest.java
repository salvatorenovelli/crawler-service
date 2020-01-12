package com.myseotoolbox.crawler.spider.filter;

import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathFilterTest {


    private URI SOURCE_URI = URI.create("http://testhost/");

    @Test(expected = IllegalArgumentException.class)
    public void canOnlyAcceptAbsolutePath() {
        new PathFilter(Collections.singletonList("base/something"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldOnlyAcceptPath() {
        new PathFilter(Collections.singletonList("http://domain/base"));
    }

    @Test
    public void shouldCrawlInsidePath() {
        PathFilter sut = new PathFilter(Collections.singletonList("/base/"));
        assertTrue(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/base")));
        assertTrue(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/base/")));
        assertTrue(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/base/path1")));
    }

    @Test
    public void shouldNotCrawlOutsidePath() {
        PathFilter sut = new PathFilter(Collections.singletonList("/base/"));
        assertFalse(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/outside/path2")));
        assertFalse(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/base2/path2")));
        assertFalse(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/basepath2"))); //this is the tricky one ^^
    }

    @Test
    public void isNotTolerantToMissingLeadingSlash() {
        String path = URI.create("http://domain/base").getPath();
        PathFilter sut = new PathFilter(Collections.singletonList(path));
        assertTrue(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/base2/path1")));
    }

    @Test
    public void canFilterBySubPath() {
        PathFilter sut = new PathFilter(Collections.singletonList("/path/subpath/"));

        assertTrue(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/path/subpath")));
        assertTrue(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/path/subpath/something")));
        assertFalse(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/path")));
        assertFalse(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/path/subpath2/something")));
    }

    @Test
    public void shouldAllowMultipleBase() {
        PathFilter sut = new PathFilter(Arrays.asList("/base1/", "/base2/"));
        assertTrue(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/base1/")));
        assertTrue(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/base1/2")));
        assertTrue(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/base2/3")));
        assertFalse(sut.shouldCrawl(SOURCE_URI, SOURCE_URI.resolve("/base3/1")));
    }

    @Test
    public void shouldAllowCrawlOutsideAllowedPathIfComingFromInside() {
        PathFilter sut = new PathFilter(Arrays.asList("/base1/", "/base2/"));
        assertTrue(sut.shouldCrawl(SOURCE_URI.resolve("/base2/"), SOURCE_URI.resolve("/base3/base2")));
        assertTrue(sut.shouldCrawl(SOURCE_URI.resolve("/base1/"), SOURCE_URI.resolve("/something-on-root")));
    }

    @Test
    public void shouldNotAllowCrawlOfUriComingFromOutsideAllowedPath() {
        PathFilter sut = new PathFilter(Arrays.asList("/base1/", "/base2/"));
        assertFalse(sut.shouldCrawl(SOURCE_URI.resolve("/base3/"), SOURCE_URI.resolve("/something-on-root")));
    }
}