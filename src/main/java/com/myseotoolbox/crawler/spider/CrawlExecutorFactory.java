package com.myseotoolbox.crawler.spider;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CrawlExecutorFactory {

    private final AtomicInteger threadId = new AtomicInteger(0);

    public ThreadPoolExecutor buildExecutor(String namePostfix, int concurrentConnections) {
        ThreadFactory factory = r -> {
            Thread thread = new Thread(r);
            thread.setName("crawler-" + namePostfix + "-" + threadId.getAndIncrement());
            return thread;
        };
        return new ThreadPoolExecutor(concurrentConnections, concurrentConnections, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), factory);
    }
}
