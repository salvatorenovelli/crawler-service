package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CrawlJob implements SnapshotCompletedListener {

    private final CrawlerTaskQueue queue;

    public CrawlJob(List<URI> seeds, WebPageReader pageReader, int concurrentConnections) {
        ExecutorService executor = Executors.newFixedThreadPool(concurrentConnections);
        CrawlersPool pool = new CrawlersPool(pageReader, this, executor);
        queue = new CrawlerTaskQueue(seeds, pool);
    }

    public void start() {
        queue.start();
    }

    @Override
    public void onSnapshotComplete(PageSnapshot snapshot) {
        if (snapshot.getLinks() != null && snapshot.getLinks().size() > 0) {
            queue.onNewLinksDiscovered(URI.create(snapshot.getUri()), snapshot.getLinks().stream().map(URI::create).collect(Collectors.toList()));
        }
    }
}


