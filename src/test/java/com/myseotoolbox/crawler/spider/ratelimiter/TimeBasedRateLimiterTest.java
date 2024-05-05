package com.myseotoolbox.crawler.spider.ratelimiter;


import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class TimeBasedRateLimiterTest {

    private final TestClockUtils clockUtils = new TestClockUtils();
    private final TimeBasedRateLimiter limiter = new TimeBasedRateLimiter(1000, clockUtils);
    private final RateLimiter.Task<String> TEST_TASK = () -> "Executed at " + clockUtils.currentTimeMillis();


    @Test
    public void shouldExecuteOnlyOncePerInterval() {
        Optional<String> firstResult = limiter.process(TEST_TASK);
        assertTrue(firstResult.isPresent());
        assertEquals("Executed at 0", firstResult.get());

        clockUtils.sleep(999);
        Optional<String> secondResult = limiter.process(TEST_TASK);
        assertFalse(secondResult.isPresent());

        clockUtils.sleep(1);
        Optional<String> thirdResult = limiter.process(TEST_TASK);
        assertTrue(thirdResult.isPresent());
        assertEquals("Executed at 1000", thirdResult.get());
    }
}
