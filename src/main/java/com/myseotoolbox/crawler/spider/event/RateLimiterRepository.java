package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import com.myseotoolbox.crawler.spider.ratelimiter.RateLimiter;
import com.myseotoolbox.crawler.spider.ratelimiter.TimeBasedRateLimiter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class RateLimiterRepository {
    private final Map<String, RateLimiter> rateLimiters;
    private final int intervalMillis;
    private final ClockUtils clockUtils;

    public RateLimiterRepository(int intervalMillis, ClockUtils clockUtils, int maxEntryCount) {
        this.intervalMillis = intervalMillis;
        this.clockUtils = clockUtils;
        this.rateLimiters = new LinkedHashMap<String, RateLimiter>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, RateLimiter> eldest) {
                return size() > maxEntryCount;
            }
        };
    }

    public synchronized RateLimiter getFor(String key) {
        rateLimiters.putIfAbsent(key, createRateLimiter());
        return rateLimiters.get(key);
    }

    private RateLimiter createRateLimiter() {
        if (intervalMillis > 0) {
            return new TimeBasedRateLimiter(intervalMillis, clockUtils);
        }
        return RateLimiter.UNLIMITED_RATE_LIMITER;
    }
}


