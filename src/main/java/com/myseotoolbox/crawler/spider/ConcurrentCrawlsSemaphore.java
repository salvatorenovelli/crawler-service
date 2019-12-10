package com.myseotoolbox.crawler.spider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

@Slf4j
public class ConcurrentCrawlsSemaphore {

    private final Semaphore semaphore;

    public ConcurrentCrawlsSemaphore(int maxConcurrentCrawls) {
        log.info("Initializing crawl semaphore with {} concurrent crawls", maxConcurrentCrawls);
        this.semaphore = new Semaphore(maxConcurrentCrawls);
    }

    @SneakyThrows
    public void acquire() {
        log.info("Acquiring crawl permit...");
        semaphore.acquire();
        log.info("Crawl permit Acquired. Available: ~{}, Queue: ~{}", semaphore.availablePermits(), semaphore.getQueueLength());
    }

    public void release() {
        log.info("Releasing crawl permit. Available: {}, Queue: {}", semaphore.availablePermits(), semaphore.getQueueLength());
        semaphore.release();
    }
}
