package com.myseotoolbox.crawler.spider.ratelimiter;

import com.myseotoolbox.crawler.spider.configuration.ClockUtils;

public class TestClockUtils implements ClockUtils {
    private long currentTime = 0;

    public long currentTimeMillis() {
        return currentTime;
    }

    public void sleep(long delayMillis) {
        currentTime += delayMillis;
    }

}