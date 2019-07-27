package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import com.myseotoolbox.crawler.websitecrawl.CrawlStartedEvent;
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
    private final URI origin;
    private final List<URI> seeds;
    private final CrawlerQueue crawlerQueue;

    public CrawlJob(URI origin, Collection<URI> seeds, WebPageReader pageReader, UriFilter uriFilter, ThreadPoolExecutor executor, int maxCrawls, CrawlEventDispatch dispatch) {
        this.origin = origin;
        this.seeds = new ArrayList<>(seeds);
        String name = origin.getHost();
        CrawlersPool pool = new CrawlersPool(pageReader, executor);
        this.crawlerQueue = new CrawlerQueue(name, removeSeedsOutsideOrigin(origin, seeds), pool, uriFilter, maxCrawls, dispatch);
        startMonitoring(name, executor);
        this.dispatch = dispatch;
    }

    private void startMonitoring(String name, ThreadPoolExecutor executor) {
        new CrawlerPoolStatusMonitor(name, executor).start();
    }

    public void start() {
        notifyCrawlStart();
        crawlerQueue.start();
    }

    private List<URI> removeSeedsOutsideOrigin(URI origin, Collection<URI> seeds) {
        List<URI> filtered = seeds.stream().filter(u -> WebsiteOriginUtils.isHostMatching(origin, u)).collect(Collectors.toList());
        if (filtered.size() != seeds.size())
            log.warn("Seeds from external domains found on {}. Original Seeds: {} Filtered Seeds: {}", origin, seeds.size(), filtered.size());
        return filtered;
    }

    private void notifyCrawlStart() {
        List<String> collect = seeds.subList(0, Math.min(seeds.size(), 20)).stream().map(URI::toString).collect(Collectors.toList());
        dispatch.crawlStarted(new CrawlStartedEvent(origin.toString(), collect));
    }

}


