package com.myseotoolbox.crawler.utils;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LinkResolverTest {
    @Test
    public void canResolveNormalLinks() {
        assertThat(LinkResolver.resolve(URI.create("http://host"), "/linkWithPath"), is(URI.create("http://host/linkWithPath")));
    }

    @Test
    public void canResolveRelativeLinksInRoot() {
        assertThat(LinkResolver.resolve(URI.create("http://host"), "relativeLink"), is(URI.create("http://host/relativeLink")));
    }
}