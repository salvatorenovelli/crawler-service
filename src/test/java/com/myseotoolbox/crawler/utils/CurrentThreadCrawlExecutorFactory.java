package com.myseotoolbox.crawler.utils;

import com.myseotoolbox.crawler.spider.CrawlExecutorFactory;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;

import java.util.concurrent.ThreadPoolExecutor;

public class CurrentThreadCrawlExecutorFactory extends CrawlExecutorFactory {
    @Override
    public ThreadPoolExecutor buildExecutor(String namePostfix, int concurrentConnections) {
        return new CurrentThreadTestExecutorService();
    }
}
