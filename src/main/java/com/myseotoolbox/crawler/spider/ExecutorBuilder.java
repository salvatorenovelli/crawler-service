package com.myseotoolbox.crawler.spider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorBuilder {

    private static final AtomicInteger threadId = new AtomicInteger(0);

    public static ExecutorService buildExecutor(int concurrentConnections) {
        ThreadFactory factory = r -> {
            Thread thread = new Thread(r);
            thread.setName("crawler-" + threadId.getAndIncrement());
            return thread;
        };
        return Executors.newFixedThreadPool(concurrentConnections, factory);
    }
}
