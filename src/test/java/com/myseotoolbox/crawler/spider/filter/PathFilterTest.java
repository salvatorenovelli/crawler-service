package com.myseotoolbox.crawler.spider.filter;

import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathFilterTest {


    private URI TEST_ORIGIN = URI.create("http://testhost");

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
        PathFilter sut = new PathFilter(Collections.singletonList("/base"));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base/path1")));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base")));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base/")));
    }

    @Test
    public void shouldNotCrawlOutsidePath() {
        PathFilter sut = new PathFilter(Collections.singletonList("/base"));
        assertFalse(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/outside/path2")));
        assertFalse(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base2/path2")));
        assertFalse(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/basepath2"))); //this is the tricky one ^^
    }

    @Test
    public void isTolerantToLeadingSlash() {

        String path = URI.create("http://domain/base/").getPath();

        PathFilter sut = new PathFilter(Collections.singletonList(path));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base/path1")));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base")));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base/")));
    }

    @Test
    public void shouldNotCrawlParentOfBase() {
        PathFilter sut = new PathFilter(Collections.singletonList("/parent/base"));

        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/parent/base")));
        assertFalse(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/parent")));
        assertFalse(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/parent/base2")));
    }

    @Test
    public void shouldAllowMultipleBase() {
        PathFilter sut = new PathFilter(Arrays.asList("/base1/**", "/base2/"));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base1")));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base1/2")));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base2/3")));
    }

    @Test
    public void shouldBeAbleToExcludePathsEvenWithMultipleBase() {
        PathFilter sut = new PathFilter(Arrays.asList("/base1", "/base2"));
        assertFalse(sut.shouldCrawl(TEST_ORIGIN, TEST_ORIGIN.resolve("/base3/base2")));
    }

    @Test
    public void shouldAllowCrawlOutsideAllowedPathIfComingFromInside() {
        PathFilter sut = new PathFilter(Arrays.asList("/base1", "/base2"));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN.resolve("/base2"), TEST_ORIGIN.resolve("/base3/base2")));
        assertTrue(sut.shouldCrawl(TEST_ORIGIN.resolve("/base1"), TEST_ORIGIN.resolve("/something-on-root")));
    }

    @Test
    public void shouldNotAllowCrawlOfUriComingFromOutsideAllowedPath() {
        PathFilter sut = new PathFilter(Arrays.asList("/base1", "/base2"));
        assertFalse(sut.shouldCrawl(TEST_ORIGIN.resolve("/base3"), TEST_ORIGIN.resolve("/something-on-root")));
    }
}