package com.myseotoolbox.crawler.spider;

import org.junit.Test;

import static org.junit.Assert.*;

public class PathMatcherTest {

    @Test
    public void shouldMatchSubPath() {
        assertTrue(PathMatcher.isSubPath("/base/", "/base"));
        assertTrue(PathMatcher.isSubPath("/base/", "/base/something"));
        assertTrue(PathMatcher.isSubPath("/base/", "/base/subpath/something"));
    }

    @Test
    public void toleratePathEndingWithFiles() {
        assertTrue(PathMatcher.isSubPath("/base/index.html", "/base/somethingElse"));
    }

    @Test
    public void canManageEmptyPath() {
        assertTrue(PathMatcher.isSubPath("", ""));
        assertTrue(PathMatcher.isSubPath("", "/salve"));
    }
}