package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CrawlJob {

    private final CrawlEventDispatch dispatch;
    @Getter private final WebsiteCrawl websiteCrawl;
    private final CrawlerQueue crawlerQueue;
    private final ThreadPoolExecutor executor;
    private CrawlerPoolStatusMonitor crawlerPoolStatusMonitor;

    public CrawlJob(CrawlJobConfiguration configuration,
                    CrawlerQueue crawlerQueue,
                    ThreadPoolExecutor executor,
                    CrawlEventDispatch dispatch) {

        this.executor = executor;
        this.websiteCrawl = configuration.getWebsiteCrawl();
        this.crawlerQueue = crawlerQueue;
        initMonitoring(configuration.getOrigin().toString(), executor);
        this.dispatch = dispatch;
    }

    private void initMonitoring(String name, ThreadPoolExecutor executor) {
        crawlerPoolStatusMonitor = new CrawlerPoolStatusMonitor(name, executor);
    }

    public void run() {
        notifyCrawlStart();
        crawlerQueue.start();
        crawlerPoolStatusMonitor.start();
        join();
    }

    private void join() {
        try {
            if (!executor.awaitTermination(5, TimeUnit.HOURS)) {
                log.warn("Crawl did not terminate in time, forcing shutdown. WebsiteCrawl: {} ", websiteCrawl);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void notifyCrawlStart() {
        dispatch.onCrawlStarted();
    }

    public String getWebsiteCrawlId() {
        return websiteCrawl.getId().toHexString();
    }
}