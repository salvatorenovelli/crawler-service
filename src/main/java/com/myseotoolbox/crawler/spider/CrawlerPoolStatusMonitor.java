package com.myseotoolbox.crawler.spider;

import lombok.extern.slf4j.Slf4j;

import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class CrawlerPoolStatusMonitor {

    private final Thread thread;

    public CrawlerPoolStatusMonitor(String name, ThreadPoolExecutor executor) {
        this.thread = new Thread(() -> {

            WeakReference<ThreadPoolExecutor> reference = new WeakReference<>(executor);
            while (true) {
                try {
                    Thread.sleep(5000);
                    ThreadPoolExecutor execRef = reference.get();
                    if (execRef == null || execRef.getActiveCount() < 1) {
                        log.info("No active threads. Terminating monitoring for Crawler: {} ({})", name, execRef);
                        break;
                    }

                    log.info("{} - {} Pool size: {} Active Threads: {} Queued Tasks: {} Completed Tasks: {}",
                            name,
                            getRunState(execRef),
                            execRef.getPoolSize(),
                            execRef.getActiveCount(),
                            execRef.getQueue().size(),
                            execRef.getCompletedTaskCount());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setDaemon(true);
    }

    private String getRunState(ThreadPoolExecutor execRef) {
        return execRef.isTerminated() ? "Terminated" : "Running";
    }

    public void start() {
        thread.start();
    }
}
