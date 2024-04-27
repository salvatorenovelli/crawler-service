package com.myseotoolbox.crawler.spider.ratelimiter;


import org.junit.Before;
import org.junit.Test;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class TimeBasedThrottlerTest {
    private TimeBasedThrottler timeBasedThrottler;
    private TestClockUtils testClockUtils = new TestClockUtils();

    @Before
    public void setUp() {
        timeBasedThrottler = new TimeBasedThrottler(500, testClockUtils);  // 1 call per second
    }


    @Test
    public void testRateLimiting() throws InterruptedException {
        timeBasedThrottler.throttle();  // immediate, as it's the first call
        timeBasedThrottler.throttle();  // should wait for about 500 ms in "mock time"
        assertEquals(500, testClockUtils.currentTimeMillis());
    }

    @Test
    public void testRateLimitingWithMultipleRequests() throws InterruptedException {
        timeBasedThrottler.throttle();  // immediate, as it's the first call
        timeBasedThrottler.throttle();  // should wait for about 500 ms in "mock time"
        timeBasedThrottler.throttle();  // should wait for about 500 ms in "mock time"
        timeBasedThrottler.throttle();  // should wait for about 500 ms in "mock time"
        timeBasedThrottler.throttle();  // should wait for about 500 ms in "mock time"
        assertEquals(2000, testClockUtils.currentTimeMillis());
    }

    @Test
    public void testRateLimiterInitialization() {
        assertEquals(0, testClockUtils.currentTimeMillis());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRateLimiterThrowsExceptionForNegativeDelay() {
        new TimeBasedThrottler(-1, testClockUtils);
    }

    @Test
    public void testThrottleWithZeroMinDelay() throws InterruptedException {
        TestClockUtils spyClockUtils = spy(testClockUtils);
        timeBasedThrottler = new TimeBasedThrottler(0, spyClockUtils);
        timeBasedThrottler.throttle();
        timeBasedThrottler.throttle();
        timeBasedThrottler.throttle();
        verify(spyClockUtils, never()).sleep(anyLong());
    }


    // Mock time source for testing

}
