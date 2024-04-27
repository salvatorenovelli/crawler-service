package com.myseotoolbox.crawler.spider.ratelimiter;

import com.myseotoolbox.crawler.spider.configuration.ClockUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;
import java.util.function.Supplier;

@ThreadSafe
public class RateLimiter<T> {
    private final long intervalMillis;
    private long lastExecutionTime;
    private final ClockUtils clockUtils;

    public RateLimiter(long intervalMillis, ClockUtils clockUtils) {
        if (intervalMillis < 0) {
            throw new IllegalArgumentException("Interval must be at least 0");
        }
        this.intervalMillis = intervalMillis;
        this.clockUtils = clockUtils;
        this.lastExecutionTime = clockUtils.currentTimeMillis() - intervalMillis;
    }

    public synchronized Optional<T> process(Supplier<T> task) {
        long currentTime = clockUtils.currentTimeMillis();
        if (currentTime - lastExecutionTime >= intervalMillis) {
            lastExecutionTime = currentTime;
            T result = task.get();
            return Optional.of(result);
        }
        return Optional.empty();
    }
}