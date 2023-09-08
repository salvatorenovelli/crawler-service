package com.myseotoolbox.crawler.spider.ratelimiter;

import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Slf4j
public class RateLimiter {
    private final long minDelayMillis;
    private long nextAvailableTime;
    private final ClockUtils clockUtils;

    public RateLimiter(long minDelayMillis, ClockUtils clockUtils) {
        if (minDelayMillis < 0) {
            throw new IllegalArgumentException("Rate must be at least 0");
        }
        this.minDelayMillis = minDelayMillis;
        this.clockUtils = clockUtils;
        this.nextAvailableTime = clockUtils.currentTimeMillis();
    }

    @SneakyThrows
    public synchronized void throttle() {
        if (minDelayMillis == 0) return;
        long currentTime = clockUtils.currentTimeMillis();
        if (currentTime < nextAvailableTime) {
            long delay = nextAvailableTime - currentTime;
            log.trace("Throttling for {} ms", delay);
            clockUtils.sleep(delay);
        }
        nextAvailableTime = clockUtils.currentTimeMillis() + minDelayMillis;
    }
}
