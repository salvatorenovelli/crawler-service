package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class CrawlJob {

    private final CrawlEventDispatch dispatch;
    @Getter private final WebsiteCrawl websiteCrawl;
    private final CrawlerQueue crawlerQueue;
    private CrawlerPoolStatusMonitor crawlerPoolStatusMonitor;

    public CrawlJob(CrawlJobConfiguration configuration,
                    CrawlerQueue crawlerQueue,
                    ThreadPoolExecutor executor,
                    CrawlEventDispatch dispatch) {

        this.websiteCrawl = configuration.getWebsiteCrawl();
        this.crawlerQueue = crawlerQueue;
        initMonitoring(configuration.getOrigin().toString(), executor);
        this.dispatch = dispatch;
    }

    private void initMonitoring(String name, ThreadPoolExecutor executor) {
        crawlerPoolStatusMonitor = new CrawlerPoolStatusMonitor(name, executor);
    }

    public void start() {
        notifyCrawlStart();
        crawlerQueue.start();
        crawlerPoolStatusMonitor.start();
    }

    private void notifyCrawlStart() {
        dispatch.onCrawlStarted();
    }

    public String getWebsiteCrawlId() {
        return websiteCrawl.getId().toHexString();
    }
}