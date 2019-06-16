package com.myseotoolbox.crawler.spider.configuration;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



public class FilterConfigurationTest {

    @Test
    public void shouldNotIgnoreByDefault() {
        assertFalse(new FilterConfiguration(null).shouldIgnoreRobotsTxt());
    }

    @Test
    public void shouldTakeValue() {
        assertFalse(new FilterConfiguration(false).shouldIgnoreRobotsTxt());
        assertTrue(new FilterConfiguration(true).shouldIgnoreRobotsTxt());
    }
}