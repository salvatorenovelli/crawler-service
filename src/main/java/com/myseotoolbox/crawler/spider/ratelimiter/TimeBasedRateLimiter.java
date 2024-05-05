package com.myseotoolbox.crawler.spider.ratelimiter;

import com.myseotoolbox.crawler.spider.configuration.ClockUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;

@ThreadSafe
public class TimeBasedRateLimiter implements RateLimiter {
    private final long intervalMillis;
    private long lastExecutionTime;
    private final ClockUtils clockUtils;

    public TimeBasedRateLimiter(long intervalMillis, ClockUtils clockUtils) {
        if (intervalMillis < 0) {
            throw new IllegalArgumentException("Interval must be at least 0");
        }
        this.intervalMillis = intervalMillis;
        this.clockUtils = clockUtils;
        this.lastExecutionTime = clockUtils.currentTimeMillis() - intervalMillis;
    }

    @Override
    public synchronized <T> Optional<T> process(Task<T> task) {
        long currentTime = clockUtils.currentTimeMillis();
        if (currentTime - lastExecutionTime >= intervalMillis) {
            lastExecutionTime = currentTime;
            T result = task.execute();
            return Optional.ofNullable(result);
        }
        return Optional.empty();
    }
}