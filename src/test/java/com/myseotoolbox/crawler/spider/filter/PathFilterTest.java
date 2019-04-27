package com.myseotoolbox.crawler.spider.filter;

import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class PathFilterTest {


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
        assertTrue(sut.shouldCrawl("/base/path1"));
        assertTrue(sut.shouldCrawl("/base"));
        assertTrue(sut.shouldCrawl("/base/"));
    }

    @Test
    public void shouldNotCrawlOutsidePath() {
        PathFilter sut = new PathFilter(Collections.singletonList("/base"));
        assertFalse(sut.shouldCrawl("/outside/path2"));
        assertFalse(sut.shouldCrawl("/base2/path2"));
        assertFalse(sut.shouldCrawl("/basepath2")); //this is the tricky one ^^
    }

    @Test
    public void isTolerantToLeadingSlash() {

        String path = URI.create("http://domain/base/").getPath();

        PathFilter sut = new PathFilter(Collections.singletonList(path));
        assertTrue(sut.shouldCrawl("/base/path1"));
        assertTrue(sut.shouldCrawl("/base"));
        assertTrue(sut.shouldCrawl("/base/"));
    }

    @Test
    public void shouldNotCrawlParentOfBase() {
        PathFilter sut = new PathFilter(Collections.singletonList("/parent/base"));

        assertTrue(sut.shouldCrawl("/parent/base"));
        assertFalse(sut.shouldCrawl("/parent"));
        assertFalse(sut.shouldCrawl("/parent/base2"));
    }

    @Test
    public void shouldAllowMultipleBase() {
        PathFilter sut = new PathFilter(Arrays.asList("/base1/**", "/base2/"));
        assertTrue(sut.shouldCrawl("/base1"));
        assertTrue(sut.shouldCrawl("/base1/2"));
        assertTrue(sut.shouldCrawl("/base2/3"));
    }

    @Test
    public void shouldBeAbleToExcludePathsEvenWithMultipleBase() {
        PathFilter sut = new PathFilter(Arrays.asList("/base1", "/base2"));
        assertFalse(sut.shouldCrawl("/base3/base2"));
    }
}