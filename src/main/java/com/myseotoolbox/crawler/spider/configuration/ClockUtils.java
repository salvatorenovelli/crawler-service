package com.myseotoolbox.crawler.spider.configuration;

public interface ClockUtils {
    long currentTimeMillis();

    void sleep(long millis) throws InterruptedException;
}
