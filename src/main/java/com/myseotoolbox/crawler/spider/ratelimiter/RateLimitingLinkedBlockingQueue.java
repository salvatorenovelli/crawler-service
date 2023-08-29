package com.myseotoolbox.crawler.spider.ratelimiter;

import java.util.concurrent.LinkedBlockingQueue;

public class RateLimitingLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

    private final RateLimiter rateLimiter;

    public RateLimitingLinkedBlockingQueue(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public E take() throws InterruptedException {
        rateLimiter.throttle();
        return super.take();
    }
}
