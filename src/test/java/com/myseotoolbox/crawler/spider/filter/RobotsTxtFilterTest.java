package com.myseotoolbox.crawler.spider.filter;

import com.panforge.robotstxt.RobotsTxt;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.junit.Assert.*;

public class RobotsTxtFilterTest {

    private RobotsTxtFilter sut;

    @Before
    public void setUp() throws Exception {
        InputStream stream = IOUtils.toInputStream("User-agent: *\nDisallow: /*order\n", "UTF-8");
        sut = new RobotsTxtFilter(stream);
    }

    @Test
    public void shouldDisallow() throws IOException {
        assertFalse(sut.test(URI.create("http://domain/order")));
    }

    @Test
    public void shouldAllow() throws IOException {
        assertTrue(sut.test(URI.create("http://domain/product")));
    }
}