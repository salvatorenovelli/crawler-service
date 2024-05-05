package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import com.myseotoolbox.crawler.spider.ratelimiter.RateLimiter;
import com.myseotoolbox.crawler.spider.ratelimiter.TestClockUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class RateLimiterRepositoryTest {

    private static final int MAX_ENTRY_COUNT = 20;
    private ClockUtils testClockUtils = new TestClockUtils();
    private final RateLimiterRepository sut = new RateLimiterRepository(100, testClockUtils, MAX_ENTRY_COUNT);

    @Test
    public void shouldReturnARateLimiter() {
        assertNotNull(sut.getFor("A"));
    }

    @Test
    public void shouldCache() {
        assertSame(sut.getFor("A"), sut.getFor("A"));
    }

    @Test
    public void shouldReturnDifferentLimitersForDifferentKeys() {
        assertNotSame(sut.getFor("A"), sut.getFor("B"));
    }

    @Test
    public void shouldGarbageCollect() {
        RateLimiter firstLimiter = sut.getFor("A");
        for (int i = 0; i < MAX_ENTRY_COUNT; i++) {
            sut.getFor("" + i);
        }
        assertNotSame(firstLimiter, sut.getFor("A"));
    }
}