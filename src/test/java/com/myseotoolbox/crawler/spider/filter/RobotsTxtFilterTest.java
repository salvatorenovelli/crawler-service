package com.myseotoolbox.crawler.spider.filter;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RobotsTxtFilterTest {

    private RobotsTxtFilter sut;

    @Before
    public void setUp() throws Exception {
        InputStream stream = IOUtils.toInputStream("User-agent: *\nDisallow: /*order\n", "UTF-8");
        sut = new RobotsTxtFilter(stream);
    }

    @Test
    public void shouldDisallow() {
        assertFalse(sut.shouldCrawl(null, URI.create("http://domain/order")));
    }

    @Test
    public void shouldAllow() {
        assertTrue(sut.shouldCrawl(null, URI.create("http://domain/product")));
    }
}