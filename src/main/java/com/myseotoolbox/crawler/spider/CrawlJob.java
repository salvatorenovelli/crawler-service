package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
public class CrawlJob {

    private final CrawlEventDispatch dispatch;
    @Getter private final WebsiteCrawl websiteCrawl;
    @Getter private final URI crawlOrigin;
    private final List<URI> seeds;
    private final CrawlerQueue crawlerQueue;
    private CrawlerPoolStatusMonitor crawlerPoolStatusMonitor;

    public CrawlJob(WebsiteCrawl websiteCrawl, URI crawlOrigin, Collection<URI> seeds, WebPageReader pageReader, UriFilter uriFilter, ThreadPoolExecutor executor, int maxCrawls, CrawlEventDispatch dispatch) {
        this.websiteCrawl = websiteCrawl;
        this.crawlOrigin = crawlOrigin;
        this.seeds = new ArrayList<>(seeds);
        String name = this.crawlOrigin.getHost();
        CrawlersPool pool = new CrawlersPool(pageReader, executor);
        this.crawlerQueue = new CrawlerQueue(name, removeSeedsOutsideOrigin(this.crawlOrigin, seeds), pool, uriFilter, maxCrawls, dispatch);
        initMonitoring(name, executor);
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

    private List<URI> removeSeedsOutsideOrigin(URI origin, Collection<URI> seeds) {
        List<URI> filtered = seeds.stream().filter(u -> WebsiteOriginUtils.isHostMatching(origin, u)).collect(Collectors.toList());
        if (filtered.size() != seeds.size())
            log.warn("Seeds from external domains found on {}. Original Seeds: {} Filtered Seeds: {}", origin, seeds.size(), filtered.size());
        return filtered;
    }

    private void notifyCrawlStart() {
        dispatch.onCrawlStarted();
    }

    public String getWebsiteCrawlId() {
        return websiteCrawl.getId().toHexString();
    }
}


