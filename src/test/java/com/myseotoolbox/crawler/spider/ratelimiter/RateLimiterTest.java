package com.myseotoolbox.crawler.spider.ratelimiter;


import org.junit.Before;
import org.junit.Test;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class RateLimiterTest {
    private RateLimiter rateLimiter;
    private TestClockUtils testClockUtils = new TestClockUtils();

    @Before
    public void setUp() {
        rateLimiter = new RateLimiter(500, testClockUtils);  // 1 call per second
    }


    @Test
    public void testRateLimiting() throws InterruptedException {
        rateLimiter.throttle();  // immediate, as it's the first call
        rateLimiter.throttle();  // should wait for about 500 ms in "mock time"
        assertEquals(500, testClockUtils.currentTimeMillis());
    }

    @Test
    public void testRateLimiterInitialization() {
        assertEquals(0, testClockUtils.currentTimeMillis());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRateLimiterThrowsExceptionForNegativeDelay() {
        new RateLimiter(-1, testClockUtils);
    }

    @Test
    public void testThrottleWithZeroMinDelay() throws InterruptedException {
        TestClockUtils spyClockUtils = spy(testClockUtils);
        rateLimiter = new RateLimiter(0, spyClockUtils);
        rateLimiter.throttle();
        rateLimiter.throttle();
        rateLimiter.throttle();
        verify(spyClockUtils, never()).sleep(anyLong());
    }


    // Mock time source for testing

}
