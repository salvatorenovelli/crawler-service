package com.myseotoolbox.crawler.spider;

import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadPoolExecutor;

public class CrawlerPoolStatusMonitor {

    private final Thread thread;

    public CrawlerPoolStatusMonitor(ThreadPoolExecutor executor) {
        this.thread = new Thread(() -> {
            WeakReference<ThreadPoolExecutor> reference = new WeakReference<>(executor);
            while (true) {
                try {
                    Thread.sleep(3000);
                    ThreadPoolExecutor execRef = reference.get();
                    if (execRef == null || execRef.getActiveCount() < 1) {
                        System.out.println("No active threads. Terminating monitoring: " + execRef);
                        break;
                    }

                    System.out.println(execRef);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Cleared");
        });
        thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }
}
