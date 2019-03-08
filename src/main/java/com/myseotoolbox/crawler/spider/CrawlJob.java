package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.isHostMatching;

@Slf4j
public class CrawlJob {

    private final CrawlerQueue crawlerQueue;

    public CrawlJob(URI websiteOrigin, List<URI> seeds, WebPageReader pageReader, UriFilter uriFilter, ExecutorService executor) {
        List<URI> allSeeds = addOriginToSeeds(websiteOrigin, seeds);
        CrawlersPool pool = new CrawlersPool(pageReader, executor);
        this.crawlerQueue = new CrawlerQueue(allSeeds, pool, uriFilter);
    }

    public void subscribeToPageCrawled(Consumer<PageSnapshot> subscriber) {
        this.crawlerQueue.subscribeToPageCrawled(subscriber);
    }

    public void start() {
        crawlerQueue.start();
    }

    private List<URI> addOriginToSeeds(URI websiteOrigin, List<URI> seeds) {
        verifySameOrigin(websiteOrigin, seeds);
        List<URI> allSeeds = new ArrayList<>(seeds);
        allSeeds.add(websiteOrigin);
        return allSeeds.stream().distinct().collect(Collectors.toList());
    }

    private void verifySameOrigin(URI websiteOrigin, List<URI> seeds) {
        if (seeds.stream().anyMatch(uri -> !isHostMatching(websiteOrigin, uri)))
            throw new IllegalStateException("Seeds host must match website origin. Origin: " + websiteOrigin + " Seeds:" + seeds);
    }

}


